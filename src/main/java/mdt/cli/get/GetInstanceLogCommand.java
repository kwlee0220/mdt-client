package mdt.cli.get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "log",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get instance log data."
)
public class GetInstanceLogCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceLogCommand.class);

	@ParentCommand GetMDTInstanceCommand m_parent;
	
	public GetInstanceLogCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		HttpMDTInstanceClient instance = manager.getInstance(m_parent.getInstanceId());

		System.out.print(instance.getOutputLog());
	}

	public static final void main(String... args) throws Exception {
		main(new GetInstanceLogCommand(), args);
	}
}
