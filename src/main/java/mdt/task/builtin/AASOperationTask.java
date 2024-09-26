package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;
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

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;
import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.SubmodelElementReference;
import mdt.model.service.SubmodelService;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.task.MDTTask;
import mdt.task.Port;
import utils.LoggerSettable;
import utils.func.FOption;
import utils.func.Funcs;
import utils.stream.FStream;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class AASOperationTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	private String operationReferenceString;
	private boolean async = false;
	private Duration pollInterval = DEFAULT_POLL_INTERVAL;
	private Logger logger;
	
	private Port m_elapsedTimePort = null;
	private Instant m_started = null;
	
	public static TaskTemplateDescriptor getTemplateDescriptor() {
		TaskTemplateDescriptor tmplt = new TaskTemplateDescriptor();
		tmplt.setId("aas");
		tmplt.setName("AAS Operation 구동 태스크");
		tmplt.setType(AASOperationTask.class.getName());
		tmplt.setDescription("AAS 모델 내 Operation을 구동하는 태스크.");
		
		tmplt.getOptions().add(new OptionDescriptor("endpoint", false, "MDT-Manager 접속 endpoint", null));
		tmplt.getOptions().add(new OptionDescriptor("logger", false, "Logger level", null));
		tmplt.getOptions().add(new OptionDescriptor("operation", true,
													"실행시킬 Operation SubmodelElement reference", null));
		tmplt.getOptions().add(new OptionDescriptor("async", false, "비동기 태스크 수행 여부. (default: false)", null));
		tmplt.getOptions().add(new OptionDescriptor("timeout", false, "태스크 수행 제한 시간", null));
		
		return tmplt;
	}

	@Override
	public void run(MDTInstanceManager manager, Map<String, Port> inputPorts,
					Map<String, Port> outputPorts, Duration timeout) 
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		Preconditions.checkArgument(manager != null);

		m_elapsedTimePort = Funcs.findFirst(outputPorts.values(),
									p -> p.getName().equals(MDTTask.ELAPSED_TIME_PORT_NAME))
								.peek(p -> m_started = Instant.now())
								.getOrNull();
		
		SubmodelElementReference opRef = SubmodelElementReference.parseString(manager, this.operationReferenceString);
		Operation op = opRef.getAsOperation();
		SubmodelService svc = opRef.getSubmodelService();
		String opIdShortPath = opRef.getIdShortPath();
		
		// Operation SubmodelElement에 정의된 InputVariable의 순서대로
		// input port를 정렬시킨다.
		List<Port> inputPortList = reorderPorts(op.getInputVariables(), inputPorts);
		// Operation SubmodelElement에 정의된 OutputVariable의 순서대로
		// output port를 정렬시킨다.
		List<Port> outputPortList = reorderPorts(op.getOutputVariables(), outputPorts);
		
		List<OperationVariable> inputVariables = Funcs.map(inputPortList, this::read);
		
		OperationResult result = null;
		if ( this.async ) {
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("invoking AASOperation asynchronously: id={}, timeout={}, pollInterval={}",
								opIdShortPath, timeout, this.pollInterval);	
			}
			result = svc.runOperationAsync(opIdShortPath, inputVariables, List.of(),
											timeout, this.pollInterval);
		}
		else {
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("invoking AASOperation synchronously: id={}, timeout={}", opIdShortPath, timeout);	
			}
			javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(timeout.toMillis());
			result = svc.invokeOperationSync(opIdShortPath, inputVariables, List.of(), jtimeout);
		}
		if ( !result.getInoutputArguments().isEmpty() ) {
			if ( getLogger().isWarnEnabled() ) {
				String nameListStr = FStream.from(result.getInoutputArguments())
											.flatMapNullable(ov -> ov.getValue().getIdShort())
											.join(", ");
				getLogger().warn("MDTTask does not support AASOperations's inoutput variables: {}", nameListStr);
			}
		}
		
		// AAS operation 호출 결과를 해당 output Port를 통해 반영시킨다.
		FStream.from(outputPortList)
				.zipWith(FStream.from(result.getOutputArguments()))
				.forEach(tup -> tup._1.set(tup._2.getValue()));
		
		if ( m_elapsedTimePort != null ) {
			Duration elapsed = Duration.between(m_started, Instant.now());
			m_elapsedTimePort.setJsonNode(new TextNode(elapsed.toString()));
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
	
	private List<Port> reorderPorts(List<OperationVariable> varList, Map<String,Port> ports) {
		return FStream.from(varList)
						.map(opVar -> {
							String key = opVar.getValue().getIdShort();
							Preconditions.checkState(key != null, "OperationVariable idShort is null");
							Port port = ports.get(key);
							Preconditions.checkState(port != null, "OperationVariable's value is missing: {}", key);
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
}
