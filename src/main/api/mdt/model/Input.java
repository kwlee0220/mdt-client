package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

/**
 * AI/Simulation 서브모델의 연산(operation) 정보에 포함되는 입력 인자를 표현하는 인터페이스이다.
 * <p>
 * 하나의 입력 인자는 식별자(InputID), 이름(InputName), 값(InputValue), 타입(InputType)으로 구성되며,
 * SubmodelElementCollection 형태로 저장된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Input {
	/** 입력 인자 SubmodelElementCollection의 semantic id ({@link MDTSemanticIds#ARGUMENT_INPUT}). */
	public static final String SEMANTIC_ID = MDTSemanticIds.ARGUMENT_INPUT;
	/** {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}. */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 입력 인자의 식별자를 반환한다.
	 *
	 * @return	입력 인자 식별자.
	 */
	public String getInputID();
	/**
	 * 입력 인자의 식별자를 설정한다.
	 *
	 * @param inputID	입력 인자 식별자.
	 */
	public void setInputID(String inputID);

	/**
	 * 입력 인자의 이름을 반환한다.
	 *
	 * @return	입력 인자 이름.
	 */
	public String getInputName();
	/**
	 * 입력 인자의 이름을 설정한다.
	 *
	 * @param inputName	입력 인자 이름.
	 */
	public void setInputName(String inputName);

	/**
	 * 입력 인자의 값을 반환한다.
	 *
	 * @return	입력 인자 값을 담은 {@link SubmodelElement}.
	 */
	public SubmodelElement getInputValue();
	/**
	 * 입력 인자의 값을 설정한다.
	 *
	 * @param inputValue	입력 인자 값을 담은 {@link SubmodelElement}.
	 */
	public void setInputValue(SubmodelElement inputValue);

	/**
	 * 입력 인자의 타입 이름을 반환한다.
	 *
	 * @return	입력 인자 타입.
	 */
	public String getInputType();
	/**
	 * 입력 인자의 타입 이름을 설정한다.
	 *
	 * @param inputType	입력 인자 타입.
	 */
	public void setInputType(String inputType);
}
