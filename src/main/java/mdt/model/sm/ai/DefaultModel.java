package mdt.model.sm.ai;

import java.time.Duration;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * AI {@link Model}의 기본 구현 엔티티 ({@code Model} SubmodelElementCollection).
 * <p>
 * 각 필드는 같은 이름의 Property에 대응되며, {@code LastExecutionTime}은 {@code Duration} 타입으로 변환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultModel extends SubmodelElementCollectionEntity implements Model {
	@PropertyField(idShort="LearningMethod")
	private String learningMethod;
	
	@PropertyField(idShort="AIModelType")
	private String AIModelType;
	
	@PropertyField(idShort="AIFramework")
	private String AIFramework;
	
	@PropertyField(idShort="ModelDescription")
	private String modelDescription;
	
	@PropertyField(idShort="LastExecutionTime", valueType="Duration")
	private Duration lastExecutionTime;
	
	/**
	 * idShort가 {@code Model}인 빈 AI 모델 정보 엔티티를 생성한다.
	 */
	public DefaultModel() {
		setIdShort("Model");
	}
	
	public String getLearningMethod() {
		return learningMethod;
	}
	
	public void setLearningMethod(String learningMethod) {
		this.learningMethod = learningMethod;
	}
	
	public String getAIModelType() {
		return AIModelType;
	}
	
	public void setAIModelType(String aIModelType) {
		AIModelType = aIModelType;
	}
	
	public String getAIFramework() {
		return AIFramework;
	}
	
	public void setAIFramework(String aIFramework) {
		AIFramework = aIFramework;
	}
	
	public String getModelDescription() {
		return modelDescription;
	}
	
	public void setModelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
	}
	
	public Duration getLastExecutionTime() {
		return lastExecutionTime;
	}
	
	public void setLastExecutionTime(Duration lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
}