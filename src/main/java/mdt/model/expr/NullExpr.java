package mdt.model.expr;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class NullExpr implements MDTExpression {
	@Override
	public Object evaluate() {
		return null;
	}
}
