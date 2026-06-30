package mdt.model.expr;

import java.util.function.Function;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;

import mdt.model.expr.MdtExprParser.ExprContext;
import mdt.model.expr.MdtExprParser.FullElementSpecContext;
import mdt.model.expr.MdtExprParser.IdShortPathContext;
import mdt.model.expr.MdtExprParser.InstanceSpecContext;
import mdt.model.expr.MdtExprParser.SubmodelSpecContext;
import mdt.model.expr.MdtExprParser.TsRangeSpecContext;
import mdt.model.expr.MdtExprParser.ValueLiteralSpecContext;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.value.IdShortPath;


/*
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTExpressionParser {
	public static LiteralExpr parseValueLiteral(String exprStr) {
		ValueLiteralSpecContext tree = parse(exprStr, MdtExprParser::valueLiteralSpec);
		return (LiteralExpr)new MDTExpressionVisitor().visit(tree);
	}

	@SuppressWarnings("unchecked")
	public static SimpleValueExpr<IdShortPath> parseIdShortPath(String exprStr) {
		IdShortPathContext tree = parse(exprStr, MdtExprParser::idShortPath);
		return (SimpleValueExpr<IdShortPath>)new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * FullSubmodelSpec 문법에 해당하는 문자열을 파싱하여 {@link MDTSubmodelExpr} 객체를 생성한다.
	 *
	 * @param exprStr	Submodel 표현식
	 * @return	파싱된 {@link MDTSubmodelExpr} 객체.
	 */
	public static MDTSubmodelExpr parseSubmodelReference(String exprStr) {
		SubmodelSpecContext tree = parse(exprStr, MdtExprParser::submodelSpec);
		return (MDTSubmodelExpr)new MDTExpressionVisitor().visit(tree);
	}

	public static MDTElementReferenceExpr parseElementReference(String exprStr) {
		FullElementSpecContext tree = parse(exprStr, MdtExprParser::fullElementSpec);
		return (MDTElementReferenceExpr)new MDTExpressionVisitor().visit(tree);
	}

	public static StringExpr parseInstanceReference(String exprStr) {
		InstanceSpecContext tree = parse(exprStr, MdtExprParser::instanceSpec);
		return (StringExpr)new MDTExpressionVisitor().visit(tree);
	}
	
	public static TimeSeriesRangeExpr parseTimeSeriesRange(String exprStr) {
		TsRangeSpecContext tree = parse(exprStr, MdtExprParser::tsRangeSpec);
		return (TimeSeriesRangeExpr)new MDTExpressionVisitor().visit(tree);
	}

	public static MDTExpression parseExpr(String exprStr) {
		ExprContext tree = parse(exprStr, MdtExprParser::expr);
		return new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * 주어진 문자열을 지정한 파서 규칙으로 파싱한다.
	 * <p>
	 * 어휘/구문 오류 발생 시 {@link IllegalArgumentException}을 던지며, 규칙이 입력 전체를 소비하지
	 * 않고 남은 토큰이 있으면(후행 토큰) 역시 예외를 던진다(EOF 강제).
	 */
	private static <T extends ParserRuleContext> T parse(String exprStr,
														Function<MdtExprParser, T> rule) {
		Preconditions.checkArgument(exprStr != null, "expression is null");

		MdtExprLexer lexer = new MdtExprLexer(CharStreams.fromString(exprStr));
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MdtExprParser parser = new MdtExprParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowingErrorListener.INSTANCE);

		T tree = rule.apply(parser);

		Token next = parser.getCurrentToken();
		if ( next.getType() != Token.EOF ) {
			throw new IllegalArgumentException(
					String.format("Unexpected trailing input '%s' in expression: %s",
									next.getText(), exprStr));
		}
		return tree;
	}

	private static class ThrowingErrorListener extends BaseErrorListener {
		static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
								int charPositionInLine, String msg, RecognitionException e) {
			throw new IllegalArgumentException(
					String.format("Parse error at %d:%d - %s", line, charPositionInLine, msg));
		}
	}
}
