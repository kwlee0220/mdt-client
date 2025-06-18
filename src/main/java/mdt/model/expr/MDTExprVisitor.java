package mdt.model.expr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import mdt.model.expr.LiteralExpr.FileValueSpec;
import mdt.model.expr.LiteralExpr.MLPropertyValueSpec;
import mdt.model.expr.LiteralExpr.PropertyValueSpec;
import mdt.model.expr.LiteralExpr.RangeValueSpec;
import mdt.model.expr.MDTElementReferenceExpr.ArgumentReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.DefaultElementReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.OperationVariableReferenceExpr;
import mdt.model.expr.MDTElementReferenceExpr.ParameterReferenceExpr;
import mdt.model.expr.MDTSubmodelExpr.SubmodelByIdExpr;
import mdt.model.expr.MDTSubmodelExpr.SubmodelByIdShortExpr;
import mdt.model.expr.MdtExprParser.ArgumentSpecContext;
import mdt.model.expr.MdtExprParser.AssignmentExprContext;
import mdt.model.expr.MdtExprParser.DefaultElementSpecContext;
import mdt.model.expr.MdtExprParser.DefaultSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.FileValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.FullElementSpecContext;
import mdt.model.expr.MdtExprParser.FullInstanceSpecContext;
import mdt.model.expr.MdtExprParser.FullSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.IdBasedSubmodelSpecContext;
import mdt.model.expr.MdtExprParser.IdOrStringContext;
import mdt.model.expr.MdtExprParser.IdShortSegContext;
import mdt.model.expr.MdtExprParser.InstanceSpecContext;
import mdt.model.expr.MdtExprParser.MlpPropertyValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.OpVarSpecContext;
import mdt.model.expr.MdtExprParser.ParameterAllSpecContext;
import mdt.model.expr.MdtExprParser.ParameterPathSpecContext;
import mdt.model.expr.MdtExprParser.ParameterSpecContext;
import mdt.model.expr.MdtExprParser.PropertyValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.RangeValueLiteralSpecContext;
import mdt.model.expr.MdtExprParser.SubmodelSpecContext;
import mdt.model.expr.TerminalExpr.DoubleExpr;
import mdt.model.expr.TerminalExpr.IntegerExpr;
import mdt.model.expr.TerminalExpr.StringExpr;
import mdt.model.expr.TerminalExpr.SymbolExpr;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTSubmodelReference;
import mdt.model.sm.ref.OperationVariableReference;
import mdt.model.sm.ref.OperationVariableReference.Kind;
import mdt.model.sm.value.IdShortPath;
import mdt.model.sm.value.PropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTExprVisitor extends MdtExprBaseVisitor<MDTExpr> {
	@Override
	public AssignmentExpr visitAssignmentExpr(AssignmentExprContext ctx) {
		MDTElementReferenceExpr lhs = (MDTElementReferenceExpr)ctx.getChild(0).accept(this);
		MDTExpr rhs = ctx.getChild(2).accept(this);
		
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
	public MDTSubmodelExpr visitFullSubmodelSpec(FullSubmodelSpecContext ctx) {
		return visitSubmodelSpec((SubmodelSpecContext)ctx.getChild(0));
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
		return (ParameterReferenceExpr)ctx.getChild(0).accept(this);
	}
	
	@Override
	public ParameterReferenceExpr visitParameterPathSpec(ParameterPathSpecContext ctx) {
		StringExpr instIdExpr = (StringExpr)ctx.getChild(0).accept(this);
		MDTExpr paramKeyExpr = ctx.getChild(2).accept(this);
		
		MDTIdShortPathExpr subPathExpr = null;
		if ( ctx.getChildCount() > 3 ) {
			subPathExpr = (MDTIdShortPathExpr)ctx.getChild(4).accept(this);
		}
		return new ParameterReferenceExpr(instIdExpr, paramKeyExpr, subPathExpr);
	}
	
	@Override
	public ParameterReferenceExpr visitParameterAllSpec(ParameterAllSpecContext ctx) {
		StringExpr instIdExpr = (StringExpr)ctx.getChild(0).accept(this);
		return new ParameterReferenceExpr(instIdExpr, new SymbolExpr("*"), null);
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
		DefaultElementReferenceExpr smeExpr = (DefaultElementReferenceExpr)ctx.getChild(0).accept(this);
		String kindStr = (String)ctx.getChild(2).accept(this).evaluate();
		Kind kind = OperationVariableReference.Kind.fromString(kindStr);
		IntegerExpr opVarIdxExpr = (IntegerExpr)ctx.getChild(4).accept(this);
		
		return new OperationVariableReferenceExpr(smeExpr, kind, opVarIdxExpr);
	}
	
	@Override
	public MDTIdShortPathExpr visitIdShortPath(MdtExprParser.IdShortPathContext ctx) {
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
		return new MDTIdShortPathExpr(builder.build());
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
		PropertyValue min = (PropertyValue)ctx.getChild(1).accept(this).evaluate();
		PropertyValue max = (PropertyValue)ctx.getChild(3).accept(this).evaluate();
		return new RangeValueSpec(min.get(), max.get());
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
            default:
            	return new SymbolExpr(node.toString());
		}
	}

	@Override
	public StringExpr visitIdOrString(IdOrStringContext ctx) {
		return (StringExpr)ctx.getChild(0).accept(this);
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