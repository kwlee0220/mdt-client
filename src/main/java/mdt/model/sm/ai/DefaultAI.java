package mdt.model.sm.ai;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.DefaultSubModelInfo;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 * {@link AI} Submodel의 기본 구현 엔티티.
 * <p>
 * {@code SubModelInfo}({@link DefaultSubModelInfo})와 {@code AIInfo}({@link DefaultAIInfo})
 * SubmodelElementCollection을 필드로 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultAI extends SubmodelEntity implements AI {
	@SMCollectionField(idShort="SubModelInfo", adaptorClass=DefaultSubModelInfo.class)
	private SubModelInfo subModelInfo;
	
	@SMCollectionField(idShort="AIInfo")
	private DefaultAIInfo AIInfo;
	
	/**
	 * 주어진 AI Submodel로부터 {@link DefaultAI} 객체를 생성한다.
	 *
	 * @param submodel	AI Submodel.
	 * @return	생성된 {@link DefaultAI} 객체.
	 */
	public static DefaultAI from(Submodel submodel) {
		DefaultAI AI = new DefaultAI();
		AI.updateFromAasModel(submodel);
		
		return AI;
	}

	/**
	 * semanticId가 {@link AI#SEMANTIC_ID_REFERENCE}로 설정된 빈 AI Submodel 엔티티를 생성한다.
	 */
	public DefaultAI() {
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
	public SubModelInfo getSubModelInfo() {
		return this.subModelInfo;
	}
	
	/**
	 * Submodel 정보를 설정한다.
	 *
	 * @param subModelInfo	Submodel 정보.
	 */
	public void setSubModelInfo(SubModelInfo subModelInfo) {
		this.subModelInfo = subModelInfo;
	}
	
	public DefaultAIInfo getAIInfo() {
		return this.AIInfo;
	}
	
	/**
	 * AI 정보를 설정한다.
	 *
	 * @param AIInfo	AI 정보.
	 */
	public void setAIInfo(DefaultAIInfo AIInfo) {
		this.AIInfo = AIInfo;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), subModelInfo.getTitle());
	}
}
