package mdt.model.expr;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SimpleValueExpr<T> implements MDTExpression {
	private final T m_value;
	
	public SimpleValueExpr(T value) {
		m_value = value;
	}

	@Override
	public T evaluate() {
		return m_value;
	}
}
