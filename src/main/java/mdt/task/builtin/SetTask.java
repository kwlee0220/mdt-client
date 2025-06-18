package mdt.task.builtin;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;
import mdt.task.AbstractMDTTask;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTask extends AbstractMDTTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(SetTask.class);
	public static final String VARIABLE_SOURCE = "source";
	public static final String VARIABLE_TARGET = "target";
	
	public SetTask(TaskDescriptor descriptor) {
		super(descriptor);
		
		setLogger(s_logger);
	}
	
	@Override
	public void run(MDTInstanceManager manager) throws TimeoutException, InterruptedException,
														CancellationException, TaskException {
		try {
			Variable target = getTaskDescriptor().getOutputVariables().getOfKey(VARIABLE_TARGET);
			Variables.activate(target, manager);
			
			Variable source = getTaskDescriptor().getInputVariables().getOfKey(VARIABLE_SOURCE);
			Variables.activate(source, manager);
			
			target.updateValue(source.readValue());
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to run SetTask: " + this, e);
		}
	}
}