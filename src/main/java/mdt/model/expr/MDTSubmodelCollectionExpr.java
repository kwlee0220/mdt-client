package mdt.model.expr;

import java.util.List;

import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTSubmodelCollectionExpr implements MDTExpr {
	private final MDTInstanceExpr m_instExpr;
	
	public MDTSubmodelCollectionExpr(MDTInstanceExpr instExpr) {
		m_instExpr = instExpr;
	}

	@Override
	public List<SubmodelService> evaluate() {
		MDTInstance instance = m_instExpr.evaluate();
		return instance.getSubmodelServiceAll();
	}
}
