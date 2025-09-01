package mdt.model.expr;

import utils.func.Lazy;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInstanceExpr implements MDTExpression {
	private final MDTInstanceManager m_mdt;
	private final String m_instId;
	private final Lazy<MDTInstance> m_lazyInstance = Lazy.of(this::getInstance);
	
	public MDTInstanceExpr(MDTInstanceManager mdt, String instId) {
		m_mdt = mdt;
		m_instId = instId;
	}
	
	public String getInstanceId() {
		return m_instId;
	}

	@Override
	public MDTInstance evaluate() {
		return m_lazyInstance.get();
	}
	
	private MDTInstance getInstance() {
		return m_mdt.getInstance(m_instId);
	}
}
