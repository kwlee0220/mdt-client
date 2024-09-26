package mdt.task.builtin;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Maps;

import utils.LoggerSettable;
import utils.Throwables;
import utils.func.FOption;
import utils.func.Funcs;

import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.port.PortDeclaration;
import mdt.task.MDTTask;
import mdt.task.Port;

import jslt2.Jslt2;
import jslt2.Jslt2Exception;
import lombok.Getter;
import lombok.Setter;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class JsltTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(JsltTask.class);

	private File scriptFile;
	private String scriptExpr;
	private Logger logger;
	
	public JsltTask() {
		setLogger(s_logger);
	}
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("jslt");
		tmplt.setName("JSLT 태스크");
		tmplt.setType(JsltTask.class.getName());
		tmplt.setDescription("JSLT script를 활용한 SME를 변형시키는 태스크.");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
		tmplt.getOptions().add(new OptionDescriptor("file", false, "JSLT script 파일 경로", null));
		tmplt.getOptions().add(new OptionDescriptor("expr", false, "JSLT script 문자열", null));
		
		tmplt.getOutputPorts().add(new PortDeclaration("output", "변형된 결과 SubmodelElement 저장될 위치"));
		
		return tmplt;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(this.logger, s_logger);
	}

	@Override
	public void run(MDTInstanceManager manager, Map<String,Port> inputPorts,
					Map<String,Port> outputPorts, Duration timeout) throws ExecutionException {
		try {
			String mergedJson = null;
			if ( inputPorts.size() > 1 ) {
				Map<String,JsonNode> merged = Maps.newHashMap();
				for ( Map.Entry<String,Port> ent: inputPorts.entrySet() ) {
					JsonNode value = ent.getValue().getJsonNode();

					if ( getLogger().isInfoEnabled() ) {
						getLogger().info("[IN] {}:", ent.getKey());
						getLogger().info(AASUtils.writeJson(value));
						getLogger().info("---------------------------------------------------------------------");
					}
					merged.put(ent.getKey(), value);
				}
				mergedJson = AASUtils.writeJson(merged);
			}
			else if ( inputPorts.size() == 1 ) {
				Port input = Funcs.getFirstOrNull(inputPorts.values());
				mergedJson = input.getAsJsonValueString();

				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("[IN] {}:", input.getName());
					getLogger().info(mergedJson);
					getLogger().info("---------------------------------------------------------------------");
				}
			}
			else {
				throw new ExecutionException(new IllegalArgumentException("Empty input port"));
			}
			
			JsonMapper mapper = AASUtils.getJsonMapper();
			Jslt2 runtime = Jslt2.builder().objectMapper(mapper).build();
			Jslt2UDFs.initialize(runtime);

			JsonNode input = mapper.readTree(mergedJson);
			Reader scriptReader;
			if ( this.scriptFile != null ) {
				scriptReader = new FileReader(this.scriptFile);
			}
			else if ( this.scriptExpr != null ) {
				scriptReader = new StringReader(this.scriptExpr);
			}
			else {
				scriptReader = new InputStreamReader(System.in);
			}
			JsonNode output = runtime.eval(scriptReader, input);
			String outputJson = mapper.writeValueAsString(output);
			
			Port outputPort = Funcs.getFirstOrNull(outputPorts.values());
			FOption.accept(outputPort, p -> p.setJsonString(outputJson));
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("[OUT] {}:", outputPort.getName());
				getLogger().info(outputJson);
				getLogger().info("---------------------------------------------------------------------");
			}
		}
		catch ( Jslt2Exception e ) {
			throw new ExecutionException(e.getCause());
		}
		catch ( Exception e ) {
			Throwables.throwIfInstanceOf(e, ExecutionException.class);
			throw new ExecutionException(e);
		}
		
		System.exit(0);
	}

	@Override
	public boolean cancel() {
		return false;
	}
}
