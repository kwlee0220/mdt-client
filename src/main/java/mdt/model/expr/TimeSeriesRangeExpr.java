package mdt.model.expr;

import java.time.Duration;
import java.time.Instant;

import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.Nullable;

import utils.Instants;
import utils.Preconditions;
import utils.func.Nullables;

import mdt.model.expr.TimeSeriesRangeExpr.AbsoluteExpr;
import mdt.model.expr.TimeSeriesRangeExpr.LengthExpr;
import mdt.model.expr.TimeSeriesRangeExpr.SinceExpr;
import mdt.model.expr.TimeSeriesRangeExpr.TrailingExpr;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;
import mdt.model.sm.ref.timeseries.TimeSeriesRange.Anchor;

/**
 * 시계열 조회 구간({@code tsRangeSpec})을 나타내는 표현식이다.
 * <p>
 * 평가 결과는 {@link TimeSeriesRange}이며, 구간 지정 방식에 따라 다음 구현이 있다.
 * <ul>
 *   <li>{@link LengthExpr}: 마지막 N개 레코드 ({@code last=N})</li>
 *   <li>{@link TrailingExpr}: 기준 시각으로부터 마지막 기간 ({@code last=<duration>[@now|@latest]})</li>
 *   <li>{@link AbsoluteExpr}: 절대 시각 구간 ({@code from~to}, {@code from~}, {@code ~to})</li>
 *   <li>{@link SinceExpr}: 시작 시각부터 N개 레코드 ({@code from+N})</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public sealed interface TimeSeriesRangeExpr extends MDTExpression
											permits LengthExpr, TrailingExpr, AbsoluteExpr, SinceExpr {
	/**
	 * 표현식이 나타내는 시계열 구간을 생성한다.
	 *
	 * @return	시계열 구간.
	 */
	public abstract TimeSeriesRange evaluate();
	
	/** 마지막 N개 레코드 구간 표현식 ({@code last=N}). */
	public static final class LengthExpr implements TimeSeriesRangeExpr {
		private final int m_length;
		
		/**
		 * 표현식을 생성한다.
		 *
		 * @param length	레코드 개수.
		 */
		public LengthExpr(int length) {
			m_length = length;
		}
		
		@Override
		public TimeSeriesRange evaluate() {
			return TimeSeriesRange.length(m_length);
		}
	}
	
	/**
	 * 기준 시각으로부터 마지막 기간 구간 표현식 ({@code last=<duration>[@now|@latest]}).
	 * <p>
	 * 기준 시각(anchor)이 생략되면 {@code latest}(가장 최신 데이터 시각)를 사용한다.
	 */
	public static final class TrailingExpr implements TimeSeriesRangeExpr {
		private final SimpleValueExpr<Duration> m_durExpr;
		@Nullable private final ParseTree m_anchor;
		
		/**
		 * 표현식을 생성한다.
		 *
		 * @param durExpr	기간 표현식.
		 * @param anchor	기준 시각을 나타내는 파스 트리 ({@code now} 또는 {@code latest}). 생략된 경우는 {@code null}.
		 */
		public TrailingExpr(SimpleValueExpr<Duration> durExpr, ParseTree anchor) {
			m_durExpr = durExpr;
			m_anchor = anchor;
		}
		
		/**
		 * 기간과 기준 시각으로 구간을 생성한다.
		 *
		 * @return	시계열 구간.
		 * @throws IllegalArgumentException	기준 시각 이름이 {@link Anchor} 상수에 해당하지 않는 경우.
		 */
		@Override
		public TimeSeriesRange evaluate() {
			Duration duration = m_durExpr.evaluate();
			String anchorStr = (m_anchor != null) ? m_anchor.getText() : "LATEST";
			Anchor anchor = Anchor.valueOf(anchorStr.toUpperCase());  // validate anchor
			
			return TimeSeriesRange.last(duration, anchor);
		}
	}
	
	/**
	 * 절대 시각 구간 표현식 ({@code from~to}, {@code from~}, {@code ~to}).
	 * <p>
	 * 시작 또는 종료 시각 중 한쪽은 생략될 수 있으며, 생략된 쪽은 열린 구간이 된다.
	 */
	public static final class AbsoluteExpr implements TimeSeriesRangeExpr {
		@Nullable private final TerminalExpr m_startExpr;
		@Nullable private final TerminalExpr m_endExpr;
		
		/**
		 * 표현식을 생성한다.
		 *
		 * @param startExpr	시작 시각 문자열 표현식. 생략된 경우는 {@code null}.
		 * @param endExpr	종료 시각 문자열 표현식. 생략된 경우는 {@code null}.
		 * @throws IllegalArgumentException	{@code startExpr}와 {@code endExpr}가 모두 {@code null}인 경우.
		 */
		public AbsoluteExpr(TerminalExpr startExpr, TerminalExpr endExpr) {
			Preconditions.checkArgument(startExpr != null || endExpr != null,
										"at least one of startExpr and endExpr must be non-null");
			
			m_startExpr = startExpr;
			m_endExpr = endExpr;
		}
		
		/**
		 * 시작/종료 시각 문자열을 {@link Instant}로 해석하여 구간을 생성한다.
		 *
		 * @return	시계열 구간.
		 * @throws IllegalArgumentException	시작/종료 표현식의 평가 결과가 문자열이 아닌 경우.
		 * @throws RuntimeException	시각 문자열을 해석할 수 없는 경우 ({@link Instants#parse(String)}의 예외).
		 */
		@Override
		public TimeSeriesRange evaluate() {
			String start = evaluateString(m_startExpr, "start");
			String end = evaluateString(m_endExpr, "end");
			
			Instant startTs = Nullables.map(start, Instants::parse);  // validate startStr
			Instant endTs = Nullables.map(end, Instants::parse);  // validate endStr
			return TimeSeriesRange.between(startTs, endTs);
		}
		
		private static String evaluateString(@Nullable TerminalExpr expr, String name) {
			if ( expr == null ) {
				return null;
			}
			Object value = expr.evaluate();
			if ( value != null && !(value instanceof String) ) {
				throw new IllegalArgumentException(name + " must be a String expression: " + value);
			}
			return (String)value;
		}
	}

	/** 시작 시각(포함)부터 시간 오름차순으로 N개 레코드 구간 표현식 ({@code from+N}). */
	public static final class SinceExpr implements TimeSeriesRangeExpr {
		private final TerminalExpr m_fromExpr;
		private final int m_length;

		/**
		 * 표현식을 생성한다.
		 *
		 * @param fromExpr	시작 시각 문자열 표현식.
		 * @param length	레코드 개수 (0 이상).
		 * @throws IllegalArgumentException	{@code fromExpr}가 {@code null}이거나 {@code length}가 음수인 경우.
		 */
		public SinceExpr(TerminalExpr fromExpr, int length) {
			Preconditions.checkNotNullArgument(fromExpr, "fromExpr is null");
			Preconditions.checkArgument(length >= 0, "length must be non-negative: length=%s", length);

			m_fromExpr = fromExpr;
			m_length = length;
		}

		/**
		 * 시작 시각 문자열을 {@link Instant}로 해석하여 구간을 생성한다.
		 *
		 * @return	시계열 구간.
		 * @throws IllegalArgumentException	시작 시각 표현식의 값이 문자열이 아닌 경우.
		 */
		@Override
		public TimeSeriesRange evaluate() {
			Object from = m_fromExpr.evaluate();
			if ( !(from instanceof String fromStr) ) {
				throw new IllegalArgumentException("'from' must be a String expression: " + from);
			}

			Instant fromTs = Instants.parse(fromStr);  // validate fromStr
			return TimeSeriesRange.since(fromTs, m_length);
		}
	}
}
