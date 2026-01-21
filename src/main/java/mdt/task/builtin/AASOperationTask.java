package mdt.task.builtin;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Throwables;
import utils.stream.KeyValueFStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.AASUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.task.AbstractMDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.TaskDescriptor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTask extends AbstractMDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTask.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);
	private static final javax.xml.datatype.Duration INFINITE = AASUtils.DATATYPE_FACTORY.newDuration("P7D");

	public static final String OPTION_OPERATION = "operation";
	
	private AASOperationClient m_opClient = null;
	
	public AASOperationTask(TaskDescriptor descriptor) {
		super(descriptor);
		
		setLogger(s_logger);
	}

	@Override
	protected Map<String,SubmodelElement> invoke(MDTInstanceManager manager,
												Map<String,SubmodelElement> inputArguments,
												Map<String,SubmodelElement> outputArguments)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		TaskDescriptor descriptor = getTaskDescriptor();
		try {
			MDTElementReference opRef
				= descriptor.findOptionValue(OPTION_OPERATION)
							.map(ElementReferences::parseExpr)
							.orElseThrow(() -> new IllegalArgumentException("Option[operation] is not provided"));
			opRef.activate(manager);
			
			Duration pollInterval = getPollInterval().orElse(DEFAULT_POLL_INTERVAL);
			m_opClient = new AASOperationClient(opRef, pollInterval);
	
			// TaskDescriptor에 기술된 input variable을 이용해서 입력 인자를 설정한다.
			Map<String,ElementValue> inputValues = KeyValueFStream.from(inputArguments)
																	.mapValue(ElementValues::getValue)
																	.toMap();
			m_opClient.setInputVariableValues(inputValues);
			
			// AASOperation을 실행하고 완료될 때까지 대기한다.
			m_opClient.run();

			// 출력 변수의 값을 수집된 출력 값으로 갱신한다.
			Map<String,ElementValue> outputValues = m_opClient.getOutputVariableValues();
			KeyValueFStream.from(outputValues)
							.match(outputArguments)
							.forEach((argId, match) -> {
								ElementValues.update(match._2, match._1);
							});
			getLogger().info("AASOperation completed: ref={}, out={}", opRef, outputValues);
			
			return outputArguments;
		}
		catch ( IOException e ) {
			throw new TaskException("fails to execute AASOperationTask", e);
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException("fails to execute AASOperationTask", cause);
		}
	}

	@Override
	public boolean cancel() {
		return false;
	}
}
