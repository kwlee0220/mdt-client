package mdt.test;

import java.io.IOException;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.expr.LiteralExpr;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMDTExprParser {
	public static final void main(String... args) throws Exception {
		MDTExprParser evaluator = new MDTExprParser();
		
		LiteralExpr literal = evaluator.parseValueLiteral("'abc'");
		System.out.println(literal);
		
		System.out.println(evaluator.parseValueLiteral("'abc'@en"));
		System.out.println(evaluator.parseValueLiteral("file:'/tmp/test.txt'('/text/plain')"));
		System.out.println(evaluator.parseValueLiteral("['abc', 'abd']"));
		
		System.out.println(evaluator.parseIdShortPath("a.b.c").evaluate());
		System.out.println(evaluator.parseIdShortPath("a[4].c").evaluate());
		System.out.println(evaluator.parseIdShortPath("a[4][0]").evaluate());
		
		HttpMDTManager mdt = HttpMDTManager.connectWithDefault();
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		String expr = "welder:Data:DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		printElementReference(manager, expr);
		
		String idShortPath = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		expr = String.format("welder:\"Data\":%s", idShortPath);
		printElementReference(manager, expr);
		
		idShortPath = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		expr = String.format("welder:idShort=Data:%s", idShortPath);
		printElementReference(manager, expr);
		
		idShortPath = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		expr = String.format("submodel:id='https://www.samcheon.com/mdt/Welder/sm/Data':%s", idShortPath);
		printElementReference(manager, expr);
		
		printElementReference(manager, "param:welder:Ampere");
		printElementReference(manager, "param:welder:*");
		
		printElementReference(manager, "oparg:in:welder:Simulation:PeakQuality");
		printElementReference(manager, "oparg:out:welder:Simulation:0");
	}
	
	private static void printElementReference(MDTInstanceManager manager, String expr) throws IOException {
		MDTExprParser evaluator = new MDTExprParser();
		MDTElementReference ref = (MDTElementReference) evaluator.parseElementReference(expr).evaluate();
		ref.activate(manager);
		System.out.println(ref.read());
	}
}