package mdt.task.builtin;

import java.io.File;
import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import utils.io.IOUtils;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.SubmodelElementReference;
import mdt.model.sm.SubmodelElementReferences;
import mdt.task.TaskException;
import mdt.task.builtin.SetTask.SetDefaultTask;
import mdt.task.builtin.SetTask.SetFileTask;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTaskCommand extends AbstractMDTCommand {
	@Parameters(index="0", arity="1", paramLabel="ref", description="target SubmodelElementReference to set")
	private String m_target = null;
	
	@ArgGroup(exclusive=true)
	private PropertyElementSpec m_propSpec;
	static class PropertyElementSpec {
		@Option(names={"--value"}, arity="1", paramLabel="value(s)", description="Json string")
		private String m_value;

		@Option(names={"--json"}, paramLabel="Json file path", description="Json file path")
		private File m_jsonFile;
	}
	
	@ArgGroup(exclusive = false)
	FileElementSpec m_fileElementSpec;
	static class FileElementSpec {
		@Option(names = {"--file", "-f"}, paramLabel="file path", required=true)
		private File m_file;
		
		@Option(names = {"--path"}, paramLabel="value for File", required=false)
		private String m_path;
	}

	@Override
	protected void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		SubmodelElementReference ref = SubmodelElementReferences.parseString(m_target);
		if ( ref instanceof MDTInstanceManagerAwareReference aref ) {
			aref.activate(manager);
		}
		
		SetTask task = newTask(ref);
		task.run(manager);
	}

	protected SetTask newTask(SubmodelElementReference ref) throws TaskException {
		try {
			SubmodelElement sme = ref.read();
			 if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
				Preconditions.checkArgument(m_fileElementSpec != null);
				
				if ( m_fileElementSpec.m_path == null ) {
					m_fileElementSpec.m_path = m_fileElementSpec.m_file.getName();
				}
				
				return new SetFileTask(ref, m_fileElementSpec.m_file, m_fileElementSpec.m_path);
			 }
			 else {
				if ( m_propSpec.m_value != null ) {
					return new SetDefaultTask(ref, m_propSpec.m_value);
				}
				else if ( m_propSpec.m_jsonFile != null ) {
					String jsonStr = IOUtils.toString(m_propSpec.m_jsonFile);
					return new SetDefaultTask(ref, jsonStr);
				}
				else {
					throw new IllegalArgumentException("Value is not specified");
				}
			}
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to create SetTask", e);
		}
	}

	public static void main(String... args) throws Exception {
		main(new SetTaskCommand(), args);
	}
}
