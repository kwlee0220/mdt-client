package mdt.task.builtin;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import mdt.model.instance.MDTInstanceManager;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.ArgumentSpec.ReferenceArgumentSpec;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(SetTask.class);
	public static final String ARG_SOURCE = "source";
	public static final String ARG_TARGET = "target";
	public static final String OPTION_LOG_LEVEL = "loglevel";
	
	private final TaskDescriptor m_descriptor;
	
	public SetTask(TaskDescriptor descriptor) {
		m_descriptor = descriptor;
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
	}

	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
			throws TimeoutException, InterruptedException, CancellationException, TaskException {
		try {
			ArgumentSpec srcArgSpec = m_descriptor.getInputArgumentSpecs().get(ARG_SOURCE);
			if ( srcArgSpec == null ) {
				throw new TaskException("No input argument spec for key=" + ARG_SOURCE);
			}
			ArgumentSpec outArgSpec = m_descriptor.getOutputArgumentSpecs().get(ARG_TARGET);
			if ( outArgSpec == null ) {
				throw new TaskException("No output argument spec for key=" + ARG_TARGET);
			}
			
			Preconditions.checkArgument(outArgSpec instanceof ReferenceArgumentSpec,
					                    "Output argument '%s' is not a ReferenceArgumentSpec", ARG_TARGET);
			
			((ReferenceArgumentSpec)outArgSpec).updateValue(srcArgSpec.readValue());
			
			return Map.of(ARG_TARGET, ((ReferenceArgumentSpec)outArgSpec).read());
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to set value: cause=" + e, e);
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}
}