package mdt.cli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.task.builtin.CopyTaskCommand;
import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "copy",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "SubmodelElement-value-copy task execution command."
)
public class CopyTaskLauncher extends CopyTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(CopyTaskLauncher.class);
	
	public CopyTaskLauncher() {
		super();
		
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		main(new CopyTaskLauncher(), args);
	}
}
