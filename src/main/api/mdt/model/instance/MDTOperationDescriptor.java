package mdt.model.instance;

import java.util.List;

import utils.stream.FStream;

import mdt.model.sm.value.NamedValueType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTOperationDescriptor {
	/**
	 * MDT 연산의 이름을 반환한다.
	 * 
	 * @return	파라미터 이름
	 */
	public String getName();
	
	/**
	 * MDT 연산의 타입을 반환한다.
	 *
	 * @return MDT 연산 타입
	 */
	public String getOperationType();
	
	/**
	 * MDT 연산의 입력 인자 목록을 반환한다.
	 *
	 * @return	입력 파라미터 목록
	 */
	public List<NamedValueType> getInputArguments();
	
	/**
	 * MDT 연산의 출력 인자 목록을 반환한다.
	 *
	 * @return	출력 인자 목록
	 */
	public List<NamedValueType> getOutputArguments();
	
	public default String toSignatureString() {
		String inArgsStr = FStream.from(getInputArguments())
									.map(NamedValueType::getName)
									.join(", ");
		String outArgsStr = FStream.from(getOutputArguments())
									.map(NamedValueType::getName)
									.join(", ");
		return String.format("%s(%s) -> (%s)", getName(), inArgsStr, outArgsStr);
	}
}
