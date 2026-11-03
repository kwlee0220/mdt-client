package mdt.model.expr;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.sm.SubmodelUtils;

/**
 * MDTInstance, Submodel idShort, idShortPath로 지정된 SubmodelElement를 직접 읽어오는 표현식이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTDefaultSubmodelElementExpr implements MDTExpression {
	private final MDTInstanceExpr m_instanceExpr;
	private final String m_submodelIdShort;
	private final String m_idShortPath;
	
	/**
	 * 표현식을 생성한다.
	 *
	 * @param instanceExpr	대상 MDTInstance 표현식.
	 * @param submodelIdShort	대상 Submodel의 idShort.
	 * @param idShortPath	Submodel 내 SubmodelElement의 idShortPath.
	 */
	public MDTDefaultSubmodelElementExpr(MDTInstanceExpr instanceExpr, String submodelIdShort, String idShortPath) {
		m_instanceExpr = instanceExpr;
		m_submodelIdShort = submodelIdShort;
		m_idShortPath = idShortPath;
	}

	/**
	 * 대상 Submodel 전체를 조회한 뒤 idShortPath에 해당하는 SubmodelElement를 찾아 반환한다.
	 *
	 * @return	SubmodelElement 객체.
	 * @throws ResourceNotFoundException	주어진 idShort의 Submodel이 없는 경우.
	 */
	@Override
	public SubmodelElement evaluate() {
		MDTInstance instance = m_instanceExpr.evaluate();
		Submodel submodel = instance.getSubmodelServiceByIdShort(m_submodelIdShort)
                .map(SubmodelService::getSubmodel)
                .getOrThrow(() -> ResourceNotFoundException.ofIdShort("Submodel", m_submodelIdShort));

		return SubmodelUtils.traverse(submodel, m_idShortPath);
	}
}
