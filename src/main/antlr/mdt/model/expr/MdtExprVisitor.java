// Generated from MdtExpr.g4 by ANTLR 4.13.2

package mdt.model.expr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MdtExprParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MdtExprVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(MdtExprParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MdtExprParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#nullExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullExpr(MdtExprParser.NullExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#assignmentExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpr(MdtExprParser.AssignmentExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#fullInstanceSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullInstanceSpec(MdtExprParser.FullInstanceSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#instanceSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceSpec(MdtExprParser.InstanceSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#submodelSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubmodelSpec(MdtExprParser.SubmodelSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#defaultSubmodelSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultSubmodelSpec(MdtExprParser.DefaultSubmodelSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#idBasedSubmodelSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdBasedSubmodelSpec(MdtExprParser.IdBasedSubmodelSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#fullElementSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullElementSpec(MdtExprParser.FullElementSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#defaultElementSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultElementSpec(MdtExprParser.DefaultElementSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#idShortPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdShortPath(MdtExprParser.IdShortPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#idShortSeg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdShortSeg(MdtExprParser.IdShortSegContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#parameterSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterSpec(MdtExprParser.ParameterSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#argumentSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentSpec(MdtExprParser.ArgumentSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#opVarSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpVarSpec(MdtExprParser.OpVarSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#valueLiteralSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueLiteralSpec(MdtExprParser.ValueLiteralSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#propertyValueLiteralSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyValueLiteralSpec(MdtExprParser.PropertyValueLiteralSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#mlpPropertyValueLiteralSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMlpPropertyValueLiteralSpec(MdtExprParser.MlpPropertyValueLiteralSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#fileValueLiteralSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileValueLiteralSpec(MdtExprParser.FileValueLiteralSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#rangeValueLiteralSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeValueLiteralSpec(MdtExprParser.RangeValueLiteralSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdtExprParser#idOrString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdOrString(MdtExprParser.IdOrStringContext ctx);
}