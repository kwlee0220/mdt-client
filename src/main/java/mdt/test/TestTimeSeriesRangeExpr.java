package mdt.test;

import mdt.model.expr.MDTExpressionParser;
import mdt.model.expr.TimeSeriesRangeExpr;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestTimeSeriesRangeExpr {
	public static final void main(String... args) throws Exception {
		TimeSeriesRangeExpr expr = MDTExpressionParser.parseTimeSeriesRange("last=5");
		TimeSeriesRange range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("last=5s");
		range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("last=5m@latest");
		range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("last=5m@now");
		range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("2023-05-25T14:59:10~2023-05-25T15:00:10");
		range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("2023-05-25T14:59:10Z ~ 2023-05-25T15:00:10Z");
		range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("~ 2023-05-25T15:00:10Z");
		range = expr.evaluate();
		System.out.println(range);
		
		expr = MDTExpressionParser.parseTimeSeriesRange("2023-05-25T14:59:10Z ~");
		range = expr.evaluate();
		System.out.println(range);
	}
}