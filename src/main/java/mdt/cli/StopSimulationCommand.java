package mdt.cli;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.operation.HttpSimulationClient;
import mdt.client.operation.OperationStatusResponse;
import mdt.model.MDTManager;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.simulation.Simulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "simulation",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Stop a simulation."
)
public class StopSimulationCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(StopSimulationCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="target Simulation Submodel (or MDTInstance) id")
	private String m_targetId;
	
	@Parameters(index="1", paramLabel="opId", description="target Simulation operation id")
	private String m_opId;

	public static final void main(String... args) throws Exception {
		main(new StopSimulationCommand(), args);
	}

	public StopSimulationCommand() {
		setLogger(s_logger);
	}
		
	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
		SubmodelService svc;
		try {
			HttpMDTInstanceClient inst = (HttpMDTInstanceClient)client.getInstanceBySubmodelId(m_targetId);
			svc = inst.getSubmodelServiceById(m_targetId);
		}
		catch ( ResourceNotFoundException expected ) {
			HttpMDTInstanceClient inst = client.getInstance(m_targetId);
			svc = inst.getSubmodelServiceByIdShort("Simulation");
		}
		
		Submodel simModel = svc.getSubmodel();
		String endpoint = SubmodelUtils.getPropertyValueByPath(simModel,
																Simulation.IDSHORT_PATH_ENDPOINT,
																String.class);
		
		HttpSimulationClient simulation = new HttpSimulationClient(client.getHttpClient(), endpoint);
		
		OperationStatusResponse<Void> resp = simulation.cancelSimulation(m_opId);
		switch ( resp.getStatus() ) {
			case COMPLETED:
				System.out.println("Simulation completes");
				System.exit(0);
			case FAILED:
				System.out.printf("Simulation is failed: cause=%s%n", resp.getMessage());
				System.exit(-1);
			case CANCELLED:
				System.out.println("Simulation is cancelled");
				System.exit(-1);
			default:
				throw new AssertionError();
		}
	}
}
