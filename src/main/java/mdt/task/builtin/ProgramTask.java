package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import utils.InternalException;
import utils.KeyValue;
import utils.KeyedValueList;
import utils.Throwables;
import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.CommandExecution;
import utils.async.CommandVariable;
import utils.async.CommandVariable.FileVariable;
import utils.async.Guard;
import utils.func.FOption;
import utils.io.FileUtils;
import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.aas.DataTypes;
import mdt.client.operation.OperationUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.AASFile;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.ref.MDTInstanceManagerAwareReference;
import mdt.model.sm.value.PropertyValue;
import mdt.task.MDTTask;
import mdt.task.OperationExecutionContext;
import mdt.task.Parameter;
import mdt.task.TaskException;


/**
 * 실행 파일 프로그램을 실행하는 태스크.
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class ProgramTask extends AbstractThreadedExecution<Map<String,SubmodelElement>>
							implements MDTTask, CancellableWork  {
	private static final Logger s_logger = LoggerFactory.getLogger(ProgramTask.class);

	private volatile MDTInstanceManager m_manager;
	private final ProgramOperationDescriptor m_descriptor;
	
	private final Map<String,Parameter> m_inputParameters;
	private final Map<String,Parameter> m_outputParameters;
	private Map<String,SubmodelElement> m_outputValues;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private CommandExecution m_cmdExec;
	
	public ProgramTask(ProgramOperationDescriptor descriptor) {
		Preconditions.checkNotNull(descriptor);

		m_descriptor = descriptor;
		m_inputParameters = FStream.from(m_descriptor.getInputParameters())
                                    .toMap(Parameter::getName);
		m_outputParameters = FStream.from(m_descriptor.getOutputParameters())
				                    .toMap(Parameter::getName);	
	}
	
	public void setMDTInstanceManager(MDTInstanceManager manager) {
		m_manager = manager;
	}
	
	public ProgramOperationDescriptor getOperationDescriptor() {
		return m_descriptor;
	}
	
	public void setWorkingDirectory(File dir) {
		Preconditions.checkArgument(dir != null && dir.isDirectory(), "invalid working directory: {}", dir);
		m_descriptor.setWorkingDirectory(dir);
	}
	
	public Map<String,Parameter> getInputParameters() {
        return m_inputParameters;
	}
	
	public Map<String, Parameter> getOutputParameters() {
		return m_outputParameters;
	}
	
	public void addOrReplaceInputParameter(Parameter param) {
		m_descriptor.getInputParameters().addOrReplace(param);
	}
	
	public void addOrReplaceOutputParameter(Parameter param) {
		m_descriptor.getOutputParameters().addOrReplace(param);
	}
	
	public Map<String,SubmodelElement> getOutputValues() {
		Preconditions.checkState(m_outputValues != null, "Task has not been completed");
		
		return m_outputValues;
	}

	@Override
	public void run(MDTInstanceManager manager)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		m_manager = manager;
		
		try {
			run();
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			
			Throwables.throwIfInstanceOf(cause, TimeoutException.class);
			throw new TaskException(cause);
		}
		catch ( Throwable e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException(cause);
		}
	}

	@Override
	public boolean cancel() {
		return cancel(true);
	}

	@Override
	public boolean cancelWork() {
		m_guard.runAndSignalAll(() -> {
			if ( m_cmdExec != null ) {
				m_cmdExec.cancel(true);
			}
		});
		
		return true;
	}

	@Override
	protected Map<String,SubmodelElement> executeWork() throws InterruptedException, CancellationException,
																TimeoutException, Exception {
		Instant started = Instant.now();

		OperationExecutionContext context = buildContext(m_manager);
		m_guard.runAndSignalAll(() -> {
			if ( m_cmdExec != null ) {
				throw new IllegalStateException("Task has already started");
			}
			
			m_cmdExec = buildCommandExecution(context);
		});
		
		try {
			m_cmdExec.executeWork();
	
			Map<String,CommandVariable> cmdVarMap = m_cmdExec.getVariableMap();
			Map<String,SubmodelElement> outputValues = Maps.newHashMap();
			
			// CommandVariable들 중에서 output parameter의 이름과 동일한 varaible의
			// 값을 해당 parameter의 SubmodelElement을 갱신시킨다.
			FStream.from(context.getOutputParameters())
					.innerJoin(FStream.from(cmdVarMap), Parameter::getName, KeyValue::key)
					.forEach(match -> {
						Parameter target = match._1;
						CommandVariable cmdVar = match._2.value();

						try {
							SubmodelElement updated = target.getReference().updateWithRawString(cmdVar.getValue());
							if ( s_logger.isInfoEnabled() ) {
								s_logger.info("update output parameter[{}]: {}",
												target.getName(), cmdVar.getValue());
							}
							
							outputValues.put(target.getName(), updated);
						}
						catch ( IOException e ) {
							s_logger.error("Failed to update output parameter[{}]: {}, cause={}",
											target.getName(), cmdVar.getValue(), e);
						}
					});
			
			// LastExecutionTime 정보가 제공된 경우 task의 수행 시간을 계산하여 해당 SubmodelElement를 갱신한다.
			Duration execTime = Duration.between(started, Instant.now());
			if ( context.getLastExecutionTimeReference() != null ) {
				try {
					String execTimeStr = DataTypes.DURATION.toString(execTime);
					context.getLastExecutionTimeReference().update(new PropertyValue(execTimeStr));
				}
				catch ( ResourceNotFoundException | IOException expected ) {
					s_logger.warn("Failed to update 'LastExecutionTime', cause=" + expected);
				}
			}
			
			return m_outputValues = outputValues;
		}
		finally {
			m_cmdExec.close();
		}
	}

	private OperationExecutionContext buildContext(MDTInstanceManager manager) {
		OperationExecutionContext execContext;
		if ( m_descriptor.getSubmodelReference() != null ) {
			DefaultSubmodelReference smeRef = DefaultSubmodelReference.parseString(m_descriptor.getSubmodelReference());
			smeRef.activate(manager);
			
			execContext = OperationUtils.loadSubmodelExecutionContext(smeRef);
		}
		else {
			execContext = new OperationExecutionContext();
		}
		
		for ( Parameter param: m_descriptor.getInputParameters() ) {
			if ( param.getReference() instanceof MDTInstanceManagerAwareReference aware ) {
				aware.activate(manager);
			}
			execContext.getInputParameters().addOrReplace(param);
		}
		for ( Parameter param: m_descriptor.getOutputParameters() ) {
			if ( param.getReference() instanceof MDTInstanceManagerAwareReference aware ) {
				aware.activate(manager);
			}
			
			// 만일 output parameter와 동일한 이름의 parameter가 input parameter에도 존재하는 경우에는
			// 'output' parameter가 input parameter도 포함하기 때문에 같이 갱신한다.
			if ( execContext.getInputParameters().containsKey(param.getName()) ) {
				execContext.getInputParameters().addOrReplace(param);
			}
			execContext.getOutputParameters().addOrReplace(param);
		}
		
		return execContext;
	}

	private CommandExecution buildCommandExecution(OperationExecutionContext context) {
		File workingDir = FOption.getOrElse(m_descriptor.getWorkingDirectory(),
											FileUtils::getCurrentWorkingDirectory);
		
		CommandExecution.Builder builder = CommandExecution.builder()
															.addCommand(m_descriptor.getCommandLine())
															.setWorkingDirectory(workingDir)
															.setTimeout(m_descriptor.getTimeout());
		
		KeyedValueList<String, Parameter> parameters = KeyedValueList.newInstance(Parameter::getName);
		FStream.from(context.getInputParameters())
				.concatWith(FStream.from(context.getOutputParameters()))
				.filterNot(p -> parameters.containsKey(p.getName()))
				.forEach(parameters::add);
		FStream.from(parameters)
				.mapOrThrow(param -> newCommandVariable(workingDir, param))
				.forEachOrThrow(builder::addVariable);

		// stdout/stderr redirection
		builder.redirectErrorStream();
		builder.redictStdoutToFile(new File(workingDir, "output.log"));
		
		return builder.build();
	}
	
	private FileVariable newCommandVariable(File workingDir, Parameter param) {
		File file = null;
		try {
			SubmodelElement data = param.getReference().read();
			
			if ( data instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
				MDTElementReference dref = (MDTElementReference)param.getReference();
				AASFile mdtFile = dref.getSubmodelService().getFileByPath(dref.getElementPath());
				
				String fileName = String.format("%s.%s", param.getName(),
														FilenameUtils.getExtension(mdtFile.getPath()));
				file = new File(workingDir, fileName);
				IOUtils.toFile(mdtFile.getContent(), file);
				
				return new FileVariable(param.getName(), file);
			}
			else if ( data instanceof Property ) {
				// PropertyValue인 경우, 바로 JSON으로 출력하면 double-quote가 추가되기 때문에
				// 이를 막기 위해 값을 직접 저장한다.
				file = new File(workingDir, param.getName());
				String extStr = OperationUtils.toExternalString(data);
				IOUtils.toFile(extStr, StandardCharsets.UTF_8, file);
				
				return new FileVariable(param.getName(), file);
			}
			else {
				throw new IllegalArgumentException("Unsupported Port data type: " + data.getClass());
			}
		}
		catch ( IOException e ) {
			throw new InternalException("Failed to write value to file: name=" + param.getName()
										+ ", path=" + file.getAbsolutePath(), e);
		}
	}
}
