package mdt.model.sm.simulation;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultMinResourceRequirements;
import mdt.model.MinResourceRequirements;
import mdt.model.sm.DefaultOperationInfo;
import mdt.model.sm.entity.SMCollectionField;


/**
 * {@link SimulationInfo}의 기본 구현 엔티티 ({@code SimulationInfo} SubmodelElementCollection).
 * <p>
 * 입출력 인자 목록 외에 {@code Model}, {@code SimulationTool}, {@code MinResourceRequirements}
 * SubmodelElementCollection을 필드로 갖는다. 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulationInfo extends DefaultOperationInfo implements SimulationInfo {
	@SMCollectionField(idShort="Model", adaptorClass=DefaultModel.class)
	private Model model;
	
	@SMCollectionField(idShort="SimulationTool", adaptorClass=DefaultSimulationTool.class)
	private SimulationTool simulationTool;
	
	@SMCollectionField(idShort="MinResourceRequirements", adaptorClass=DefaultMinResourceRequirements.class)
	private MinResourceRequirements minResourceRequirements;
	
	/**
	 * idShort가 {@code SimulationInfo}인 빈 시뮬레이션 정보 엔티티를 생성한다.
	 */
	public DefaultSimulationInfo() {
		super("SimulationInfo");
	}
}
