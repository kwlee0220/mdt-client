package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.service.SubmodelService;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

	private SubmodelElementReference m_opRef;
	private boolean m_async = false;
	private Duration m_timeout = DEFAULT_TIMEOUT;
	private Duration m_pollInterval = DEFAULT_POLL_INTERVAL;

	@Override
	public void setMDTInstanceManager(MDTInstanceManager manager) {
	}
	
	public void setOperationReference(SubmodelElementReference ref) {
		m_opRef = ref;
	}
	
	public void setCallAsync(boolean async) {
		m_async = async;
	}
	
	public void setPollInterval(Duration interval) {
		m_pollInterval = interval;
	}
	
	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}

	@Override
	public void run(Map<String, Port> inputPorts, Map<String, Port> inoutPorts,
						Map<String, Port> outputPorts, Map<String, String> options) 
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		Operation op = m_opRef.getAsOperation();
		SubmodelService svc = m_opRef.getSubmodelService();
		String opIdShortPath = m_opRef.getIdShortPath();
		
		List<Port> inputPortList = reorderPorts(op.getInputVariables(), inputPorts);
		List<Port> inoutputPortList = reorderPorts(op.getInoutputVariables(), inoutPorts);
		List<Port> outputPortList = reorderPorts(op.getOutputVariables(), outputPorts);
		
		List<OperationVariable> inputVariables = Funcs.map(inputPortList, this::read);
		List<OperationVariable> inoutputVariables = Funcs.map(inoutputPortList, this::read);

		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(m_timeout.toMillis());
		OperationResult result = ( m_async )
								? svc.runOperationAsync(opIdShortPath, inputVariables, inoutputVariables,
														m_timeout, m_pollInterval)
								: svc.invokeOperationSync(opIdShortPath, inputVariables, inoutputVariables,
														jtimeout);
		
		FStream.from(inputPortList)
				.zipWith(FStream.from(result.getInoutputArguments()))
				.forEach(tup -> tup._1.set(tup._2.getValue()));
		FStream.from(outputPortList)
				.zipWith(FStream.from(result.getOutputArguments()))
				.forEach(tup -> tup._1.set(tup._2.getValue()));
	}
	
	private List<Port> reorderPorts(List<OperationVariable> varList, Map<String,Port> ports) {
		return FStream.from(varList)
						.map(opVar -> {
							String key = opVar.getValue().getIdShort();
							if ( key == null ) {
								throw new IllegalArgumentException("OperationVariable idShort is null");
							}
							Port port = ports.get(key);
							if ( port == null ) {
								throw new IllegalArgumentException("OperationVariable's value is missing: " + key);
							}
							return port;
						})
						.toList();
	}
	
	private OperationVariable read(Port port) {
		SubmodelElement sme = port.getSubmodelElement();
		return new DefaultOperationVariable.Builder()
											.value(sme)
											.build();
	}
	
	public static class Command extends MDTTaskCommand<AASOperationTask> {
		@Option(names={"--op"}, paramLabel="reference", description="the mdt-reference to the target operation")
		private String m_opRefString;

		@Option(names={"--async"}, description="invoke asynchronously")
		private boolean m_async = false;

		private Duration m_timeout = Duration.ofMinutes(5);
		@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\"")
		public void setTimeout(String toStr) {
			m_timeout = UnitUtils.parseDuration(toStr);
		}

		private Duration m_pollInterval = Duration.ofSeconds(5);
		@Option(names={"--pollInterval"}, paramLabel="duration",
				description="Status polling interval (e.g. \"1s\", \"500ms\"")
		public void setPollInterval(String intvStr) {
			m_pollInterval = UnitUtils.parseDuration(intvStr);
		}

		@Override
		protected AASOperationTask newTask() {
			AASOperationTask task = new AASOperationTask();
			task.setMDTInstanceManager(m_manager);
			
			SubmodelElementReference opRef = SubmodelElementReference.parseString(m_manager, m_opRefString);
			task.setOperationReference(opRef);
			
			task.setCallAsync(m_async);
			task.setPollInterval(m_pollInterval);
			task.setTimeout(m_timeout);
			
			return task;
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
