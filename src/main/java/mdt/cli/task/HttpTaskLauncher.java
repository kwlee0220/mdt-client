package mdt.cli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.task.builtin.HttpTaskCommand;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "http",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Http-based task execution command."
)
public class HttpTaskLauncher extends HttpTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskLauncher.class);
	
	public HttpTaskLauncher() {
		super();
		
		setLogger(s_logger);
	}
}
