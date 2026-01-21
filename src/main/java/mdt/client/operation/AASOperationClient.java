package mdt.client.operation;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Message;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.async.AbstractStatePoller;
import utils.func.Funcs;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.AASUtils;
import mdt.model.SubmodelService;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationClient extends AbstractStatePoller<OperationResult> {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationClient.class);
	private static final javax.xml.datatype.Duration INFINITE = AASUtils.DATATYPE_FACTORY.newDuration("P7D");
	
	private final SubmodelService m_submodelSvc;
	private final String m_operationPath;
	private Operation m_operaionElm;
	private Map<String,OperationVariable> m_inputs = Maps.newHashMap();
	private Map<String,OperationVariable> m_outputs = Maps.newHashMap();
	private Map<String,OperationVariable> m_inoutputs = Maps.newHashMap();
	private OperationHandle m_opHandle;
	
	public AASOperationClient(SubmodelService svc, String operationPath, Duration pollInterval) throws IOException {
		super(pollInterval, true);
		
		setLogger(s_logger);
		m_submodelSvc = svc;
		m_operationPath = operationPath;

		loadOperation();
	}
	
	public AASOperationClient(MDTElementReference opRef, Duration pollInterval) throws IOException {
		super(pollInterval, true);
		
		setLogger(s_logger);
		m_submodelSvc = opRef.getSubmodelService();
		m_operationPath = opRef.getIdShortPathString();
		loadOperation();
	}
	
	public void loadOperation() throws IOException {
		m_operaionElm = (Operation)m_submodelSvc.getSubmodelElementByPath(m_operationPath);
		
		m_inputs = toMap(m_operaionElm.getInputVariables());
		m_outputs = toMap(m_operaionElm.getOutputVariables());
		m_inoutputs = toMap(m_operaionElm.getInoutputVariables());
	}
	
	public void saveOperation() throws IOException {
		m_operaionElm.setInputVariables(FStream.from(m_inputs.values()).toList());
		m_operaionElm.setOutputVariables(FStream.from(m_outputs.values()).toList());
		m_operaionElm.setInoutputVariables(FStream.from(m_inoutputs.values()).toList());
		
		m_submodelSvc.setSubmodelElementByPath(m_operationPath, m_operaionElm);
	}
	
	@Override
	protected void initializePoller() throws Exception {
		List<OperationVariable> inputs = m_inputs.values().stream().toList();
		List<OperationVariable> inoutputs = m_inoutputs.values().stream().toList();
		javax.xml.datatype.Duration jtimeout = (getTimeout() != null)
											? AASUtils.DATATYPE_FACTORY.newDuration(getTimeout().toMillis())
											: INFINITE;
		
		m_opHandle = m_submodelSvc.invokeOperationAsync(m_operationPath, inputs, inoutputs, jtimeout);
		getLogger().info("invoked operation async, handle={}", m_opHandle.getHandleId());
	};

	@Override
	protected Optional<OperationResult> pollState() throws Exception {
		BaseOperationResult result = m_submodelSvc.getOperationAsyncStatus(m_opHandle);
		getLogger().info("polled operation result: {}", result.getExecutionState());
		switch ( result.getExecutionState() ) {
			case COMPLETED:
				return Optional.of(m_submodelSvc.getOperationAsyncResult(m_opHandle));
			case FAILED:
				throw new Exception("operation execution failed: " + toMessage(result));
			case CANCELED:
				throw new CancellationException("operation execution cancelled: " + toMessage(result));
			case TIMEOUT:
				return null;
			default:
				return Optional.empty();
		}
	}
	
	@Override
	protected void finalizePoller(OperationResult result) throws Exception {
		if ( result != null ) {
			m_outputs = toMap(result.getOutputArguments()); 
			m_inoutputs = toMap(result.getInoutputArguments());
		}
	};
	
	public Map<String, ElementValue> getInputVariableValues() {
		return KeyValueFStream.from(m_inputs)
								.mapValue(opv -> ElementValues.getValue(opv.getValue()))
								.toMap();
	}
	public void setInputVariableValues(Map<String,ElementValue> values) {
		KeyValueFStream.from(m_inputs)
						.match(values)
						.forEach((k, match) -> {
							ElementValues.update(match._1.getValue(), match._2);
						});
	}
	public void setInputVariableValue(String name, ElementValue value) {
		OperationVariable opVar = m_inputs.get(name);
		if ( opVar != null ) {
			ElementValues.update(opVar.getValue(), value);
		}
	}

	public Map<String, ElementValue> getOutputVariableValues() {
		return KeyValueFStream.from(m_outputs)
								.mapValue(opv -> ElementValues.getValue(opv.getValue()))
								.toMap();
	}
	public void setOutputVariableValues(Map<String,ElementValue> values) {
		KeyValueFStream.from(m_outputs)
						.match(values)
						.forEach((k, match) -> {
							ElementValues.update(match._1.getValue(), match._2);
						});
	}
	public void setOutputVariableValue(String name, ElementValue value) {
		OperationVariable opVar = m_outputs.get(name);
		if ( opVar != null ) {
			ElementValues.update(opVar.getValue(), value);
		}
	}

	public Map<String, ElementValue> getInoutputVariableValues() {
		return KeyValueFStream.from(m_inoutputs)
								.mapValue(opv -> ElementValues.getValue(opv.getValue()))
								.toMap();
	}
	public void setInoutputVariableValues(Map<String,ElementValue> values) {
		KeyValueFStream.from(m_inoutputs)
						.match(values)
						.forEach((k, match) -> {
							ElementValues.update(match._1.getValue(), match._2);
						});
	}

	public void setInoutputVariableValue(String name, ElementValue value) {
		OperationVariable opVar = m_inoutputs.get(name);
		if ( opVar != null ) {
			ElementValues.update(opVar.getValue(), value);
		}
	}
	
	private Map<String, OperationVariable> toMap(Iterable<OperationVariable> opvIter) {
		return Funcs.toMap(opvIter, opv -> KeyValue.of(opv.getValue().getIdShort(), opv));
	}
	
	private String toMessage(BaseOperationResult result) {
		Message msg = result.getMessages().getFirst();
		return String.format("[%s]: %s", msg.getMessageType(), msg.getText());
	}
}
