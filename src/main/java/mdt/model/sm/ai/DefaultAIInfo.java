package mdt.model.sm.ai;

import mdt.model.DefaultMinResourceRequirements;
import mdt.model.MinResourceRequirements;
import mdt.model.sm.DefaultOperationInfo;
import mdt.model.sm.entity.SMCollectionField;


/**
 * {@link AIInfo}의 기본 구현 엔티티 ({@code AIInfo} SubmodelElementCollection).
 * <p>
 * 입출력 인자 목록 외에 {@code Model}({@link DefaultModel})과
 * {@code MinResourceRequirements} SubmodelElementCollection을 필드로 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultAIInfo extends DefaultOperationInfo implements AIInfo {
	@SMCollectionField(idShort="Model", adaptorClass=DefaultModel.class)
	private Model model;
	
	@SMCollectionField(idShort="MinResourceRequirements", adaptorClass=DefaultMinResourceRequirements.class)
	private MinResourceRequirements minResourceRequirements;
	
	/**
	 * idShort가 {@code AIInfo}인 빈 AI 정보 엔티티를 생성한다.
	 */
	public DefaultAIInfo() {
		super("AIInfo");
	}
	
	/**
	 * AI 모델 정보({@code Model} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	AI 모델 정보.
	 */
	public Model getModel() {
		return model;
	}
	
	/**
	 * AI 모델 정보를 설정한다.
	 *
	 * @param model	AI 모델 정보.
	 */
	public void setModel(Model model) {
		this.model = model;
	}
	
	public MinResourceRequirements getMinResourceRequirements() {
		return minResourceRequirements;
	}
	
	public void setMinResourceRequirements(MinResourceRequirements minResourceRequirements) {
		this.minResourceRequirements = minResourceRequirements;
	}
}