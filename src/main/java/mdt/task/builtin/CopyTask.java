package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import utils.LoggerSettable;
import utils.func.FOption;

import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.port.PortDeclaration;
import mdt.task.MDTTask;
import mdt.task.Port;
import mdt.task.Tasks;

import lombok.Getter;
import lombok.Setter;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class CopyTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(CopyTask.class);
	private static final String INPUT_PORT_NAME = "input";
	private static final String OUTPUT_PORT_NAME = "output";
	
	private boolean verbose;
	private Logger logger;
	private Instant m_started = null;
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("copy");
		tmplt.setName("AAS 모델 데이터 복사 태스크");
		tmplt.setType(CopyTask.class.getName());
		tmplt.setDescription("AAS SubmodelElement 사이의 데이터를 복사하는 태스크");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));

		tmplt.getInputPorts().add(new PortDeclaration(INPUT_PORT_NAME, "복사할 원시 SubmodelElement 위치"));
		tmplt.getOutputPorts().add(new PortDeclaration(OUTPUT_PORT_NAME, "변경될 SubmodelElement 위치"));
		
		return tmplt;
	}
	
	public CopyTask() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTInstanceManager manager, Map<String,Port> inputPorts,
					Map<String,Port> outputPorts, Duration timeout) {
		Port srcPort = inputPorts.get(INPUT_PORT_NAME);
		Port tarPort = outputPorts.get(OUTPUT_PORT_NAME);
		
		if ( srcPort == null ) {
			throw new IllegalArgumentException(String.format("Input port is missing: %s", INPUT_PORT_NAME));
		}
		if ( tarPort == null ) {
			throw new IllegalArgumentException(String.format("Output port is missing: %s", OUTPUT_PORT_NAME));
		}
		
		if ( srcPort.isValuePort() && !tarPort.isValuePort() ) {
			throw new IllegalStateException("Output port should be valueonly because input port is valueonly");
		}
		
		FOption<Port> elapsedTimePort = Tasks.findElapsedTimePort(outputPorts.values())
												.peek(p -> m_started = Instant.now());
		
		JsonNode src = srcPort.getJsonNode();
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("[IN] {}:", srcPort.getName());
			getLogger().info(AASUtils.writeJson(src));
			getLogger().info("---------------------------------------------------------------------");
		}
		
		tarPort.setJsonNode(src);
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("[OUT] {}:", tarPort.getName());
			getLogger().info(AASUtils.writeJson(src));
			getLogger().info("---------------------------------------------------------------------");
		}
		elapsedTimePort.forEach(p -> {
			Duration elapsed = Duration.between(m_started, Instant.now());
			
			if ( getLogger().isDebugEnabled() ) {
				getLogger().debug("save the elapsed task-execution time: {}", elapsed);
			}
			p.setJsonNode(new TextNode(elapsed.toString()));
		});
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(this.logger, s_logger);
	}
}
