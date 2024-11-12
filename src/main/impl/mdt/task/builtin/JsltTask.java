package mdt.task.builtin;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Maps;

import utils.KeyedValueList;
import utils.Throwables;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.task.MultiParameterTask;
import mdt.task.Parameter;
import mdt.task.TaskException;

import jslt2.Jslt2;
import jslt2.Jslt2Exception;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class JsltTask extends MultiParameterTask {
	private static final Logger s_logger = LoggerFactory.getLogger(JsltTask.class);

	private final File m_scriptFile;
	private final String m_scriptExpr;
	
	public JsltTask(KeyedValueList<String,Parameter> parameters, Set<String> outputParameterNames) {
		super(parameters, outputParameterNames);
		
		m_scriptFile = null;
		m_scriptExpr = null;
	}
	
	public JsltTask(File scriptFile, KeyedValueList<String,Parameter> parameters, Set<String> outputParameterNames) {
		super(parameters, outputParameterNames);
		
		m_scriptFile = scriptFile;
		m_scriptExpr = null;
	}
	
	public JsltTask(String scriptExpr, KeyedValueList<String,Parameter> parameters, Set<String> outputParameterNames) {
		super(parameters, outputParameterNames);
		
		m_scriptFile = null;
		m_scriptExpr = scriptExpr;
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		try {
			Map<String,SubmodelElement> values = Maps.newHashMap();
			for ( Parameter param: FStream.from(m_parameters) ) {
				SubmodelElement sme = param.getReference().read();
				values.put(param.getName(), sme);
			}
			JsonNode input = MDTModelSerDe.toJsonNode(values);
			
			// Jslt2 runtime을 생성한다.
			JsonMapper mapper = MDTModelSerDe.getJsonMapper();
			Jslt2 runtime = Jslt2.builder().objectMapper(mapper).build();
			Jslt2UDFs.initialize(runtime);
			
			// script를 읽는다.
			Reader scriptReader;
			if ( this.m_scriptFile != null ) {
				scriptReader = new FileReader(this.m_scriptFile);
			}
			else if ( this.m_scriptExpr != null ) {
				scriptReader = new StringReader(this.m_scriptExpr);
			}
			else {
				scriptReader = new InputStreamReader(System.in);
			}
			
			// Transform 작업을 수행한다.
			JsonNode output = runtime.eval(scriptReader, input);
			
			// Transform 결과를 task variable들에 반영한다.
			FStream.from(m_outputParameterNames)
					.map(m_parameters::getOfKey)
					.innerJoin(FStream.from(output.fields()), Parameter::getName, Map.Entry::getKey)
					.forEachOrThrow(match -> {
						if ( s_logger.isInfoEnabled() ) {
							String valueStr = MDTModelSerDe.toJsonString(match._2.getValue());
							s_logger.info("update variable[{}] with {}", match._1.getName(), valueStr);
						}
						match._1.getReference().updateWithValueJsonNode(match._2.getValue());
					});
		}
		catch ( Jslt2Exception e ) {
			throw new TaskException(e.getCause());
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			throw new TaskException(cause);
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}
	
//	public static TaskDescriptor getTemplateDescriptor() {
//		TaskDescriptor tmplt = new TaskDescriptor();
//		tmplt.setId("jslt");
//		tmplt.setName("JSLT 태스크");
//		tmplt.setType(JsltTask.class.getName());
//		tmplt.setDescription("JSLT script를 활용한 SME를 변형시키는 태스크.");
//		
//		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
//		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
//		tmplt.getOptions().add(new OptionDescriptor("file", false, "JSLT script 파일 경로", null));
//		tmplt.getOptions().add(new OptionDescriptor("expr", false, "JSLT script 문자열", null));
//
//		VariableDescriptor var = VariableDescriptor.declare("output", Kind.OUTPUT,
//															"변형된 결과 SubmodelElement 저장될 위치");
//		tmplt.getVariables().add(var);
//		
//		return tmplt;
//	}
}
