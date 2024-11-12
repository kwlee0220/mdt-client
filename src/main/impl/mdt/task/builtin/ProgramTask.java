package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
import com.google.common.collect.Lists;

import utils.InternalException;
import utils.KeyValue;
import utils.KeyedValueList;
import utils.Throwables;
import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.CommandExecution;
import utils.async.CommandExecution.FileVariable;
import utils.async.CommandExecution.Variable;
import utils.async.Guard;
import utils.func.FOption;
import utils.io.FileUtils;
import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.aas.DataTypes;
import mdt.aas.DefaultSubmodelReference;
import mdt.client.operation.OperationUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.MDTFile;
import mdt.model.sm.MDTInstanceManagerAwareReference;
import mdt.model.sm.MDTSubmodelElementReference;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;
import mdt.task.MDTTask;
import mdt.task.OperationExecutionContext;
import mdt.task.Parameter;
import mdt.task.TaskException;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class ProgramTask extends AbstractThreadedExecution<List<Parameter>> implements MDTTask, CancellableWork  {
	private static final Logger s_logger = LoggerFactory.getLogger(ProgramTask.class);

	private volatile MDTInstanceManager m_manager;
	private final ProgramOperationDescriptor m_descriptor;
	private List<Parameter> m_outputValues;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private CommandExecution m_cmdExec;
	
	public ProgramTask(ProgramOperationDescriptor descriptor) {
		Preconditions.checkNotNull(descriptor);

		m_descriptor = descriptor;
	}
	
	public void setMDTInstanceManager(MDTInstanceManager manager) {
		m_manager = manager;
	}
	
	public ProgramOperationDescriptor getOperationDescriptor() {
		return m_descriptor;
	}
	
	public void setWorkingDirectory(File dir) {
		Preconditions.checkArgument(dir != null && dir.isDirectory(),
									"invalid working directory: {}", dir);
		m_descriptor.setWorkingDirectory(dir);
	}
	
	public void addOrReplaceInputParameter(Parameter param) {
		m_descriptor.getInputParameters().addOrReplace(param);
	}
	
	public void addOrReplaceOutputParameter(Parameter param) {
		m_descriptor.getOutputParameters().addOrReplace(param);
	}
	
	public List<Parameter> getOutputValues() {
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
	protected List<Parameter> executeWork() throws InterruptedException, CancellationException,
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
	
			Map<String,Variable> cmdVarMap = m_cmdExec.getVariableMap();
			List<Parameter> outputValues = Lists.newArrayList();
			
			// CommandVariable들 중에서 output parameter의 이름과 동일한 varaible의
			// 값을 해당 parameter의 SubmodelElement에 반영시킨다.
			FStream.from(context.getOutputParameters())
					.innerJoin(FStream.from(cmdVarMap), Parameter::getName, KeyValue::key)
					.forEach(match -> {
						Parameter target = match._1;
						Variable cmdVar = match._2.value();

						try {
							if ( target.getReference() != null ) {
								target.getReference().updateWithExternalString(cmdVar.getValue());
								if ( s_logger.isInfoEnabled() ) {
									s_logger.info("update output parameter[{}]: {}",
													target.getName(), cmdVar.getValue());
								}
							}
							else {
								Preconditions.checkState(target.getElement() != null);
								ElementValues.updateWithExternalString(target.getElement(), cmdVar.getValue());
								outputValues.add(target);
							}
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
			SubmodelElement data = param.getElement();
			if ( data == null ) {
				data = param.getReference().read();
			}
			
			if ( data instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
				MDTSubmodelElementReference dref = (MDTSubmodelElementReference)param.getReference();
				MDTFile mdtFile = dref.getSubmodelService().getFileByPath(dref.getElementIdShortPath());
				
				String fileName = String.format("%s.%s", param.getName(), FilenameUtils.getExtension(mdtFile.getPath()));
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
