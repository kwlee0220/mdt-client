package mdt.task.builtin;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Utilities;
import utils.async.AbstractThreadedExecution;
import utils.async.StartableExecution;
import utils.stream.FStream;

import lombok.Getter;
import lombok.Setter;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.task.AbstractAsyncTask;
import mdt.task.MDTTaskModule;
import mdt.task.Port;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class JavaTask extends AbstractAsyncTask<Map<String,Object>> {
	private static final Logger s_logger = LoggerFactory.getLogger(JavaTask.class);

	private String taskModuleClassName;
	
	public JavaTask() {
		setLogger(s_logger);
	}

	@Override
	public StartableExecution<Map<String, Object>> buildExecution(MDTInstanceManager manager,
																	Map<String, Port> inputPorts,
																	Map<String, Port> outputPorts,
																	Duration timeout) {
		MDTTaskModule module = Utilities.newInstance(this.taskModuleClassName, MDTTaskModule.class);
		Map<String,Object> inputValues = FStream.from(inputPorts)
												.mapValue(Port::getRawValue)
												.toMap();
		
		return new TaskModuleExecution(module, inputValues, timeout);
	}

	@Override
	public void updateOutputs(Map<String, Object> outputs, Map<String, Port> outputPorts) {
		outputs.forEach((k,v) -> {
			Port port = outputPorts.get(k);
			if ( port != null ) {
				port.set(v);
			}
		});
	}
	
	private static class TaskModuleExecution extends AbstractThreadedExecution<Map<String,Object>> {
		private final MDTTaskModule m_module;
		private final Map<String,Object> m_inputs;
		private final Duration m_timeout;
		
		TaskModuleExecution(MDTTaskModule module, Map<String,Object> inputs, Duration timeout) {
			m_module = module;
			m_inputs = inputs;
			m_timeout = timeout;
		}
		
		@Override
		protected Map<String, Object> executeWork() throws InterruptedException, CancellationException,
															Exception {
			return m_module.run(m_inputs, m_timeout);
		}
	};
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("java");
		tmplt.setName("Java 기반 태스크");
		tmplt.setType(JavaTask.class.getName());
		tmplt.setDescription("Java 클래스를 활용하여 태스크를 수행하는 태스크");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
		tmplt.getOptions().add(new OptionDescriptor("class", true, "실행시킬 Java 클래스의 FQCN", null));
		tmplt.getOptions().add(new OptionDescriptor("timeout", false, "태스크 수행 제한 시간", null));
		
		return tmplt;
	}
}
