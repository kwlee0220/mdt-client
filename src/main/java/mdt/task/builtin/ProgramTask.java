package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.InternalException;
import utils.KeyedValueList;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.AbstractThreadedExecution;
import utils.async.CancellableWork;
import utils.async.CommandExecution;
import utils.async.CommandVariable;
import utils.async.CommandVariable.FileVariable;
import utils.async.Guard;
import utils.io.FileUtils;
import utils.io.IOUtils;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.AASFile;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.FileValue;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.Variable;
import mdt.task.MDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.MultiLineOption;
import mdt.workflow.model.TaskDescriptor;


/**
 * 실행 파일 프로그램을 실행하는 태스크.
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class ProgramTask extends AbstractThreadedExecution<Void> implements MDTTask, CancellableWork  {
	private static final Logger s_logger = LoggerFactory.getLogger(ProgramTask.class);
	
	public static final String OPTION_WORKING_DIRECTORY = "workingDirectory";
	public static final String OPTION_TIMEOUT = "timeout";
	
	private final TaskDescriptor m_descriptor;
	@GuardedBy("m_manager") private MDTInstanceManager m_manager;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private CommandExecution m_cmdExec;
	
	public ProgramTask(TaskDescriptor descriptor) {
		m_descriptor = descriptor;
		
		setLogger(s_logger);
	}

	@Override
	public TaskDescriptor getTaskDescriptor() {
		return m_descriptor;
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
		return cancelWork();
	}

	@Override
	public boolean cancelWork() {
		m_guard.run(() -> {
			if ( m_cmdExec != null ) {
				m_cmdExec.cancel(true);
			}
		});
		
		return true;
	}
	
	@Override
	protected Void executeWork() throws InterruptedException, CancellationException, TimeoutException, Exception {
		Instant started = Instant.now();
		
		try {
			m_guard.runChecked(() -> {
				if ( m_cmdExec != null ) {
					throw new IllegalStateException("Task has already started");
				}
				m_cmdExec = buildCommandExecution();
			});
			
			try {
				m_cmdExec.executeWork();

				// CommandExecution이 정상적으로 종료된 경우:
				// CommandVariable들 중에서 output parameter의 이름과 동일한 varaible의
				// 값을 해당 parameter의 SubmodelElement을 갱신시킨다.
				updateOutputVariables();
			}
			finally {
				m_cmdExec.close();
			}
		}
		catch ( TaskException | TimeoutException | InterruptedException | CancellationException e ) {
			throw e;
		}
		
		// LastExecutionTime 정보가 제공된 경우 task의 수행 시간을 계산하여 해당 SubmodelElement를 갱신한다.
		TaskUtils.updateLastExecutionTime(m_manager, m_descriptor, started, getLogger());
		
		return null;
	}

	private CommandExecution buildCommandExecution() throws TaskException {
		// TaskDescriptor에서 필요한 정보를 읽어 CommandExecution을 생성한다.
		//
		TaskDescriptor descriptor = getTaskDescriptor();
		
		File workingDir = descriptor.findStringOption("workingDirectory")
									.map(File::new)
									.getOrElse(FileUtils::getCurrentWorkingDirectory);
		List<String> commandLine = descriptor.findOption("commandLine", MultiLineOption.class)
									.map(MultiLineOption::getValue)
									.getOrThrow(() -> new IllegalArgumentException("Option is not specified: commandLine"));
		Duration timeout = descriptor.findStringOption("timeout")
									.map(UnitUtils::parseDuration)
									.getOrNull();
		
		CommandExecution.Builder builder = CommandExecution.builder()
															.addCommand(commandLine)
															.setWorkingDirectory(workingDir)
															.setTimeout(timeout);
		
		KeyedValueList<String, Variable> variables = KeyedValueList.with(Variable::getName);
		FStream.from(descriptor.getInputVariables())
				.concatWith(FStream.from(descriptor.getOutputVariables()))
				.filterNot(p -> variables.containsKey(p.getName()))
				.forEach(variables::add);
		FStream.from(variables)
				.mapOrThrow(var -> newCommandVariable(workingDir, var))
				.forEachOrThrow(builder::addVariable);

		// stdout/stderr redirection
		builder.redirectErrorStream();
		builder.redictStdoutToFile(new File(workingDir, "output.log"));
		
		return builder.build();
	}
	
	private FileVariable newCommandVariable(File workingDir, Variable var) throws TaskException {
		File file = null;
		try {
			ElementValue value = var.readValue();
			
			if ( value instanceof FileValue ) {
				if ( var instanceof ReferenceVariable refVar ) {
					MDTElementReference dref = (MDTElementReference) refVar.getReference();
					AASFile mdtFile = dref.getSubmodelService().getFileByPath(dref.getIdShortPathString());

					String fileName = String.format("%s.%s", var.getName(),
													FilenameUtils.getExtension(mdtFile.getPath()));
					file = new File(workingDir, fileName);
					IOUtils.toFile(mdtFile.getContent(), file);

					return new FileVariable(var.getName(), file);
				}
				else {
					throw new TaskException("TaskPort should be a ReferencePort: port=" + var);
				}
			}
			else {
				// PropertyValue인 경우, 바로 JSON으로 출력하면 double-quote가 추가되기 때문에
				// 이를 막기 위해 값을 직접 저장한다.
				file = new File(workingDir, var.getName());
				String extStr = (value instanceof PropertyValue pvalue)
								? pvalue.get() : MDTModelSerDe.toJsonString(value);
				IOUtils.toFile(extStr, StandardCharsets.UTF_8, file);
				
				return new FileVariable(var.getName(), file);
			}
		}
		catch ( IOException e ) {
			throw new InternalException("Failed to write value to file: name=" + var.getName()
										+ ", path=" + file.getAbsolutePath(), e);
		}
	}
	
	private void updateOutputVariables() {
		TaskDescriptor descriptor = getTaskDescriptor();
		
		for ( Variable outVar: descriptor.getOutputVariables() ) {
			// Output port와 동일 이름을 가진 command variable을 찾는다.
			CommandVariable cmdVar = m_cmdExec.getVariableMap().get(outVar.getName());
			if ( cmdVar == null ) {
				continue;
			}
			
			// 동일 이름을 command-variable이 검색된 경우.
			try {
				outVar.updateWithRawString(cmdVar.getValue());
				if ( getLogger().isInfoEnabled() ) {
					getLogger().info("Updated: output variable[{}]: {}", outVar.getName(), cmdVar.getValue());
				}
			}
			catch ( IOException e ) {
				getLogger().error("Failed to update output parameter[{}]: {}, cause={}",
									outVar.getName(), cmdVar.getValue(), e);
			}
		}
	}
}
