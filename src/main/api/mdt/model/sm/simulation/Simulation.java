package mdt.model.sm.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.AASModelEntity;


/**
 * 시뮬레이션 모델과 시뮬레이터를 기술하는 Simulation Submodel 인터페이스.
 * <p>
 * Submodel은 {@code SubModelInfo}, {@code SimulationInfo} SubmodelElementCollection과
 * {@code SimulationOperation} Operation으로 구성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Simulation extends AASModelEntity<Submodel> {
	/**
	 * 시뮬레이터 endpoint Property의 idShort 경로.
	 */
	public static final String IDSHORT_PATH_ENDPOINT = "SimulationInfo.SimulationTool.SimulatorEndpoint";
	/**
	 * 시뮬레이션 제한 시간 Property의 idShort 경로.
	 */
	public static final String IDSHORT_PATH_TIMEOUT = "SimulationInfo.SimulationTool.SimulationTimeout";
	/**
	 * 시뮬레이션 세션 유지 시간 Property의 idShort 경로.
	 */
	public static final String IDSHORT_PATH_SESSION_TIMEOUT = "SimulationInfo.SimulationTool.SessionRetainTimeout";
	/**
	 * 시뮬레이션 출력 인자 목록 SubmodelElementList의 idShort 경로.
	 */
	public static final String IDSHORT_PATH_OUTPUTS = "SimulationInfo.Outputs";

	/**
	 * Simulation Submodel의 semanticId 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_SIMULATION;
	/**
	 * Simulation Submodel의 semanticId 참조 ({@link #SEMANTIC_ID}로부터 생성).
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * Submodel 정보({@code SubModelInfo} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	Submodel 정보.
	 */
	public SubModelInfo getSubModelInfo();
	/**
	 * 시뮬레이션 정보({@code SimulationInfo} SubmodelElementCollection)를 반환한다.
	 *
	 * @return	시뮬레이션 정보.
	 */
	public SimulationInfo getSimulationInfo();
	/**
	 * 시뮬레이션을 수행하는 AAS {@code Operation} 요소({@code SimulationOperation})를 반환한다.
	 *
	 * @return	시뮬레이션 Operation.
	 */
	public Operation getSimulationOperation();
}
