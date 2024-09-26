package mdt.cli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.task.builtin.AASOperationTaskCommand;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "aas",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "AAS Operation task execution command."
)
public class AASOperationTaskLauncher extends AASOperationTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTaskLauncher.class);
	
	public AASOperationTaskLauncher() {
		super();
		
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		main(new AASOperationTaskLauncher(), args);
	}
}
