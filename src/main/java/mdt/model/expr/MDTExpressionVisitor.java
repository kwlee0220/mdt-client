package mdt.model.expr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import utils.UnitUtils;

import mdt.model.expr.LiteralExpr.FileValueSpec;
import mdt.model.expr.LiteralExpr.MLPropertyValueSpec;
import mdt.model.expr.LiteralExpr.PropertyValueSpec;
import mdt.model.expr.LiteralExpr.RangeValueSpec;
import mdt.model.expr.MDTElementReferenceExpr.ArgumentReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.DefaultElementReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.OperationVariableReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.ParameterReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.TimeseriesReferenceExpr;
import mdt.model.expr.MDTSubmodelExpr.SubmodelByIdExpr;
import mdt.model.expr.MDTSubmodelExpr.SubmodelByIdShortExpr;
import mdt.model.expr.MdtExprParser.ArgumentSpecContext;
import mdt.model.expr.MdtExprParser.AssignmentExprContext;
import mdt.model.expr.MdtExprParser.DefaultElementSpecContext;
import mdt.model.expr.MdtExprParser.DefaultSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.DurationLiteralSpecContext;
import mdt.model.expr.MdtExprParser.FileValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.FullElementSpecContext;
import mdt.model.expr.MdtExprParser.FullInstanceSpecContext;
import mdt.model.expr.MdtExprParser.IdBasedSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.IdOrStringContext;
import mdt.model.expr.MdtExprParser.IdShortSegContext;
import mdt.model.expr.MdtExprParser.InstanceSpecContext;
import mdt.model.expr.MdtExprParser.Iso8601DurationLiteralSpecContext;
import mdt.model.expr.MdtExprParser.MlpPropertyValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.OpVarSpecContext;
import mdt.model.expr.MdtExprParser.ParameterSpecContext;
import mdt.model.expr.MdtExprParser.PropertyValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.RangeValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.SubmodelSpecContext;
import mdt.model.expr.MdtExprParser.TimeseriesSpecContext;
import mdt.model.expr.MdtExprParser.TsProjectionSpecContext;
import mdt.model.expr.MdtExprParser.TsRangeAbsoluteSpecContext;
import mdt.model.expr.MdtExprParser.TsRangeSpecContext;
import mdt.model.expr.TerminalExpr.BooleanExpr;
import mdt.model.expr.TerminalExpr.DoubleExpr;
import mdt.model.expr.TerminalExpr.IntegerExpr;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.expr.TerminalExpr.SymbolExpr;
import mdt.model.expr.TimeSeriesRangeExpr.AbsoluteExpr;
import mdt.model.expr.TimeSeriesRangeExpr.LengthExpr;
import mdt.model.expr.TimeSeriesRangeExpr.TrailingExpr;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.ref.OperationVariableReference;
import mdt.model.sm.ref.OperationVariableReference.Kind;
import mdt.model.sm.ref.timeseries.TimeSeriesRange;
import mdt.model.sm.value.IdShortPath;
import mdt.model.sm.value.PropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTExpressionVisitor extends MdtExprBaseVisitor<MDTExpression> {
	@Override
	public AssignmentExpr visitAssignmentExpr(AssignmentExprContext ctx) {
		MDTElementReferenceExpr lhs = (MDTElementReferenceExpr)ctx.getChild(0).accept(this);
		MDTExpression rhs = ctx.getChild(2).accept(this);
		
		return new AssignmentExpr(lhs, rhs);
	}
	
	@Override
	public StringExpr visitFullInstanceSpec(FullInstanceSpecContext ctx) {
		return visitInstanceSpec((InstanceSpecContext)ctx.getChild(2));
	}
	@Override
	public StringExpr visitInstanceSpec(InstanceSpecContext ctx) {
		return (StringExpr)ctx.getChild(0).accept(this);
	}

	@Override
	public MDTSubmodelExpr visitSubmodelSpec(SubmodelSpecContext ctx) {
		ParseTree pt = ctx.getChild(0);
		if ( pt instanceof DefaultSubmodelSpecContext ) {
			return (MDTSubmodelExpr) ctx.getChild(0).accept(this);
		}
		else if ( pt instanceof IdBasedSubmodelSpecContext ) {
			return (MDTSubmodelExpr) ctx.getChild(0).accept(this);
		}
		else {
			throw new IllegalArgumentException("Unknown submodel spec: " + ctx.getText());
		}
	}
	@Override
	public MDTSubmodelExpr visitDefaultSubmodelSpec(DefaultSubmodelSpecContext ctx) {
		StringExpr instIdExpr = (StringExpr)ctx.getChild(0).accept(this);
		
		int idExprIdx = (ctx.getChildCount() == 3) ? 2 : 4;
		StringExpr idExpr = (StringExpr)ctx.getChild(idExprIdx).accept(this);
		return new SubmodelByIdShortExpr(instIdExpr, idExpr);
	}
	@Override
	public MDTSubmodelExpr visitIdBasedSubmodelSpec(IdBasedSubmodelSpecContext ctx) {
		StringExpr idExpr = (StringExpr)ctx.getChild(4).accept(this);
        return new SubmodelByIdExpr(idExpr);
	}
	
	@Override
	public MDTElementReferenceExpr visitFullElementSpec(FullElementSpecContext ctx) {
		if ( ctx.getChild(0) instanceof TerminalNode ) {
			return (MDTElementReferenceExpr) ctx.getChild(2).accept(this);
		}
		else {
			return (MDTElementReferenceExpr) ctx.getChild(0).accept(this);
		}
	}
	
	@Override
	public DefaultElementReferenceExpr visitDefaultElementSpec(DefaultElementSpecContext ctx) {
		MDTSubmodelReference smRef = (MDTSubmodelReference)ctx.getChild(0).accept(this).evaluate();
		IdShortPath path = (IdShortPath)ctx.getChild(2).accept(this).evaluate();
		return new DefaultElementReferenceExpr(smRef, path);
	}

	@Override
	public ParameterReferenceExpr visitParameterSpec(ParameterSpecContext ctx) {
		StringExpr instIdExpr = (StringExpr)ctx.getChild(0).accept(this);
		MDTExpression paramExpr = ctx.getChild(2).accept(this);
		return new ParameterReferenceExpr(instIdExpr, paramExpr);
	}

	@Override
	public ArgumentReferenceExpr visitArgumentSpec(ArgumentSpecContext ctx) {
		MDTSubmodelExpr smExpr = (MDTSubmodelExpr)ctx.getChild(0).accept(this);
		
		String kindStr = (String)ctx.getChild(2).accept(this).evaluate();
		MDTArgumentKind kind = MDTArgumentKind.fromString(kindStr);
		
		ParseTree pt = ctx.getChild(4);
		SimpleValueExpr<String> argKeySpec = new SimpleValueExpr<>(pt.getText());
		
		return new ArgumentReferenceExpr(smExpr, kind, argKeySpec);
	}
	
	@Override
	public OperationVariableReferenceExpr visitOpVarSpec(OpVarSpecContext ctx) {
		// opVarSpec: defaultElementSpec ':' (in|out|inout) ':' INTEGER
		// defaultElementSpec이 가리키는 연산(Operation) 요소 참조를 그대로 사용한다.
		DefaultElementReferenceExpr opElmExpr = (DefaultElementReferenceExpr)ctx.getChild(0).accept(this);
		String kindStr = (String)ctx.getChild(2).accept(this).evaluate();
		Kind kind = OperationVariableReference.Kind.fromString(kindStr);
		IntegerExpr opVarIdxExpr = (IntegerExpr)ctx.getChild(4).accept(this);

		return new OperationVariableReferenceExpr(opElmExpr, kind, opVarIdxExpr);
	}

	@Override
	public SimpleValueExpr<IdShortPath> visitIdShortPath(MdtExprParser.IdShortPathContext ctx) {
		IdShortPath.Builder builder = IdShortPath.builder();
		
		TerminalExpr id = (TerminalExpr) ctx.getChild(0).accept(this);
		builder = builder.idShort((String)id.evaluate());
		
		if ( ctx.getChildCount() > 1 ) {
			for ( int i = 1; i < ctx.getChildCount(); ++i ) {
				ParseTree child = ctx.getChild(i);
				Object seg = child.accept(this).evaluate();
				if ( seg instanceof String key ) {
					builder = builder.idShort(key);
				}
				else if ( seg instanceof Integer index ) {
					builder = builder.index(index);
				}
				else {
					throw new IllegalArgumentException("Unknown segment type: " + seg.getClass());
				}
			}
		}
		return new SimpleValueExpr<>(builder.build());
	}
	
	@Override
	public TerminalExpr visitIdShortSeg(IdShortSegContext ctx) {
		ParseTree pt = ctx.getChild(0);
		if ( pt instanceof TerminalNode tn ) {
			switch ( MdtExprParser.VOCABULARY.getDisplayName(tn.getSymbol().getType()) ) {
				case "'.'":
					return (TerminalExpr)ctx.getChild(1).accept(this);
				case "'['":
					return (TerminalExpr)ctx.getChild(1).accept(this);
				default:
					throw new IllegalArgumentException("Unknown IdShort segment: " + ctx.getText());
			}
		}
		else {
			return (TerminalExpr)ctx.getChild(1).accept(this);
		}
	}
	
	@Override
	public PropertyValueSpec visitPropertyValueLiteralSpec(PropertyValueLiteralSpecContext ctx) {
		if ( ctx.getChildCount() == 2 ) {
			// 선두 '-' 부호가 붙은 음수 리터럴: '-' INTEGER  또는  '-' FLOAT
			TerminalNode num = (TerminalNode)ctx.getChild(1);
			String text = "-" + num.getText();
			TerminalExpr expr = (num.getSymbol().getType() == MdtExprParser.INTEGER)
								? new IntegerExpr(Integer.parseInt(text))
								: new DoubleExpr(Double.parseDouble(text));
			return new PropertyValueSpec(expr);
		}
		TerminalExpr expr = visitTerminal((TerminalNode)ctx.getChild(0));
		return new PropertyValueSpec(expr);
	}

	@Override
	public MLPropertyValueSpec visitMlpPropertyValueLiteralSpec(MlpPropertyValueLiteralSpecContext ctx) {
		String lang = (String)ctx.getChild(2).accept(this).evaluate();
		String text = (String)ctx.getChild(0).accept(this).evaluate();
		return new MLPropertyValueSpec(lang, text);
	}

	@Override
	public FileValueSpec visitFileValueLiteralSpec(FileValueLiteralSpecContext ctx) {
		String path = (String)ctx.getChild(2).accept(this).evaluate();
		String mimeType = (String)ctx.getChild(4).accept(this).evaluate();
		return new FileValueSpec(mimeType, path);
	}

	@Override
	public RangeValueSpec visitRangeValueLiteralSpec(RangeValueLiteralSpecContext ctx) {
		PropertyValue<?> min = (PropertyValue<?>)ctx.getChild(1).accept(this).evaluate();
		PropertyValue<?> max = (PropertyValue<?>)ctx.getChild(3).accept(this).evaluate();
		
		return new RangeValueSpec(min.getDataType(), min.toValueObject(), max.toValueObject());
	}
	
	@Override
	public NullExpr visitNullExpr(MdtExprParser.NullExprContext ctx) {
		return new NullExpr();
	}
	
	@Override
	public TerminalExpr visitTerminal(TerminalNode node) {
		switch ( node.getSymbol().getType() ) {
			case MdtExprParser.STRING:
				String wrapped = node.getText();
				String str = wrapped.substring(1, wrapped.length()-1);
                return new StringExpr(str);
			case MdtExprParser.ID:
                return new StringExpr(node.getText());
            case MdtExprParser.INTEGER:
                return new IntegerExpr(Integer.parseInt(node.getText()));
            case MdtExprParser.FLOAT:
            	return new DoubleExpr(Double.parseDouble(node.getText()));
            case MdtExprParser.BOOLEAN:
            	return new BooleanExpr(Boolean.parseBoolean(node.getText()));
            default:
            	return new SymbolExpr(node.toString());
		}
	}

	@Override
	public StringExpr visitIdOrString(IdOrStringContext ctx) {
		return (StringExpr)ctx.getChild(0).accept(this);
	}

	@Override
	public TimeseriesReferenceExpr visitTimeseriesSpec(TimeseriesSpecContext ctx) {
		// timeseriesSpec: submodelSpec ':' idShortPath ('#' tsRangeSpec)? ('|' tsProjectionSpec)?
		MDTSubmodelExpr smExpr = (MDTSubmodelExpr)ctx.getChild(0).accept(this);

		TimeSeriesRange range = null;
		List<String> columns = null;
		for ( int i = 2; i < ctx.getChildCount(); ++i ) {
			ParseTree child = ctx.getChild(i);
			if ( child instanceof TsRangeSpecContext rangeCtx ) {
				range = (TimeSeriesRange)rangeCtx.accept(this).evaluate();
			}
			else if ( child instanceof TsProjectionSpecContext projCtx ) {
				columns = toColumns(projCtx);
			}
			// '#', '|' 구분자(TerminalNode)는 건너뛴다.
		}
		return new TimeseriesReferenceExpr(smExpr, range, columns);
	}

	@Override
	public TimeSeriesRangeExpr visitTsRangeLastSpec(MdtExprParser.TsRangeLastSpecContext ctx) {
		// tsRangeLastSpec: 'last' '=' ( INTEGER | durationValue ('@' tsRangeAnchor)? )
		ParseTree value = ctx.getChild(2);
		if ( value instanceof TerminalNode tn && tn.getSymbol().getType() == MdtExprParser.INTEGER ) {
			// last=N (접미사 없는 정수): 마지막 N개 레코드
			int length = Integer.parseInt(tn.getText());
			return new LengthExpr(length);
		}
		else {
			// last=dur[@anchor]: 마지막 기간 구간.
			// anchor 기본값 'latest'(최신 데이터 기준 = Range.last), '@now'는 현재 시각 기준 = Range.duration
			@SuppressWarnings("unchecked")
			SimpleValueExpr<Duration> durExpr = (SimpleValueExpr<Duration>)value.accept(this);
			ParseTree anchorPt = (ctx.getChildCount() > 3) ? ctx.getChild(4) : null;
			return new TrailingExpr(durExpr, anchorPt);
		}
	}
	
	@Override
	public TimeSeriesRangeExpr visitTsRangeAbsoluteSpec(TsRangeAbsoluteSpecContext ctx) {
		// TIMESTAMP '~' TIMESTAMP | TIMESTAMP '~' | '~' TIMESTAMP
		if ( ctx.getChildCount() == 3 ) {
			// TIMESTAMP '~' TIMESTAMP
			if ( ctx.getChild(0) instanceof TerminalNode tn0
				&& tn0.getSymbol().getType() == MdtExprParser.TIMESTAMP
				&& ctx.getChild(2) instanceof TerminalNode tn2
				&& tn2.getSymbol().getType() == MdtExprParser.TIMESTAMP ) {
				// TIMESTAMP '~' TIMESTAMP
				TerminalExpr startExpr = (TerminalExpr)tn0.accept(this);
				TerminalExpr endExpr = (TerminalExpr)tn2.accept(this);
				return new AbsoluteExpr(startExpr, endExpr);
			}
			else {
				throw new IllegalArgumentException("Invalid absolute range spec: " + ctx.getText());
			}
		}
		else if ( ctx.getChildCount() == 2 ) {
			// TIMESTAMP '~'  or  '~' TIMESTAMP
			if ( ctx.getChild(0) instanceof TerminalNode tn
				&& tn.getSymbol().getType() == MdtExprParser.TIMESTAMP ) {
				// TIMESTAMP '~'
				TerminalExpr startExpr = (TerminalExpr)tn.accept(this);
				return new AbsoluteExpr(startExpr, null);
			}
			else if ( ctx.getChild(1) instanceof TerminalNode tn
				&& tn.getSymbol().getType() == MdtExprParser.TIMESTAMP ) {
				// '~' TIMESTAMP
				TerminalExpr endExpr = (TerminalExpr)tn.accept(this);
				return new AbsoluteExpr(null, endExpr);
			}
			else {
				throw new IllegalArgumentException("Invalid absolute range spec: " + ctx.getText());
			}
		}
		else {
			throw new IllegalArgumentException("Invalid absolute range spec: " + ctx.getText());
		}
	}

	@Override
	public SimpleValueExpr<Duration> visitDurationLiteralSpec(DurationLiteralSpecContext ctx) {
		// durationLiteralSpec: 3s, 500ms, 2h ...
		return new SimpleValueExpr<>(UnitUtils.parseDuration(ctx.getText()));
	}
	@Override
	public SimpleValueExpr<Duration> visitIso8601DurationLiteralSpec(Iso8601DurationLiteralSpecContext ctx) {
		// P1D, PT2H30M 등 ISO8601 기간
		return new SimpleValueExpr<>(Duration.parse(ctx.getText()));
	}

	private List<String> toColumns(TsProjectionSpecContext ctx) {
		// tsProjectionSpec: idOrString (',' idOrString)*  → 컬럼명 리스트 (생략 시 호출되지 않음 = 전체)
		List<String> columns = new ArrayList<>();
		for ( int i = 0; i < ctx.getChildCount(); i += 2 ) {	// ',' 는 홀수 인덱스이므로 건너뜀
			columns.add((String)ctx.getChild(i).accept(this).evaluate());
		}
		return columns;
	}
		
//	
//	@Override
//	public MdtParameterValueExpr visitParameterSpec(ParameterSpecContext ctx) {
//		ParseTree instPT = ctx.getChild(0);
//		MDTParameterValueCollectionExpr paramCollExpr = (MDTParameterValueCollectionExpr)instPT.accept(this);
//		
//		ParseTree pt = ctx.getChild(2);
//		if ( pt instanceof TerminalNode tn ) {
//			Token token = tn.getSymbol();
//			if ( token.getType() == MdtExprParser.INTEGER ) {
//				return new MdtParameterValueExpr(paramCollExpr, token.getText());
//			}
//		}
//		String paramId = toString(ctx.getChild(2));
//		
//		Object pl = ctx.getChild(2).getPayload();
//		
//		return new MdtParameterValueExpr(paramCollExpr, paramId);
//	}
//	
//	@Override
//	public MDTSubmodelCollectionExpr visitSubmodelsSpec(MdtExprParser.SubmodelsSpecContext ctx) {
//		ParseTree instPT = ctx.getChild(0);
//		MDTInstanceExpr instExpr = (MDTInstanceExpr)instPT.accept(this);
//		
//		return new MDTSubmodelCollectionExpr(instExpr);
//	}
//	
//
//	@Override
//	public MDTSubmodelExpr visitSubmodelSpec(MdtExprParser.SubmodelSpecContext ctx) {
//		ParseTree instPT = ctx.getChild(0);
//		MDTSubmodelCollectionExpr smCollExpr = (MDTSubmodelCollectionExpr)instPT.accept(this);
//
//		String idShort = toString(ctx.getChild(2));
//		return new MDTSubmodelByIdShortExpr(smCollExpr, idShort);
//	}
//	
//	private String toString(ParseTree ctx) {
//		String str = ctx.getText();
//		return str.substring(1, str.length()-1);
//	}
}