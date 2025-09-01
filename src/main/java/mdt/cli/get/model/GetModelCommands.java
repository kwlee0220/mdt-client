package mdt.cli.get.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "model",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get MDT model information for the instance.",
	subcommands = {
		GetModelInstanceCommand.class,
		GetModelSubmodelCommand.class,
		GetModelParametersCommand.class,
		GetModelOperationsCommand.class,
		GetModelCompositionsCommand.class,
	}
)
public class GetModelCommands extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetModelCommands.class);
	
	@Parameters(index="0", paramLabel="id", description="MDTInstance id to show.")
	private String m_instanceId;

	public static final void main(String... args) throws Exception {
		main(new GetModelCommands(), args);
	}
	
	public GetModelCommands() {
		setLogger(s_logger);
	}
	
	String getInstanceId() {
		return m_instanceId;
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		HttpMDTInstanceClient instance = manager.getInstance(m_instanceId);
	}
	
	HttpMDTInstanceClient getInstance(MDTManager mdt) {
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		return manager.getInstance(m_instanceId);
	}
}
