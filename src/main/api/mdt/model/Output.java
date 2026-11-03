package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

/**
 * AI/Simulation 서브모델의 연산(operation) 정보에 포함되는 출력 인자를 표현하는 인터페이스이다.
 * <p>
 * 하나의 출력 인자는 식별자(OutputID), 이름(OutputName), 값(OutputValue), 타입(OutputType)으로 구성되며,
 * SubmodelElementCollection 형태로 저장된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Output {
	/** 출력 인자 SubmodelElementCollection의 semantic id ({@link MDTSemanticIds#ARGUMENT_OUTPUT}). */
	public static final String SEMANTIC_ID = MDTSemanticIds.ARGUMENT_OUTPUT;
	/** {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}. */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 출력 인자의 식별자를 반환한다.
	 *
	 * @return	출력 인자 식별자.
	 */
	public String getOutputID();
	/**
	 * 출력 인자의 식별자를 설정한다.
	 *
	 * @param outputID	출력 인자 식별자.
	 */
	public void setOutputID(String outputID);

	/**
	 * 출력 인자의 이름을 반환한다.
	 *
	 * @return	출력 인자 이름.
	 */
	public String getOutputName();
	/**
	 * 출력 인자의 이름을 설정한다.
	 *
	 * @param outputName	출력 인자 이름.
	 */
	public void setOutputName(String outputName);

	/**
	 * 출력 인자의 값을 반환한다.
	 *
	 * @return	출력 인자 값을 담은 {@link SubmodelElement}.
	 */
	public SubmodelElement getOutputValue();
	/**
	 * 출력 인자의 값을 설정한다.
	 *
	 * @param outputValue	출력 인자 값을 담은 {@link SubmodelElement}.
	 */
	public void setOutputValue(SubmodelElement outputValue);

	/**
	 * 출력 인자의 타입 이름을 반환한다.
	 *
	 * @return	출력 인자 타입.
	 */
	public String getOutputType();
	/**
	 * 출력 인자의 타입 이름을 설정한다.
	 *
	 * @param outputType	출력 인자 타입.
	 */
	public void setOutputType(String outputType);
}
