package mdt.task.builtin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Maps;

import utils.Throwables;
import utils.Tuple;
import utils.async.CommandVariable;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.variable.Variable;
import mdt.task.AbstractMDTTask;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;

import jslt2.Jslt2;
import jslt2.Jslt2Exception;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class JsltTask extends AbstractMDTTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(JsltTask.class);

	private final File m_scriptFile;
	private final String m_scriptExpr;
	
	public JsltTask(TaskDescriptor descriptor, File scriptFile) {
		super(descriptor);
		
		m_scriptFile = scriptFile;
		m_scriptExpr = null;
	}
	
	public JsltTask(TaskDescriptor descriptor, String scriptExpr) {
		super(descriptor);
		
		m_scriptFile = null;
		m_scriptExpr = scriptExpr;
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return new TaskDescriptor();
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		try {
			TaskDescriptor descriptor = getTaskDescriptor();
			
			Map<String,SubmodelElement> values = Maps.newHashMap();
			for ( Variable port: descriptor.getInputVariables() ) {
				SubmodelElement sme = port.read();
				values.put(port.getName(), sme);
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
			JsonNode result = runtime.eval(scriptReader, input);
			
			// Transform 결과를 task variable들에 반영한다.
			updateOutputVariables(result);
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
	
	private void updateOutputVariables(JsonNode results) throws IOException {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		for ( Variable outPort: descriptor.getOutputVariables() ) {
			JsonNode result = results.get(outPort.getName());
			if ( s_logger.isInfoEnabled() ) {
				String valueStr = MDTModelSerDe.toJsonString(result);
				s_logger.info("update variable[{}] with {}", outPort.getName(), valueStr);
			}
			outPort.updateWithValueJsonNode(result);
		}
	}
}
