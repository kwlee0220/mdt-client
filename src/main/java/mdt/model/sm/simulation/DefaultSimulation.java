package mdt.model.sm.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultSubModelInfo;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SMElementField;
import mdt.model.sm.entity.SubmodelEntity;

/**
 * {@link Simulation} Submodel의 기본 구현 엔티티.
 * <p>
 * {@code SubModelInfo}, {@code SimulationInfo}({@link DefaultSimulationInfo}) SubmodelElementCollection과
 * {@code SimulationOperation} Operation을 필드로 갖는다. 접근자는 Lombok {@code @Getter}/{@code @Setter}로
 * 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSimulation extends SubmodelEntity implements Simulation {
	@SMCollectionField(idShort="SubModelInfo", adaptorClass=DefaultSubModelInfo.class)
	private SubModelInfo subModelInfo;
	
	@SMCollectionField(idShort="SimulationInfo")
	private DefaultSimulationInfo simulationInfo;
	
	@SMElementField(idShort="SimulationOperation")
	private Operation simulationOperation;
	
	/**
	 * 주어진 Simulation Submodel로부터 {@link DefaultSimulation} 객체를 생성한다.
	 *
	 * @param submodel	Simulation Submodel.
	 * @return	생성된 {@link DefaultSimulation} 객체.
	 */
	public static DefaultSimulation from(Submodel submodel) {
		DefaultSimulation simulation = new DefaultSimulation();
		simulation.updateFromAasModel(submodel);
		
		return simulation;
	}

	/**
	 * semanticId가 {@link Simulation#SEMANTIC_ID_REFERENCE}로 설정된 빈 Simulation Submodel 엔티티를 생성한다.
	 */
	public DefaultSimulation() {
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), subModelInfo.getTitle());
	}
}
