package mdt.model.expr;

import java.util.List;

import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;

/**
 * MDTInstance에 포함된 모든 Submodel의 서비스 목록을 나타내는 표현식이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTSubmodelCollectionExpr implements MDTExpression {
	private final MDTInstanceExpr m_instExpr;
	
	/**
	 * 표현식을 생성한다.
	 *
	 * @param instExpr	대상 MDTInstance 표현식.
	 */
	public MDTSubmodelCollectionExpr(MDTInstanceExpr instExpr) {
		m_instExpr = instExpr;
	}

	/**
	 * 대상 MDTInstance의 모든 Submodel 서비스 목록을 반환한다.
	 *
	 * @return	{@link SubmodelService} 리스트.
	 */
	@Override
	public List<? extends SubmodelService> evaluate() {
		MDTInstance instance = m_instExpr.evaluate();
		return instance.getSubmodelServiceAll();
	}
}
