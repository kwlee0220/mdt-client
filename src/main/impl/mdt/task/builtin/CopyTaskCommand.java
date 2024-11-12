package mdt.task.builtin;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import mdt.cli.MDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.SubmodelElementReference;
import mdt.model.sm.SubmodelElementReferences;
import mdt.task.TaskException;
import mdt.task.builtin.CopyTask.CopyFileTask;
import mdt.task.builtin.CopyTask.CopyPropertyTask;

import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "copy", description = "copy SubmodelElement")
public class CopyTaskCommand extends MDTCommand {
	@Parameters(index="0", arity="1", paramLabel="ref", description="source SubmodelElementReference")
	private String m_from;
	
	@Parameters(index="1", arity="1", paramLabel="ref", description="target SubmodelElementReference")
	private String m_to;

	@Override
	protected void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		SubmodelElementReference fromRef = SubmodelElementReferences.parseString(m_from);
		if ( fromRef instanceof MDTInstanceManagerAwareReference aref ) {
			aref.activate(manager);
		}
		
		SubmodelElementReference toRef = SubmodelElementReferences.parseString(m_to);
		if ( toRef instanceof MDTInstanceManagerAwareReference aref ) {
			aref.activate(manager);
		}
		
		CopyTask task = newTask(fromRef, toRef);
		task.run(manager);
	}

	protected CopyTask newTask(SubmodelElementReference fromRef, SubmodelElementReference toRef) throws TaskException {
		try {
			SubmodelElement sme = fromRef.read();
			SubmodelElement toSme = toRef.read();
			
			if ( sme instanceof Property ) {
				Preconditions.checkArgument(toSme instanceof Property,
											"Incompatible for copy: from={}, to={}", sme.getClass(), toSme.getClass());
				return new CopyPropertyTask(fromRef, toRef);
			}
			else if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
				Preconditions.checkArgument(toSme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File,
											"Incompatible for copy: from={}, to={}", sme.getClass(), toSme.getClass());
				return new CopyFileTask(fromRef, toRef);
			}
			else {
				String msg = String.format("Unsupported SubmodelElement type %s for CopyTask", sme.getClass());
				throw new IllegalArgumentException(msg);
			}
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to create CopyTask", e);
		}
	}

	public static void main(String... args) throws Exception {
		main(new CopyTaskCommand(), args);
	}
}
