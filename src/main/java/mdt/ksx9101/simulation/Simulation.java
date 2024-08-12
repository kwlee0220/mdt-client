package mdt.ksx9101.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import mdt.ksx9101.model.SubModelInfo;
import mdt.model.MDTAASModel;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Simulation extends MDTAASModel<Submodel> {
	public static final String IDSHORT_PATH_ENDPOINT = "SimulationInfo.SimulationTool.SimulatorEndpoint";
	public static final String IDSHORT_PATH_TIMEOUT = "SimulationInfo.SimulationTool.SimulationTimeout";
	public static final String IDSHORT_PATH_SESSION_TIMEOUT = "SimulationInfo.SimulationTool.SessionRetainTimeout";
	public static final String IDSHORT_PATH_OUTPUTS = "SimulationInfo.Outputs";
	
	public static final Reference SEMANTIC_ID
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value("https://etri.re.kr/mdt/Submodel/Simulation/1/1")
									.build())
				.build();
	
	public SubModelInfo getSubModelInfo();
	public SimulationInfo getSimulationInfo();
	public Operation getSimulationOperation();
}
