package mdt.model.expr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.checkerframework.com.google.common.base.Preconditions;

import mdt.model.expr.MdtExprParser.ExprContext;
import mdt.model.expr.MdtExprParser.FullElementSpecContext;
import mdt.model.expr.MdtExprParser.IdShortPathContext;
import mdt.model.expr.MdtExprParser.InstanceSpecContext;
import mdt.model.expr.MdtExprParser.SubmodelSpecContext;
import mdt.model.expr.MdtExprParser.ValueLiteralSpecContext;
import mdt.model.expr.TerminalExpr.StringExpr;


/*
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTExpressionParser {
	public static LiteralExpr parseValueLiteral(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		ValueLiteralSpecContext tree = parser.valueLiteralSpec();
		MDTExpressionVisitor visitor = new MDTExpressionVisitor();

		return (LiteralExpr)visitor.visit(tree);
	}
	
	public static MDTIdShortPathExpr parseIdShortPath(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		IdShortPathContext tree = parser.idShortPath();
		MDTExpressionVisitor visitor = new MDTExpressionVisitor();

		return (MDTIdShortPathExpr) visitor.visit(tree);
	}
	
	/**
	 * FullSubmodelSpec 문법에 해당하는 문자열을 파싱하여 {@link MDTSubmodelExpr} 객체를 생성한다.
	 *
	 * @param exprStr	Submodel 표현식
	 * @return	파싱된 {@link MDTSubmodelExpr} 객체.
	 */
	public static MDTSubmodelExpr parseSubmodelReference(String exprStr) {
		Preconditions.checkArgument(exprStr != null, "SubmodelReference expression is null");
		
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		SubmodelSpecContext tree = parser.submodelSpec();
		MDTExpressionVisitor visitor = new MDTExpressionVisitor();

		return (MDTSubmodelExpr)visitor.visit(tree);
	}
	
	public static MDTElementReferenceExpr parseElementReference(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		FullElementSpecContext tree = parser.fullElementSpec();
		MDTExpressionVisitor visitor = new MDTExpressionVisitor();

		return (MDTElementReferenceExpr)visitor.visit(tree);
	}
	
	public static StringExpr parseInstanceReference(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		InstanceSpecContext tree = parser.instanceSpec();
		MDTExpressionVisitor visitor = new MDTExpressionVisitor();

		return (StringExpr)visitor.visit(tree);
	}
	
	public static MDTExpression parseExpr(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		ExprContext tree = parser.expr();
		MDTExpressionVisitor visitor = new MDTExpressionVisitor();

		return visitor.visit(tree);
	}
}
