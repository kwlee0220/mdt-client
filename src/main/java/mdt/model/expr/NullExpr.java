package mdt.model.expr;

/**
 * {@code null} 리터럴 표현식으로, 평가 결과는 항상 {@code null}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class NullExpr implements MDTExpression {
	@Override
	public Object evaluate() {
		return null;
	}
}
