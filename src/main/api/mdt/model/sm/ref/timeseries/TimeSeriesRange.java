package mdt.model.sm.ref.timeseries;

import java.time.Duration;
import java.time.Instant;

import org.jetbrains.annotations.Nullable;

import utils.Preconditions;

import mdt.model.expr.MDTExpressionParser;


/**
 * 시계열 레코드 조회 범위를 표현하는 sealed 타입.
 * <p>
 * MdtExpr 문법의 {@code tsRangeSpec}과 1:1로 대응한다.
 * <ul>
 *   <li>{@link Count} &mdash; {@code last=N}(접미사 없는 정수) : 마지막 N개 레코드.
 *   <li>{@link Trailing} &mdash; {@code last=dur@anchor} : trailing 기간 구간.
 *       {@link Anchor#LATEST}는 마지막 레코드 시각 기준, {@link Anchor#NOW}는 현재 시각 기준.
 *   <li>{@link Absolute} &mdash; {@code from~to} : 절대 시간 범위(한쪽 개방 가능).
 *   <li>{@link Since} &mdash; {@code from+N} : {@code from} 시각부터 시간 오름차순으로 N개 레코드.
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public sealed interface TimeSeriesRange permits TimeSeriesRange.Count, TimeSeriesRange.Trailing,
												TimeSeriesRange.Absolute, TimeSeriesRange.Since {
	/** trailing 구간의 기준점. */
	enum Anchor {
		/** 마지막(가장 최신) 레코드의 시각을 기준점으로 한다(MdtExpr {@code @latest}, 기본값). */
		LATEST,
		/** 현재 시각을 기준점으로 한다(MdtExpr {@code @now}). */
		NOW
	}

	// --- 팩토리 (기존 호출자 호환) ---

	/**
	 * 마지막 {@code length}개 레코드 범위를 생성한다(MdtExpr {@code last=N}).
	 *
	 * @param length	레코드 개수.
	 * @return	{@link Count} 범위 객체.
	 * @throws IllegalArgumentException	{@code length}가 음수인 경우.
	 */
	static TimeSeriesRange length(int length) {
		return new Count(length);
	}
	/**
	 * 마지막 레코드 시각 기준({@link Anchor#LATEST}) trailing 범위를 생성한다(MdtExpr {@code last=dur}).
	 *
	 * @param duration	구간 길이.
	 * @return	{@link Trailing} 범위 객체.
	 * @throws IllegalArgumentException	{@code duration}이 {@code null}이거나 음수인 경우.
	 */
	static TimeSeriesRange last(Duration duration) {
		return new Trailing(duration, Anchor.LATEST);
	}
	/**
	 * anchor를 명시한 trailing 범위를 생성한다(MdtExpr {@code last=dur@anchor}).
	 *
	 * @param duration	구간 길이.
	 * @param anchor	구간의 기준점.
	 * @return	{@link Trailing} 범위 객체.
	 * @throws IllegalArgumentException	{@code duration}이나 {@code anchor}가 {@code null}이거나,
	 * 									{@code duration}이 음수인 경우.
	 */
	static TimeSeriesRange last(Duration duration, Anchor anchor) {
		return new Trailing(duration, anchor);
	}
	/**
	 * 절대 시간 범위 {@code [from, to]}를 생성한다(MdtExpr {@code from~to}).
	 * 한쪽이 {@code null}이면 개방 구간이다.
	 *
	 * @param from	구간 시작 시각. {@code null}이면 시작 제한 없음.
	 * @param to	구간 종료 시각. {@code null}이면 종료 제한 없음.
	 * @return	{@link Absolute} 범위 객체.
	 * @throws IllegalArgumentException	두 시각이 모두 {@code null}이거나 {@code from}이 {@code to}보다
	 * 									늦은 경우.
	 */
	static TimeSeriesRange between(@Nullable Instant from, @Nullable Instant to) {
		return new Absolute(from, to);
	}
	/**
	 * {@code from} 시각부터 시간 오름차순으로 {@code length}개 레코드 범위를 생성한다(MdtExpr {@code from+N}).
	 *
	 * @param from	시작 시각(포함).
	 * @param length	레코드 개수.
	 * @return	{@link Since} 범위 객체.
	 * @throws IllegalArgumentException	{@code from}이 {@code null}이거나 {@code length}가 음수인 경우.
	 */
	static TimeSeriesRange since(Instant from, int length) {
		return new Since(from, length);
	}
	
	/**
	 * MdtExpr {@code tsRangeSpec} 문법의 문자열을 파싱하여 범위 객체를 생성한다.
	 * <p>
	 * 지원 형식: {@code last=N}, {@code last=dur[@latest|@now]}({@code dur}은 {@code 10s}, {@code 5m} 같은
	 * 단위 접미사 형식 또는 {@code PT1H} 같은 ISO8601 기간), {@code from~to}, {@code from~},
	 * {@code ~to}, {@code from+N}. 각 변형의 {@code toString()}은 이 형식의 문자열을 반환한다.
	 *
	 * @param str	파싱할 범위 문자열.
	 * @return	파싱된 범위 객체.
	 * @throws mdt.model.expr.MDTParserException	문법 오류가 있는 경우.
	 * @throws IllegalArgumentException	{@code str}이 {@code null}인 경우.
	 */
	public static TimeSeriesRange parse(String str) {
		return MDTExpressionParser.parseTimeSeriesRange(str).evaluate();
	}

	// --- 변형 ---

	/**
	 * 마지막 {@code length}개 레코드 범위(MdtExpr {@code last=N}).
	 *
	 * @param length	레코드 개수.
	 */
	record Count(int length) implements TimeSeriesRange {
		/**
		 * 범위 객체를 생성한다.
		 *
		 * @param length	레코드 개수.
		 * @throws IllegalArgumentException	{@code length}가 음수인 경우.
		 */
		public Count {
			Preconditions.checkArgument(length >= 0, "length must be non-negative: length=%s", length);
		}
		
		@Override
		public String toString() {
			return String.format("last=%d", length);
		}
	}

	/**
	 * 기준점({@code anchor})으로부터 {@code duration}만큼 거슬러 올라간 trailing 기간 구간
	 * (MdtExpr {@code last=dur@anchor}).
	 *
	 * @param duration	구간 길이.
	 * @param anchor	구간의 기준점.
	 */
	record Trailing(Duration duration, Anchor anchor) implements TimeSeriesRange {
		/**
		 * 범위 객체를 생성한다.
		 *
		 * @param duration	구간 길이.
		 * @param anchor	구간의 기준점.
		 * @throws IllegalArgumentException	{@code duration}이나 {@code anchor}가 {@code null}이거나,
		 * 									{@code duration}이 음수인 경우.
		 */
		public Trailing {
			Preconditions.checkNotNullArgument(duration, "duration is null");
			Preconditions.checkArgument(!duration.isNegative(), "duration must be non-negative: duration=%s", duration);
			Preconditions.checkNotNullArgument(anchor, "anchor is null");
		}
		
		@Override
		public String toString() {
			return String.format("last=%s@%s", duration, anchor.name().toLowerCase());
		}
	}

	/**
	 * 절대 시간 범위 {@code [from, to]}(MdtExpr {@code from~to}).
	 * {@code from}/{@code to} 중 하나가 {@code null}이면 개방 구간.
	 *
	 * @param from	구간 시작 시각. {@code null}이면 시작 제한 없음.
	 * @param to	구간 종료 시각. {@code null}이면 종료 제한 없음.
	 */
	record Absolute(@Nullable Instant from, @Nullable Instant to) implements TimeSeriesRange {
		/**
		 * 범위 객체를 생성한다.
		 *
		 * @param from	구간 시작 시각. {@code null}이면 시작 제한 없음.
		 * @param to	구간 종료 시각. {@code null}이면 종료 제한 없음.
		 * @throws IllegalArgumentException	두 시각이 모두 {@code null}이거나 {@code from}이 {@code to}보다
		 * 									늦은 경우.
		 */
		public Absolute {
			Preconditions.checkArgument(from != null || to != null, "both 'from' and 'to' are null");
			Preconditions.checkArgument(from == null || to == null || !from.isAfter(to),
										"'from' must be <= 'to': from=%s, to=%s", from, to);
		}
		
		@Override
		public String toString() {
			if ( from != null && to != null ) {
				return String.format("%s~%s", from, to);
			}
			else if ( from != null ) {
				return String.format("%s~", from);
			}
			else /* if ( to != null ) */ {
				return String.format("~%s", to);
			}
		}
	}

	/**
	 * {@code from} 시각(포함)부터 시간 오름차순으로 {@code length}개 레코드 범위(MdtExpr {@code from+N}).
	 *
	 * @param from	시작 시각(포함).
	 * @param length	레코드 개수.
	 */
	record Since(Instant from, int length) implements TimeSeriesRange {
		/**
		 * 범위 객체를 생성한다.
		 *
		 * @param from	시작 시각(포함).
		 * @param length	레코드 개수.
		 * @throws IllegalArgumentException	{@code from}이 {@code null}이거나 {@code length}가 음수인 경우.
		 */
		public Since {
			Preconditions.checkNotNullArgument(from, "'from' is null");
			Preconditions.checkArgument(length >= 0, "length must be non-negative: length=%s", length);
		}

		@Override
		public String toString() {
			return String.format("%s+%d", from, length);
		}
	}
}
