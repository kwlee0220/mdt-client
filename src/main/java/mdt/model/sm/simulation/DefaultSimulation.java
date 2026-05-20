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
	
	public static DefaultSimulation from(Submodel submodel) {
		DefaultSimulation simulation = new DefaultSimulation();
		simulation.updateFromAasModel(submodel);
		
		return simulation;
	}

	public DefaultSimulation() {
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), subModelInfo.getTitle());
	}
}
