package mdt.model.sm.ai;

import mdt.model.MinResourceRequirements;
import mdt.model.sm.OperationEntity;

/**
 * AI Submodel의 {@code AIInfo} SubmodelElementCollection 인터페이스.
 * <p>
 * 입출력 인자 목록({@link OperationEntity})과 최소 자원 요구사항을 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AIInfo extends OperationEntity {
//	public Model getModel();
//	public void setModel(Model model);
	
	/**
	 * AI 모델 실행에 필요한 최소 자원 요구사항을 반환한다.
	 *
	 * @return	최소 자원 요구사항.
	 */
	public MinResourceRequirements getMinResourceRequirements();
	/**
	 * AI 모델 실행에 필요한 최소 자원 요구사항을 설정한다.
	 *
	 * @param format	최소 자원 요구사항.
	 */
	public void setMinResourceRequirements(MinResourceRequirements format);
}
