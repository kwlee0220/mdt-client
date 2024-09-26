package mdt.task.builtin;

import java.time.Duration;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.Maps;

import utils.InternalException;
import utils.async.StartableExecution;
import utils.stream.FStream;

import lombok.Getter;
import lombok.Setter;
import mdt.client.SSLUtils;
import mdt.client.operation.HttpOperationClient;
import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.task.AbstractAsyncTask;
import mdt.task.Port;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class HttpTask extends AbstractAsyncTask<JsonNode> {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTask.class);
	private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(3);

	private String taskServerEndpoint;
	private Duration pollInterval = DEFAULT_POLL_TIMEOUT;
	
	public HttpTask() {
		setLogger(s_logger);
	}
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("http");
		tmplt.setName("HTTP기반 원격 수행 태스크");
		tmplt.setType(HttpTask.class.getName());
		tmplt.setDescription("실행 프로그램을 구동하여 태스크를 수행하는 태스크.");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
		tmplt.getOptions().add(new OptionDescriptor("url", true, "원격 태스크 수행 서버 URL", null));
		tmplt.getOptions().add(new OptionDescriptor("pollInterval", false, "비동기로 태스크가 수행되는 경우 서버 수행 여부 확인 시간 간격", null));
		tmplt.getOptions().add(new OptionDescriptor("timeout", false, "태스크 수행 제한 시간", null));
		
		return tmplt;
	}

	@Override
	public StartableExecution<JsonNode> buildExecution(MDTInstanceManager manager,
														Map<String, Port> inputPorts,
														Map<String, Port> outputPorts,
														Duration timeout) throws Exception {
		Map<String,Object> parameters = Maps.newHashMap();
		for ( Map.Entry<String,Port> ent: inputPorts.entrySet() ) {
			Port port = ent.getValue();
			
			JsonNode value = port.getJsonNode();
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("[IN] {}:", ent.getKey());
				getLogger().info(AASUtils.writeJson(value));
				getLogger().info("---------------------------------------------------------------------");
			}
			parameters.put(ent.getKey(), value);
		}
		FStream.from(outputPorts).mapValue(Port::getJsonNode).forEach(parameters::put);
		String paramsJsonStr = AASUtils.writeJson(parameters);
		
		return HttpOperationClient.builder()
								.setHttpClient(SSLUtils.newTrustAllOkHttpClientBuilder().build())
								.setEndpoint(this.taskServerEndpoint)
								.setRequestBodyJson(paramsJsonStr)
								.setPollInterval(this.pollInterval)
								.setTimeout(timeout)
								.build();
	}

	@Override
	public void updateOutputs(JsonNode outputs, Map<String, Port> outputPorts) {
		for ( Map.Entry<String,Port> ent: outputPorts.entrySet() ) {
			Port port = ent.getValue();
			
			JsonNode output = outputs.get(port.getName());
			update(port, output);
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("[OUT] {}:", ent.getKey());
				getLogger().info(AASUtils.writeJson(output));
				getLogger().info("---------------------------------------------------------------------");
			}
		}
	}
	
	private void update(Port port, JsonNode valueNode) {
		if ( port.isValuePort() ) {
			try {
				JsonMapper mapper = AASUtils.getJsonMapper();
				if ( valueNode instanceof ValueNode ) {
					port.setJsonString(valueNode.asText());
				}
				else {
					String json = mapper.writeValueAsString(valueNode);
					port.setJsonString(json);
				}
			}
			catch ( JsonProcessingException e ) {
				throw new InternalException("Failed to write JSON, cause=" + e);
			}
		}
		else {
			SubmodelElement result = AASUtils.readJson(valueNode, SubmodelElement.class);
			port.set(result);
		}
	}
}
