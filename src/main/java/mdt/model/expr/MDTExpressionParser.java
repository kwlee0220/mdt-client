package mdt.model.expr;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import utils.Preconditions;

import mdt.model.expr.MdtExprParser.ExprContext;
import mdt.model.expr.MdtExprParser.FullElementSpecContext;
import mdt.model.expr.MdtExprParser.IdShortPathContext;
import mdt.model.expr.MdtExprParser.InstanceSpecContext;
import mdt.model.expr.MdtExprParser.SubmodelSpecContext;
import mdt.model.expr.MdtExprParser.TsRangeSpecContext;
import mdt.model.expr.MdtExprParser.ValueLiteralSpecContext;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.sm.value.IdShortPath;


/**
 * MDT 표현식 문자열({@code MdtExpr} 문법)을 파싱하여 {@link MDTExpression} 객체를 생성하는
 * 정적 메소드들을 제공한다.
 * <p>
 * 각 메소드는 문법의 특정 규칙으로 입력 전체를 파싱하며, 어휘/구문 오류가 있거나 규칙 뒤에 남은 입력이
 * 있으면 {@link MDTParserException}을, {@code exprStr}이 {@code null}이면
 * {@link IllegalArgumentException}을 발생시킨다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTExpressionParser {
	/**
	 * 값 리터럴({@code valueLiteralSpec}) 문자열을 파싱한다.
	 *
	 * @param exprStr	값 리터럴 문자열 (예: {@code 'abc'}, {@code 10}, {@code [0, 1]}).
	 * @return	파싱된 {@link LiteralExpr} 객체.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	public static LiteralExpr parseValueLiteral(String exprStr) {
		ValueLiteralSpecContext tree = parse(exprStr, MdtExprParser::valueLiteralSpec);
		return (LiteralExpr)new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * idShortPath({@code idShortPath}) 문자열을 파싱한다.
	 *
	 * @param exprStr	idShortPath 문자열 (예: {@code a.b[0].c}).
	 * @return	파싱된 {@link IdShortPath}를 담은 표현식.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	@SuppressWarnings("unchecked")
	public static SimpleValueExpr<IdShortPath> parseIdShortPath(String exprStr) {
		IdShortPathContext tree = parse(exprStr, MdtExprParser::idShortPath);
		return (SimpleValueExpr<IdShortPath>)new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * Submodel 참조({@code submodelSpec}) 문자열을 파싱하여 {@link MDTSubmodelExpr} 객체를 생성한다.
	 *
	 * @param exprStr	Submodel 표현식 (예: {@code inst:Data}, {@code submodel:id=<id>}).
	 * @return	파싱된 {@link MDTSubmodelExpr} 객체.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	public static MDTSubmodelExpr parseSubmodelReference(String exprStr) {
		SubmodelSpecContext tree = parse(exprStr, MdtExprParser::submodelSpec);
		return (MDTSubmodelExpr)new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * SubmodelElement 참조({@code fullElementSpec}) 문자열을 파싱한다.
	 *
	 * @param exprStr	SubmodelElement 참조 문자열 (예: {@code param:inst:Speed}).
	 * @return	파싱된 {@link MDTElementReferenceExpr} 객체.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	public static MDTElementReferenceExpr parseElementReference(String exprStr) {
		FullElementSpecContext tree = parse(exprStr, MdtExprParser::fullElementSpec);
		return (MDTElementReferenceExpr)new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * MDTInstance 참조({@code instanceSpec}) 문자열을 파싱한다.
	 *
	 * @param exprStr	MDTInstance 식별자 문자열.
	 * @return	MDTInstance 식별자를 담은 {@link StringExpr}.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	public static StringExpr parseInstanceReference(String exprStr) {
		InstanceSpecContext tree = parse(exprStr, MdtExprParser::instanceSpec);
		return (StringExpr)new MDTExpressionVisitor().visit(tree);
	}
	
	/**
	 * 시계열 구간({@code tsRangeSpec}) 문자열을 파싱한다.
	 *
	 * @param exprStr	구간 문자열 (예: {@code last=10}, {@code last=1h@now}, {@code 2024-01-01~2024-02-01}).
	 * @return	파싱된 {@link TimeSeriesRangeExpr} 객체.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	public static TimeSeriesRangeExpr parseTimeSeriesRange(String exprStr) {
		TsRangeSpecContext tree = parse(exprStr, MdtExprParser::tsRangeSpec);
		return (TimeSeriesRangeExpr)new MDTExpressionVisitor().visit(tree);
	}

	/**
	 * 임의의 MDT 표현식({@code expr}) 문자열을 파싱한다.
	 * <p>
	 * 값 리터럴, Submodel 참조, SubmodelElement 참조, 대입식, {@code null} 중 하나이다.
	 *
	 * @param exprStr	표현식 문자열.
	 * @return	파싱된 {@link MDTExpression} 객체.
	 * @throws MDTParserException	문법에 맞지 않거나 남은 입력이 있는 경우.
	 */
	public static MDTExpression parseExpr(String exprStr) {
		ExprContext tree = parse(exprStr, MdtExprParser::expr);
		return new MDTExpressionVisitor().visit(tree);
	}

	// 따옴표 없이 쓸 수 있는 이름(ID 토큰). 문법(MdtExpr.g4)의 ID 규칙과 같다.
	private static final Pattern BARE_NAME = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_-]*");
	// ID 모양이지만 다른 토큰으로 인식되어 이름 자리에 따옴표 없이 쓸 수 없는 단어들.
	// soft keyword(idShort, param, last 등)는 이름 자리에 쓸 수 있으므로 포함하지 않는다.
	private static final Set<String> RESERVED_WORDS = Set.of("true", "false", "null",
															"ms", "s", "m", "h", "d", "from", "length");
	// ISO8601_DURATION 토큰 모양(P1D, PT5M 등).
	private static final Pattern ISO8601_DURATION = Pattern.compile("P(\\d+D)?(T[0-9.HMS]+)?");
	// idShortPath 한 구간: 이름 뒤에 [n] 인덱스가 0개 이상 붙은 형태.
	private static final Pattern PATH_SEGMENT = Pattern.compile("(.*?)((?:\\[\\d+\\])*)");

	/**
	 * 이름(인스턴스 id, Submodel idShort·id, 요소 idShort, 컬럼 이름 등)을 표현식에 쓸 수 있는 형태로 만든다.
	 * <p>
	 * 따옴표 없이 쓸 수 있는 이름은 그대로 두고, 그렇지 않으면(공백·특수문자 포함, 숫자로 시작, 예약 토큰,
	 * ISO8601 기간 모양 등) 작은따옴표로, 작은따옴표가 들어 있으면 큰따옴표로 감싼다. MdtExpr 문자열에는 escape
	 * 문법이 없으므로 두 종류의 따옴표가 모두 들어 있는 이름은 표현식으로 나타낼 수 없어 그대로 반환한다.
	 *
	 * @param name	이름.
	 * @return	표현식에 쓸 이름.
	 * @throws IllegalArgumentException	{@code name}이 {@code null}인 경우.
	 */
	public static String toNameExpr(String name) {
		Preconditions.checkNotNullArgument(name, "name is null");
		
		boolean bare = BARE_NAME.matcher(name).matches()
					&& !RESERVED_WORDS.contains(name)
					&& !(name.startsWith("P") && ISO8601_DURATION.matcher(name).matches());
		if ( bare ) {
			return name;
		}
		else if ( name.indexOf('\'') < 0 ) {
			return "'" + name + "'";
		}
		else if ( name.indexOf('"') < 0 ) {
			return '"' + name + '"';
		}
		else {
			return name;
		}
	}

	/**
	 * idShort 경로 문자열({@code A.B[0].C})의 각 idShort를 {@link #toNameExpr(String)}로 바꾼 표현식을 만든다.
	 * <p>
	 * 구간은 {@code .}로 나누므로 {@code .}이 들어 있는 idShort는 구분할 수 없다(AAS idShort에는 쓸 수 없다).
	 *
	 * @param path	idShort 경로 문자열.
	 * @return	표현식에 쓸 idShort 경로.
	 * @throws IllegalArgumentException	{@code path}가 {@code null}인 경우.
	 */
	public static String toIdShortPathExpr(String path) {
		Preconditions.checkNotNullArgument(path, "path is null");
		
		StringBuilder sb = new StringBuilder();
		for ( String seg: path.split("\\.", -1) ) {
			Matcher m = PATH_SEGMENT.matcher(seg);
			m.matches();
			if ( sb.length() > 0 ) {
				sb.append('.');
			}
			if ( !m.group(1).isEmpty() ) {
				sb.append(toNameExpr(m.group(1)));
			}
			sb.append(m.group(2));
		}
		return sb.toString();
	}

	/**
	 * 주어진 문자열을 지정한 파서 규칙으로 파싱한다.
	 * <p>
	 * 어휘/구문 오류가 나거나, 규칙이 입력 전체를 소비하지 않고 남은 토큰이 있으면(후행 토큰, EOF 강제)
	 * {@link MDTParserException}을 던진다. {@code exprStr}이 {@code null}이면 {@link IllegalArgumentException}이다.
	 * <p>
	 * 2026-11-01 이전에는 후행 토큰만 {@link IllegalArgumentException}으로 보고해, 호출자마다 두 예외를 함께 잡아야 했다.
	 *
	 * @param <T>	파서 규칙의 결과 컨텍스트 타입.
	 * @param exprStr	파싱할 문자열.
	 * @param rule	적용할 파서 규칙.
	 * @return	파싱 트리.
	 */
	private static <T extends ParserRuleContext> T parse(String exprStr,
														Function<MdtExprParser, T> rule) {
		Preconditions.checkNotNullArgument(exprStr, "expression is null");

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
			throw new MDTParserException(
					String.format("Unexpected trailing input at %d:%d - '%s' in expression: %s",
									next.getLine(), next.getCharPositionInLine(), next.getText(), exprStr));
		}
		return tree;
	}

	private static class ThrowingErrorListener extends BaseErrorListener {
		static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
								int charPositionInLine, String msg, RecognitionException e) {
			throw new MDTParserException(
					String.format("Parse error at %d:%d - %s", line, charPositionInLine, msg));
		}
	}
}
