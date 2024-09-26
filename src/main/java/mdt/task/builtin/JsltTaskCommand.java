package mdt.task.builtin;

import java.io.File;

import mdt.task.MDTTaskCommand;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "jslt", description = "transform Port's data")
public class JsltTaskCommand extends MDTTaskCommand<JsltTask> {
	@ArgGroup(exclusive=true, multiplicity="0..1")
	private ScriptSpec m_script;
	static class ScriptSpec {
		@Option(names={"--expr"}, paramLabel="script", required=true, description="JSTL2 script")
		private String m_expr;

		@Option(names={"--file"}, paramLabel="script-file", required=true, description="Jstl2 script file path")
		private File m_file;
	}
	
	@Override
	protected JsltTask newTask() {
		JsltTask task = new JsltTask();
		if ( m_script != null ) {
			task.setScriptFile(m_script.m_file);
			task.setScriptExpr(m_script.m_expr);
		}
		
		return task;
	}

	public static void main(String... args) throws Exception {
		main(new JsltTaskCommand(), args);
	}
}
