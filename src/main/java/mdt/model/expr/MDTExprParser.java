package mdt.model.expr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import mdt.model.expr.MDTSubmodelExpr.SubmodelByIdShortExpr;
import mdt.model.expr.MdtExprParser.DefaultSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.ExprContext;
import mdt.model.expr.MdtExprParser.FullElementSpecContext;
import mdt.model.expr.MdtExprParser.FullSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.IdShortPathContext;
import mdt.model.expr.MdtExprParser.InstanceSpecContext;
import mdt.model.expr.MdtExprParser.SubmodelSpecContext;
import mdt.model.expr.MdtExprParser.ValueLiteralSpecContext;
import mdt.model.expr.TerminalExpr.StringExpr;


/*
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTExprParser {
	public static LiteralExpr parseValueLiteral(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		ValueLiteralSpecContext tree = parser.valueLiteralSpec();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return (LiteralExpr)visitor.visit(tree);
	}
	
	public static MDTIdShortPathExpr parseIdShortPath(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		IdShortPathContext tree = parser.idShortPath();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return (MDTIdShortPathExpr) visitor.visit(tree);
	}
	
	public static MDTSubmodelExpr parseSubmodelReference(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		SubmodelSpecContext tree = parser.submodelSpec();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return (MDTSubmodelExpr)visitor.visit(tree);
	}
	
	public static SubmodelByIdShortExpr parseDefaultSubmodelReference(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		DefaultSubmodelSpecContext tree = parser.defaultSubmodelSpec();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return (SubmodelByIdShortExpr)visitor.visit(tree);
	}
	
	public static MDTElementReferenceExpr parseElementReference(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		FullElementSpecContext tree = parser.fullElementSpec();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return (MDTElementReferenceExpr)visitor.visit(tree);
	}
	
	public static StringExpr parseInstanceReference(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		InstanceSpecContext tree = parser.instanceSpec();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return (StringExpr)visitor.visit(tree);
	}
	
	public static MDTExpr parseExpr(String exprStr) {
		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);

		ExprContext tree = parser.expr();
		MDTExprVisitor visitor = new MDTExprVisitor();

		return visitor.visit(tree);
	}
}
