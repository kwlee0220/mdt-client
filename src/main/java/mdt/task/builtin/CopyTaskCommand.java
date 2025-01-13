package mdt.task.builtin;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferenceUtils;
import mdt.model.sm.ref.MDTInstanceManagerAwareReference;
import mdt.task.TaskException;
import mdt.task.builtin.CopyTask.CopyFileTask;
import mdt.task.builtin.CopyTask.CopyPropertyTask;

import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
//@picocli.CommandLine.Command(name = "copy", description = "copy SubmodelElement")
public class CopyTaskCommand extends AbstractMDTCommand {
	@Parameters(index="0", arity="1", paramLabel="src-ref", description="source SubmodelElementReference")
	private String m_from;
	
	@Parameters(index="1", arity="1", paramLabel="tar-ref", description="target SubmodelElementReference")
	private String m_to;

	@Override
	protected void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		ElementReference fromRef = ElementReferenceUtils.parseString(m_from);
		if ( fromRef instanceof MDTInstanceManagerAwareReference aref ) {
			aref.activate(manager);
		}
		
		ElementReference toRef = ElementReferenceUtils.parseString(m_to);
		if ( toRef instanceof MDTInstanceManagerAwareReference aref ) {
			aref.activate(manager);
		}
		
		CopyTask task = newTask(fromRef, toRef);
		task.run(manager);
	}

	protected CopyTask newTask(ElementReference fromRef, ElementReference toRef) throws TaskException {
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
