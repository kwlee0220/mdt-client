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
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public sealed interface TimeSeriesRange permits TimeSeriesRange.Count, TimeSeriesRange.Trailing, TimeSeriesRange.Absolute {
	/** trailing 구간의 기준점. */
	enum Anchor { LATEST, NOW }

	// --- 팩토리 (기존 호출자 호환) ---

	/** 마지막 {@code length}개 레코드 범위를 생성한다(MdtExpr {@code last=N}). */
	static TimeSeriesRange length(int length) {
		return new Count(length);
	}
	/** 마지막 레코드 시각 기준({@link Anchor#LATEST}) trailing 범위를 생성한다. */
	static TimeSeriesRange last(Duration duration) {
		return new Trailing(duration, Anchor.LATEST);
	}
	/** anchor를 명시한 trailing 범위를 생성한다. */
	static TimeSeriesRange last(Duration duration, Anchor anchor) {
		return new Trailing(duration, anchor);
	}
	/** 절대 시간 범위 {@code [from, to]}를 생성한다. 한쪽이 {@code null}이면 개방 구간이다. */
	static TimeSeriesRange between(@Nullable Instant from, @Nullable Instant to) {
		return new Absolute(from, to);
	}
	
	public static TimeSeriesRange parse(String str) {
		return MDTExpressionParser.parseTimeSeriesRange(str).evaluate();
	}

	// --- 변형 ---

	/** 마지막 {@code length}개 레코드. */
	record Count(int length) implements TimeSeriesRange {
		public Count {
			Preconditions.checkArgument(length >= 0, "length must be non-negative: length=%d", length);
		}
		
		@Override
		public String toString() {
			return String.format("last=%d", length);
		}
	}

	/** trailing 기간 구간. */
	record Trailing(Duration duration, Anchor anchor) implements TimeSeriesRange {
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

	/** 절대 시간 범위 {@code [from, to]}. {@code from}/{@code to} 중 하나가 {@code null}이면 개방 구간. */
	record Absolute(@Nullable Instant from, @Nullable Instant to) implements TimeSeriesRange {
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
}
