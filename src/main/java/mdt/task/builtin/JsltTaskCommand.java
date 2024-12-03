package mdt.task.builtin;

import java.io.File;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "jslt", description = "transform Port's data")
public class JsltTaskCommand extends AbstractMDTCommand {
	@ArgGroup(exclusive=true, multiplicity="0..1")
	private ScriptSpec m_script;
	static class ScriptSpec {
		@Option(names={"--expr"}, paramLabel="script", required=true, description="JSTL2 script")
		private String m_expr;

		@Option(names={"--file"}, paramLabel="script-file", required=true, description="Jstl2 script file path")
		private File m_file;
	}

	@Override
	protected void run(MDTManager mdt) throws Exception {
		JsltTask task;
		if ( m_script != null ) {
			if ( m_script.m_file != null ) {
				task = new JsltTask(m_script.m_file, null, null);
			}
			else if ( m_script.m_expr != null ) {
				task = new JsltTask(m_script.m_expr, null, null);
			}
		}
		
		task = new JsltTask(null, null);
		task.run(mdt.getInstanceManager());
	}

	public static void main(String... args) throws Exception {
		main(new JsltTaskCommand(), args);
	}
}
