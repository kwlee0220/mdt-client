package mdt.cli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.task.builtin.JsltTaskCommand;

import picocli.CommandLine.Command;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "jslt",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "JSLT2-based task execution command."
)
public class JsltTaskLauncher extends JsltTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(JsltTaskLauncher.class);
	
	public JsltTaskLauncher() {
		super();
		
		setLogger(s_logger);
	}

	public static final void main(String... args) throws Exception {
		main(new JsltTaskLauncher(), args);
	}
}
