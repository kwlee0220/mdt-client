package mdt.model.sm.simulation;

import mdt.model.MinResourceRequirements;
import mdt.model.sm.OperationEntity;

/**
 * Simulation Submodel의 {@code SimulationInfo} SubmodelElementCollection 인터페이스.
 * <p>
 * 입출력 인자 목록({@link OperationEntity}), 시뮬레이션 모델, 시뮬레이션 도구, 최소 자원 요구사항을 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationInfo extends OperationEntity {
	/**
	 * 시뮬레이션 모델 정보를 반환한다.
	 *
	 * @return	시뮬레이션 모델 정보.
	 */
	public Model getModel();
	/**
	 * 시뮬레이션 모델 정보를 설정한다.
	 *
	 * @param model	시뮬레이션 모델 정보.
	 */
	public void setModel(Model model);
	
	/**
	 * 시뮬레이션 실행에 필요한 최소 자원 요구사항을 반환한다.
	 *
	 * @return	시뮬레이션 실행에 필요한 최소 자원 요구사항.
	 */
	public MinResourceRequirements getMinResourceRequirements();
	/**
	 * 시뮬레이션 실행에 필요한 최소 자원 요구사항을 설정한다.
	 *
	 * @param format	시뮬레이션 실행에 필요한 최소 자원 요구사항.
	 */
	public void setMinResourceRequirements(MinResourceRequirements format);

	/**
	 * 시뮬레이션 도구 정보를 반환한다.
	 *
	 * @return	시뮬레이션 도구 정보.
	 */
	public SimulationTool getSimulationTool();
	/**
	 * 시뮬레이션 도구 정보를 설정한다.
	 *
	 * @param id	시뮬레이션 도구 정보.
	 */
	public void setSimulationTool(SimulationTool id);
}
