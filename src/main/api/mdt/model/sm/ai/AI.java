package mdt.model.sm.ai;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.AASModelEntity;


/**
 * AI 모델을 기술하는 AI Submodel 인터페이스.
 * <p>
 * Submodel은 {@code SubModelInfo}와 {@code AIInfo} SubmodelElementCollection으로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AI extends AASModelEntity<Submodel> {
	/**
	 * AI Submodel의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_AI;
	/**
	 * AI Submodel의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * Submodel 정보({@code SubModelInfo} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	Submodel 정보.
	 */
	public SubModelInfo getSubModelInfo();
	/**
	 * AI 정보({@code AIInfo} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	AI 정보.
	 */
	public AIInfo getAIInfo();
}
