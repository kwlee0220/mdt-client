package mdt.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.HttpMDTManagerClient;
import mdt.model.MDTManager;
import picocli.CommandLine.Command;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "shutdown",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Stop the running MDTInstanceManager."
)
public class ShutdownManagerCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ShutdownManagerCommand.class);

	public static final void main(String... args) throws Exception {
		main(new ShutdownManagerCommand(), args);
	}

	public ShutdownManagerCommand() {
		setLogger(s_logger);
	}
		
	@Override
	public void run(MDTManager manager) throws Exception {
		((HttpMDTManagerClient)manager).shutdown();
		
		System.exit(0);
	}
}
