package mdt.test;

import java.io.IOException;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.expr.LiteralExpr;
import mdt.model.expr.MDTExpression;
import mdt.model.expr.MDTExpressionParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTSubmodelReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMDTExprParser {
	public static final void main(String... args) throws Exception {
		MDTExpressionParser evaluator = new MDTExpressionParser();
		
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
		
		String expr = "test:Data:DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		printElementReference(manager, expr);
		
		String idShortPath = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		expr = String.format("test:\"Data\":%s", idShortPath);
		printElementReference(manager, expr);
		
		idShortPath = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		expr = String.format("test:idShort=Data:%s", idShortPath);
		printElementReference(manager, expr);
		
		idShortPath = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		expr = String.format("submodel:id='http://mdt.etri.re.kr/mdt/Test/sm/Data':%s", idShortPath);
		printElementReference(manager, expr);
		
		printElementReference(manager, "param:test:IncAmount");
		printElementReference(manager, "param:test:*");
		printElementReference2(manager, "param:test:*");
		
		printElementReference(manager, "oparg:test:AddAndSleep:in:SleepTime");
		printElementReference(manager, "oparg:test:AddAndSleep:out:0");
		printElementReference2(manager, "oparg:test:AddAndSleep:out:0");
	}
	
	private static void printElementReference(MDTInstanceManager manager, String exprStr) throws IOException {
		MDTExpression expr = MDTExpressionParser.parseExpr(exprStr);
		ElementReference result = (ElementReference)expr.evaluate();
		if ( result instanceof MDTElementReference ref ) {
			ref.activate(manager);
		}
		else if ( result instanceof MDTSubmodelReference smRef ) {
			smRef.activate(manager);
		}
		System.out.println(result.read());
		System.out.println(result.readValue());
	}
	
	private static void printElementReference2(MDTInstanceManager manager, String exprStr) throws IOException {
		MDTElementReference ref2 = ElementReferences.parseExpr(exprStr);
		ref2.activate(manager);
		System.out.println(ref2.read());
		System.out.println(ref2.readValue());
	}
}