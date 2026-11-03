package mdt.model.sm;

import java.util.List;

import mdt.model.Input;
import mdt.model.Output;

/**
 * 입력 인자({@link Input})와 출력 인자({@link Output}) 목록을 갖는 연산 정보 엔티티 인터페이스.
 * <p>
 * AI, Simulation 등 연산형 Submodel의 정보 SubmodelElementCollection
 * ({@code AIInfo}, {@code SimulationInfo} 등)이 이 인터페이스를 확장한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface OperationEntity {
	/**
	 * 입력 인자 목록을 반환한다.
	 *
	 * @return	입력 인자 목록.
	 */
	public List<Input> getInputs();

	/**
	 * 입력 인자 목록을 설정한다.
	 *
	 * @param inputs	입력 인자 목록.
	 */
	public void setInputs(List<Input> inputs);
	
	/**
	 * 출력 인자 목록을 반환한다.
	 *
	 * @return	출력 인자 목록.
	 */
	public List<Output> getOutputs();

	/**
	 * 출력 인자 목록을 설정한다.
	 *
	 * @param outputs	출력 인자 목록.
	 */
	public void setOutputs(List<Output> outputs);
}
