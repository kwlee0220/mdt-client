package mdt.model.expr;

import java.time.Duration;
import java.time.Instant;

import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.Nullable;

import utils.Instants;
import utils.Preconditions;
import utils.func.FOption;

import mdt.model.expr.TimeSeriesRangeExpr.AbsoluteExpr;
import mdt.model.expr.TimeSeriesRangeExpr.LengthExpr;
import mdt.model.expr.TimeSeriesRangeExpr.TrailingExpr;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;
import mdt.model.sm.ref.timeseries.TimeSeriesRange.Anchor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public sealed interface TimeSeriesRangeExpr extends MDTExpression
											permits LengthExpr, TrailingExpr, AbsoluteExpr {
	public abstract TimeSeriesRange evaluate();
	
	public static final class LengthExpr implements TimeSeriesRangeExpr {
		private final int m_length;
		
		public LengthExpr(int length) {
			m_length = length;
		}
		
		@Override
		public TimeSeriesRange evaluate() {
			return TimeSeriesRange.length(m_length);
		}
	}
	
	public static final class TrailingExpr implements TimeSeriesRangeExpr {
		private final SimpleValueExpr<Duration> m_durExpr;
		@Nullable private final ParseTree m_anchor;
		
		public TrailingExpr(SimpleValueExpr<Duration> durExpr, ParseTree anchor) {
			m_durExpr = durExpr;
			m_anchor = anchor;
		}
		
		@Override
		public TimeSeriesRange evaluate() {
			Duration duration = m_durExpr.evaluate();
			String anchorStr = (m_anchor != null) ? m_anchor.getText() : "LATEST";
			Anchor anchor = Anchor.valueOf(anchorStr.toUpperCase());  // validate anchor
			
			return TimeSeriesRange.last(duration, anchor);
		}
	}
	
	public static final class AbsoluteExpr implements TimeSeriesRangeExpr {
		@Nullable private final TerminalExpr m_startExpr;
		@Nullable private final TerminalExpr m_endExpr;
		
		public AbsoluteExpr(TerminalExpr startExpr, TerminalExpr endExpr) {
			Preconditions.checkArgument(startExpr != null || endExpr != null,
										"at least one of startExpr and endExpr must be non-null");
			
			m_startExpr = startExpr;
			m_endExpr = endExpr;
		}
		
		@Override
		public TimeSeriesRange evaluate() {
			String start = (m_startExpr != null) ? (String)m_startExpr.evaluate() : null;
			if ( start != null && !(start instanceof String) ) {
				throw new IllegalArgumentException("start must be a String expression: " + start);
			}
			String end = (m_endExpr != null) ? (String)m_endExpr.evaluate() : null;
			if ( end != null && !(end instanceof String) ) {
				throw new IllegalArgumentException("end must be a String expression: " + end);
			}
			
			Instant startTs = FOption.map(start, Instants::fromString);  // validate startStr
			Instant endTs = FOption.map(end, Instants::fromString);  // validate endStr
			return TimeSeriesRange.between(startTs, endTs);
		}
	}
}
