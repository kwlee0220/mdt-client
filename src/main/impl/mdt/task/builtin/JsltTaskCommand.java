package mdt.task.builtin;

import java.io.File;
import java.util.Set;

import utils.KeyedValueList;

import mdt.task.MultiParameterTaskCommand;
import mdt.task.Parameter;
import mdt.task.TaskException;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "jslt", description = "transform Port's data")
public class JsltTaskCommand extends MultiParameterTaskCommand<JsltTask> {
	@ArgGroup(exclusive=true, multiplicity="0..1")
	private ScriptSpec m_script;
	static class ScriptSpec {
		@Option(names={"--expr"}, paramLabel="script", required=true, description="JSTL2 script")
		private String m_expr;

		@Option(names={"--file"}, paramLabel="script-file", required=true, description="Jstl2 script file path")
		private File m_file;
	}
	
	@Override
	protected JsltTask newTask(KeyedValueList<String,Parameter> parameters,
								Set<String> outputParameterNames) throws TaskException {
		if ( m_script != null ) {
			if ( m_script.m_file != null ) {
				return new JsltTask(m_script.m_file, parameters, outputParameterNames);
			}
			else if ( m_script.m_expr != null ) {
				return new JsltTask(m_script.m_expr, parameters, outputParameterNames);
			}
		}
		return new JsltTask(parameters, outputParameterNames);
	}

	public static void main(String... args) throws Exception {
		main(new JsltTaskCommand(), args);
	}
}
