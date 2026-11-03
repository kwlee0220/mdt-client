package mdt.model.sm.simulation;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link SimulationTool}의 기본 구현 엔티티 ({@code SimulationTool} SubmodelElementCollection).
 * <p>
 * 각 필드는 같은 이름의 Property에 대응되며, 제한 시간 필드는 {@code Duration} 타입으로 변환된다.
 * 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulationTool extends SubmodelElementCollectionEntity implements SimulationTool {
	@PropertyField(idShort="SimToolName")
	private String simToolName;
	
	@PropertyField(idShort="OperatingSystem")
	private String operatingSystem;
	
	@PropertyField(idShort="LicenseModel")
	private String licenseModel;
	
	@PropertyField(idShort="SimulatorEndpoint")
	private String simulatorEndpoint;
	
	@PropertyField(idShort="SimulationTimeout", valueType="Duration")
	private Duration simulationTimeout;
	
	@PropertyField(idShort="SessionRetainTimeout", valueType="Duration")
	private Duration sessionRetainTimeout;
	
	/**
	 * idShort가 {@code SimulationTool}인 빈 시뮬레이션 도구 정보 엔티티를 생성한다.
	 */
	public DefaultSimulationTool() {
		setIdShort("SimulationTool");
	}
}