package mdt.task.builtin;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import utils.async.StartableExecution;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.client.operation.ProcessBasedMDTOperation;
import mdt.client.operation.ProcessBasedMDTOperation.Builder;
import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.task.AbstractAsyncTask;
import mdt.task.MDTTask;
import mdt.task.Port;

import lombok.Getter;
import lombok.Setter;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class ProgramTask extends AbstractAsyncTask<Map<String,String>> {
	private static final Logger s_logger = LoggerFactory.getLogger(ProgramTask.class);
	
	private List<String> command;
	private File workingDir;
	private String mergedInputPortName = null;
	
	public ProgramTask() {
		setLogger(s_logger);
	}

	@Override
	public StartableExecution<Map<String, String>> buildExecution(MDTInstanceManager manager,
																	Map<String, Port> inputPorts,
																	Map<String, Port> outputPorts,
																	Duration timeout) {
		Preconditions.checkArgument(this.command != null && this.command.size() > 0);
		
		Builder opBuilder = ProcessBasedMDTOperation.builder()
													.setCommand(this.command)
													.addPortFileToCommandLine(false);
		FOption.accept(this.workingDir, opBuilder::setWorkingDirectory);
		FOption.accept(timeout, opBuilder::setTimeout);
		
		if ( this.mergedInputPortName != null ) {
			Map<String,JsonNode> merged = FStream.from(inputPorts)
												.mapValue((k,p) -> p.getJsonNode())
												.toMap();
			opBuilder.addFileArgument(this.mergedInputPortName, AASUtils.writeJson(merged), false);
		}
		else {
			FStream.from(inputPorts.values())
					.forEachOrThrow(port -> {
						// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
						String argFileName = String.format("%s", port.getName());
						String valueString = port.getAsJsonValueString();
						opBuilder.addFileArgument(argFileName, valueString, false);
						
						if ( getLogger().isInfoEnabled() ) {
							getLogger().info("[IN] {}:", port.getName());
							getLogger().info(valueString);
							getLogger().info("---------------------------------------------------------------------");
						}
					});
		}
		FStream.from(outputPorts.values())
				.filterNot(p -> p.getName().equals(MDTTask.ELAPSED_TIME_PORT_NAME))
				.forEachOrThrow(port -> {
					// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
					String valueString = port.getAsJsonValueString();
					opBuilder.addFileArgument(port.getName(), valueString, true);
				});
		
		return opBuilder.build();
	}

	@Override
	public void updateOutputs(Map<String, String> outputs, Map<String, Port> outputPorts) {
		outputs.forEach((k, json) -> {
			Port port = outputPorts.get(k);
			if ( port != null ) {
				port.setJsonString(json);
				
				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("[OUT] {}:", port.getName());
					getLogger().info(json);
					getLogger().info("---------------------------------------------------------------------");
				}
			}
		});
	}
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("program");
		tmplt.setName("실행 프로그램 기반 태스크");
		tmplt.setType(ProgramTask.class.getName());
		tmplt.setDescription("실행 프로그램을 구동하여 태스크를 수행하는 태스크");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
		tmplt.getOptions().add(new OptionDescriptor("command", true, "구동시킬 프로그램 경로", null));
		tmplt.getOptions().add(new OptionDescriptor("workingDir", false, "실행 프로그램을 구동시킬 디렉토리 경로", null));
		tmplt.getOptions().add(new OptionDescriptor("timeout", false, "태스크 수행 제한 시간", null));
		
		return tmplt;
	}
}
