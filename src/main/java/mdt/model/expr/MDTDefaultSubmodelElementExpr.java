package mdt.model.expr;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.instance.MDTInstance;
import mdt.model.sm.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTDefaultSubmodelElementExpr implements MDTExpr {
	private final MDTInstanceExpr m_instanceExpr;
	private final String m_submodelIdShort;
	private final String m_idShortPath;
	
	public MDTDefaultSubmodelElementExpr(MDTInstanceExpr instanceExpr, String submodelIdShort, String idShortPath) {
		m_instanceExpr = instanceExpr;
		m_submodelIdShort = submodelIdShort;
		m_idShortPath = idShortPath;
	}

	@Override
	public SubmodelElement evaluate() {
		MDTInstance instance = m_instanceExpr.evaluate();
		Submodel submodel = instance.getSubmodelServiceByIdShort(m_submodelIdShort).getSubmodel();
		return SubmodelUtils.traverse(submodel, m_idShortPath);
	}
}
