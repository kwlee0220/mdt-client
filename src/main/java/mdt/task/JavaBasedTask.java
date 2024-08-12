package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import utils.Throwables;
import utils.Utilities;
import utils.stream.FStream;

import mdt.model.instance.MDTInstanceManager;
import mdt.task.MDTTaskModule.Outputs;
import picocli.CommandLine.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class JavaBasedTask implements MDTTask {
	private MDTInstanceManager m_manager;
	private MDTTaskModule m_taskModule;
	private Duration m_timeout;

	@Override
	public void setMDTInstanceManager(MDTInstanceManager manager) {
		m_manager = manager;
	}
	
	public void setMDTTaskModule(MDTTaskModule module) {
		m_taskModule = module;
	}

	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}

	@Override
	public void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options)
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		Preconditions.checkState(m_manager != null);
		Preconditions.checkState(m_taskModule != null);
		
		Map<String,Object> inputValues = FStream.from(inputPorts)
												.mapValue(Port::getRawValue)
												.toMap();
		Map<String,Object> inoutValues = FStream.from(inoutPorts)
												.mapValue(Port::getRawValue)
												.toMap();
		List<String> outputPortNames = FStream.from(outputPorts).toKeyStream().toList();
		try {
			Outputs outputs = m_taskModule.run(m_manager, inputValues, inoutValues, outputPortNames,
												options, m_timeout);
			
			Map<String,Object> merged = Maps.newHashMap(outputs.getInoutPortValues());
			merged.putAll(outputs.getOutputPortValues());
			merged.forEach((k,v) -> {
				Port port = outputPorts.get(k);
				if ( port != null ) {
					port.set(v);
				}
			});
		}
		catch ( Exception e ) {
			Throwables.throwIfInstanceOf(e, TimeoutException.class);
			Throwables.throwIfInstanceOf(e, InterruptedException.class);
			Throwables.throwIfInstanceOf(e, CancellationException.class);
			Throwables.throwIfInstanceOf(e, ExecutionException.class);
			throw new ExecutionException(e);
		}
	}
	
	public static class Command extends MDTTaskCommand<JavaBasedTask> {
		@Option(names={"--task_class"}, paramLabel="FQCN",
				description="Fully qualified class name for MDTTask Module")
		private String m_taskModuleClassName;

		@Override
		protected JavaBasedTask newTask() {
			JavaBasedTask task = new JavaBasedTask();
			task.setMDTInstanceManager(m_manager);
			task.setMDTTaskModule(Utilities.newInstance(m_taskModuleClassName, MDTTaskModule.class));
			task.setTimeout(m_timeout);
			
			return task;
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
