// Generated from /home/kwlee/development/mdt/mdt-client/src/main/antlr/MdtExpr.g4 by ANTLR 4.13.1

package mdt.model.expr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MdtExprParser}.
 */
public interface MdtExprListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(MdtExprParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(MdtExprParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(MdtExprParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(MdtExprParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#nullExpr}.
	 * @param ctx the parse tree
	 */
	void enterNullExpr(MdtExprParser.NullExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#nullExpr}.
	 * @param ctx the parse tree
	 */
	void exitNullExpr(MdtExprParser.NullExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#assignmentExpr}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpr(MdtExprParser.AssignmentExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#assignmentExpr}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpr(MdtExprParser.AssignmentExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#fullInstanceSpec}.
	 * @param ctx the parse tree
	 */
	void enterFullInstanceSpec(MdtExprParser.FullInstanceSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#fullInstanceSpec}.
	 * @param ctx the parse tree
	 */
	void exitFullInstanceSpec(MdtExprParser.FullInstanceSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#instanceSpec}.
	 * @param ctx the parse tree
	 */
	void enterInstanceSpec(MdtExprParser.InstanceSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#instanceSpec}.
	 * @param ctx the parse tree
	 */
	void exitInstanceSpec(MdtExprParser.InstanceSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#submodelSpec}.
	 * @param ctx the parse tree
	 */
	void enterSubmodelSpec(MdtExprParser.SubmodelSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#submodelSpec}.
	 * @param ctx the parse tree
	 */
	void exitSubmodelSpec(MdtExprParser.SubmodelSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#defaultSubmodelSpec}.
	 * @param ctx the parse tree
	 */
	void enterDefaultSubmodelSpec(MdtExprParser.DefaultSubmodelSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#defaultSubmodelSpec}.
	 * @param ctx the parse tree
	 */
	void exitDefaultSubmodelSpec(MdtExprParser.DefaultSubmodelSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#idBasedSubmodelSpec}.
	 * @param ctx the parse tree
	 */
	void enterIdBasedSubmodelSpec(MdtExprParser.IdBasedSubmodelSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#idBasedSubmodelSpec}.
	 * @param ctx the parse tree
	 */
	void exitIdBasedSubmodelSpec(MdtExprParser.IdBasedSubmodelSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#fullElementSpec}.
	 * @param ctx the parse tree
	 */
	void enterFullElementSpec(MdtExprParser.FullElementSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#fullElementSpec}.
	 * @param ctx the parse tree
	 */
	void exitFullElementSpec(MdtExprParser.FullElementSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#defaultElementSpec}.
	 * @param ctx the parse tree
	 */
	void enterDefaultElementSpec(MdtExprParser.DefaultElementSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#defaultElementSpec}.
	 * @param ctx the parse tree
	 */
	void exitDefaultElementSpec(MdtExprParser.DefaultElementSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#idShortPath}.
	 * @param ctx the parse tree
	 */
	void enterIdShortPath(MdtExprParser.IdShortPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#idShortPath}.
	 * @param ctx the parse tree
	 */
	void exitIdShortPath(MdtExprParser.IdShortPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#idShortSeg}.
	 * @param ctx the parse tree
	 */
	void enterIdShortSeg(MdtExprParser.IdShortSegContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#idShortSeg}.
	 * @param ctx the parse tree
	 */
	void exitIdShortSeg(MdtExprParser.IdShortSegContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#parameterSpec}.
	 * @param ctx the parse tree
	 */
	void enterParameterSpec(MdtExprParser.ParameterSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#parameterSpec}.
	 * @param ctx the parse tree
	 */
	void exitParameterSpec(MdtExprParser.ParameterSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#argumentSpec}.
	 * @param ctx the parse tree
	 */
	void enterArgumentSpec(MdtExprParser.ArgumentSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#argumentSpec}.
	 * @param ctx the parse tree
	 */
	void exitArgumentSpec(MdtExprParser.ArgumentSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#opVarSpec}.
	 * @param ctx the parse tree
	 */
	void enterOpVarSpec(MdtExprParser.OpVarSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#opVarSpec}.
	 * @param ctx the parse tree
	 */
	void exitOpVarSpec(MdtExprParser.OpVarSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#valueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void enterValueLiteralSpec(MdtExprParser.ValueLiteralSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#valueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void exitValueLiteralSpec(MdtExprParser.ValueLiteralSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#propertyValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void enterPropertyValueLiteralSpec(MdtExprParser.PropertyValueLiteralSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#propertyValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void exitPropertyValueLiteralSpec(MdtExprParser.PropertyValueLiteralSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#mlpPropertyValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void enterMlpPropertyValueLiteralSpec(MdtExprParser.MlpPropertyValueLiteralSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#mlpPropertyValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void exitMlpPropertyValueLiteralSpec(MdtExprParser.MlpPropertyValueLiteralSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#fileValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void enterFileValueLiteralSpec(MdtExprParser.FileValueLiteralSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#fileValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void exitFileValueLiteralSpec(MdtExprParser.FileValueLiteralSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#rangeValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void enterRangeValueLiteralSpec(MdtExprParser.RangeValueLiteralSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#rangeValueLiteralSpec}.
	 * @param ctx the parse tree
	 */
	void exitRangeValueLiteralSpec(MdtExprParser.RangeValueLiteralSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link MdtExprParser#idOrString}.
	 * @param ctx the parse tree
	 */
	void enterIdOrString(MdtExprParser.IdOrStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link MdtExprParser#idOrString}.
	 * @param ctx the parse tree
	 */
	void exitIdOrString(MdtExprParser.IdOrStringContext ctx);
}