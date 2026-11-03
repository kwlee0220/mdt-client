package mdt.model.expr;

/**
 * 생성 시 주어진 값을 그대로 평가 결과로 반환하는 표현식이다.
 * <p>
 * 파싱 과정에서 이미 계산된 값(예: {@link mdt.model.sm.value.IdShortPath}, {@link java.time.Duration})을
 * 표현식 형태로 감싸는 데 사용된다.
 *
 * @param <T>	감싼 값의 타입.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SimpleValueExpr<T> implements MDTExpression {
	private final T m_value;
	
	/**
	 * 주어진 값을 감싸는 표현식을 생성한다.
	 *
	 * @param value	평가 결과로 반환할 값.
	 */
	public SimpleValueExpr(T value) {
		m_value = value;
	}

	@Override
	public T evaluate() {
		return m_value;
	}
}
