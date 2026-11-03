package mdt.model.sm.ai;

import java.time.Duration;

/**
 * AI Submodel의 {@code AIInfo.Model} SubmodelElementCollection 인터페이스.
 * <p>
 * AI 모델의 학습 방법, 모델 타입, 프레임워크, 설명, 최근 실행 소요 시간을 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Model {
	/**
	 * 학습 방법을 반환한다.
	 *
	 * @return	학습 방법.
	 */
	public String getLearningMethod();
	/**
	 * 학습 방법을 설정한다.
	 *
	 * @param method	학습 방법.
	 */
	public void setLearningMethod(String method);
	
	/**
	 * AI 모델 타입을 반환한다.
	 *
	 * @return	AI 모델 타입.
	 */
	public String getAIModelType();
	/**
	 * AI 모델 타입을 설정한다.
	 *
	 * @param type	AI 모델 타입.
	 */
	public void setAIModelType(String type);
	
	/**
	 * AI 프레임워크를 반환한다.
	 *
	 * @return	AI 프레임워크.
	 */
	public String getAIFramework();
	/**
	 * AI 프레임워크를 설정한다.
	 *
	 * @param framework	AI 프레임워크.
	 */
	public void setAIFramework(String framework);
	
	/**
	 * 모델 설명을 반환한다.
	 *
	 * @return	모델 설명.
	 */
	public String getModelDescription();
	/**
	 * 모델 설명을 설정한다.
	 *
	 * @param desc	모델 설명.
	 */
	public void setModelDescription(String desc);
	
	/**
	 * 최근 실행 소요 시간을 반환한다.
	 *
	 * @return	최근 실행 소요 시간.
	 */
	public Duration getLastExecutionTime();
	/**
	 * 최근 실행 소요 시간을 설정한다.
	 *
	 * @param version	최근 실행 소요 시간.
	 */
	public void setLastExecutionTime(Duration version);
}
