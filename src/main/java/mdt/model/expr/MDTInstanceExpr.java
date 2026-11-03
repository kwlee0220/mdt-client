package mdt.model.expr;

import utils.func.Lazy;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;

/**
 * 식별자로 MDTInstance를 지칭하는 표현식이다.
 * <p>
 * 평가 결과인 {@link MDTInstance}는 최초 평가 시 {@link MDTInstanceManager#getInstance(String)}로 한 번만
 * 조회되고, 이후 평가에서는 같은 객체가 반환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceExpr implements MDTExpression {
	private final MDTInstanceManager m_mdt;
	private final String m_instId;
	private final Lazy<MDTInstance> m_lazyInstance = Lazy.of(this::getInstance);
	
	/**
	 * MDTInstance 표현식을 생성한다.
	 *
	 * @param mdt	MDTInstance 조회에 사용할 관리자.
	 * @param instId	MDTInstance 식별자.
	 */
	public MDTInstanceExpr(MDTInstanceManager mdt, String instId) {
		m_mdt = mdt;
		m_instId = instId;
	}
	
	/**
	 * 대상 MDTInstance의 식별자를 반환한다.
	 *
	 * @return	MDTInstance 식별자.
	 */
	public String getInstanceId() {
		return m_instId;
	}

	/**
	 * 대상 MDTInstance를 반환한다.
	 * <p>
	 * 최초 호출 시에만 관리자를 통해 조회한다.
	 *
	 * @return	MDTInstance 객체.
	 * @throws mdt.model.ResourceNotFoundException	식별자에 해당하는 MDTInstance가 없는 경우.
	 */
	@Override
	public MDTInstance evaluate() {
		return m_lazyInstance.get();
	}
	
	private MDTInstance getInstance() {
		return m_mdt.getInstance(m_instId);
	}
}
