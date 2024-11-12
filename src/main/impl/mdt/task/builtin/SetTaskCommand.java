package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.func.FOption;
import utils.io.IOUtils;

import mdt.cli.MDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.OperationVariableReference;
import mdt.model.sm.SubmodelElementReference;
import mdt.model.sm.SubmodelElementReferences;
import mdt.task.TaskException;
import mdt.task.builtin.SetTask.SetFileTask;
import mdt.task.builtin.SetTask.SetOperationsVariableTask;
import mdt.task.builtin.SetTask.SetPropertyTask;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTaskCommand extends MDTCommand {
	@Parameters(index="0", arity="1", paramLabel="ref", description="target SubmodelElementReference to set")
	private String m_target = null;
	
//	@Option(names={"--kind"}, paramLabel="opvar_kind", required=false,
//			description="(OperationVariable only) OperationVariable kind: ${COMPLETION-CANDIDATES}")
//	private Kind m_kind;
	
	@Option(names={"--path"}, paramLabel="path", required=false, description="file path for 'File' element")
	private String m_path;
	
	@ArgGroup(exclusive=true, multiplicity="1")
	private ValueSpec m_valueSpec;
	static class ValueSpec {
		@Option(names={"--value"}, arity="1..*", paramLabel="value(s)", description="Json string")
		private List<String> m_initialValues;

		@Option(names={"--file"}, paramLabel="Json file path", description="Json file path")
		private File m_jsonFile;
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
			if ( ref instanceof OperationVariableReference opvr ) {
				return new SetOperationsVariableTask(opvr, m_valueSpec.m_initialValues);
			}
			
			SubmodelElement sme = ref.read();
			if ( sme instanceof Property ) {
				String valueJson = FOption.map(m_valueSpec.m_initialValues, l -> l.get(0));
				if ( valueJson == null ) {
					valueJson = IOUtils.toString(m_valueSpec.m_jsonFile);
				}
				
				return new SetPropertyTask(ref, valueJson);
			}
			else if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
				return new SetFileTask(ref, m_valueSpec.m_jsonFile, m_path);
			}
			else {
				String msg = String.format("Unsupported SubmodelElement type %s for SetTask", sme.getClass());
				throw new IllegalArgumentException(msg);
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
