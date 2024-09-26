package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.LoggerSettable;
import utils.func.FOption;
import utils.io.IOUtils;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.port.PortDeclaration;
import mdt.task.MDTTask;
import mdt.task.Port;

import lombok.Getter;
import lombok.Setter;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class SetTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(SetTask.class);
	private static final String OUTPUT_PORT_NAME = "output";
	
	private String jsonStr;
	private File jsonFile;
	private Logger logger;
	
	public SetTask() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTInstanceManager manager, Map<String,Port> inputPorts,
					Map<String,Port> outputPorts, Duration timeout) throws ExecutionException {
		Port tarPort = outputPorts.get(OUTPUT_PORT_NAME);
		if ( tarPort == null ) {
			throw new IllegalArgumentException(String.format("Output port is missing: %s", OUTPUT_PORT_NAME));
		}
		
		String jstr = jsonStr;
		if ( jstr == null ) {
			try {
				jstr = IOUtils.toString(jsonFile);
			}
			catch ( IOException e ) {
				throw new ExecutionException(e);
			}
		}

		if ( getLogger().isDebugEnabled() ) {
			getLogger().debug("set target port: " + tarPort + ", value=" + jstr);
		}

		tarPort.setJsonString(jstr);
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("[OUT] {}:", tarPort.getName());
			getLogger().info(jstr);
			getLogger().info("---------------------------------------------------------------------");
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(this.logger, s_logger);
	}
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("set");
		tmplt.setName("AAS 모델 데이터 변경 태스크");
		tmplt.setType(SetTask.class.getName());
		tmplt.setDescription("AAS 모델의 데이터를 갱신하는 태스크");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));

		tmplt.getOutputPorts().add(new PortDeclaration(OUTPUT_PORT_NAME, "변경될 SubmodelElement 위치"));
		
		return tmplt;
	}
}
