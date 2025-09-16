package mdt.model.sm.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.AASModelEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Simulation extends AASModelEntity<Submodel> {
	public static final String IDSHORT_PATH_ENDPOINT = "SimulationInfo.SimulationTool.SimulatorEndpoint";
	public static final String IDSHORT_PATH_TIMEOUT = "SimulationInfo.SimulationTool.SimulationTimeout";
	public static final String IDSHORT_PATH_SESSION_TIMEOUT = "SimulationInfo.SimulationTool.SessionRetainTimeout";
	public static final String IDSHORT_PATH_OUTPUTS = "SimulationInfo.Outputs";

	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_SIMULATION;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public SubModelInfo getSubModelInfo();
	public SimulationInfo getSimulationInfo();
	public Operation getSimulationOperation();
}
