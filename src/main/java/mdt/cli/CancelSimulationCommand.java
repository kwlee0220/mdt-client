package mdt.cli;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.MDTClientConfig;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.client.operation.HttpSimulationClient;
import mdt.client.operation.OperationStatusResponse;
import mdt.ksx9101.simulation.Simulation;
import mdt.model.SubmodelUtils;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.service.SubmodelService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(name = "cancel", description = "Cancel a simulation.")
public class CancelSimulationCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(CancelSimulationCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="target Simulation Submodel (or MDTInstance) id")
	private String m_targetId;
	
	@Parameters(index="1", paramLabel="opId", description="target Simulation operation id")
	private String m_opId;

	public CancelSimulationCommand() {
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		CancelSimulationCommand cmd = new CancelSimulationCommand();

		CommandLine commandLine = new CommandLine(cmd).setUsageHelpWidth(100);
		try {
			commandLine.parse(args);

			if ( commandLine.isUsageHelpRequested() ) {
				commandLine.usage(System.out, Ansi.OFF);
			}
			else {
				cmd.run();
			}
		}
		catch ( Throwable e ) {
			System.err.println(e);
			commandLine.usage(System.out, Ansi.OFF);
		}
	}
		
	@Override
	public void run(MDTClientConfig configs) throws Exception {
		HttpMDTInstanceManagerClient mdtClient = this.createMDTInstanceManager(configs);
		
		SubmodelService svc;
		try {
			HttpMDTInstanceClient inst = (HttpMDTInstanceClient)mdtClient.getInstanceBySubmodelId(m_targetId);
			svc = inst.getSubmodelServiceById(m_targetId);
		}
		catch ( ResourceNotFoundException expected ) {
			HttpMDTInstanceClient inst = mdtClient.getInstance(m_targetId);
			svc = inst.getSubmodelServiceByIdShort("Simulation");
		}
		
		Submodel simulation = svc.getSubmodel();
		String endpoint = SubmodelUtils.getPropertyValueByPath(simulation,
																Simulation.IDSHORT_PATH_ENDPOINT,
																String.class);
		
		HttpSimulationClient client = new HttpSimulationClient(mdtClient.getHttpClient(), endpoint);
		
		OperationStatusResponse<Void> resp = client.cancelSimulation(m_opId);
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
