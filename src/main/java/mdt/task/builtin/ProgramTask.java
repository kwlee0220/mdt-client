package mdt.task.builtin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.InternalException;
import utils.Throwables;
import utils.UnitUtils;
import utils.async.Guard;
import utils.async.command.CommandExecution;
import utils.async.command.CommandVariable;
import utils.async.command.CommandVariable.FileVariable;
import utils.io.FileUtils;
import utils.io.IOUtils;
import utils.stream.KeyValueFStream;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;
import mdt.task.AbstractMDTTask;
import mdt.task.TaskException;
import mdt.workflow.model.ArgumentSpec;
import mdt.workflow.model.ArgumentSpec.ReferenceArgumentSpec;
import mdt.workflow.model.TaskDescriptor;


/**
 * 실행 파일 프로그램을 실행하는 태스크.
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class ProgramTask extends AbstractMDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(ProgramTask.class);
	
	public static final String OPTION_WORKING_DIRECTORY = "workingDirectory";
	public static final String OPTION_TIMEOUT = "timeout";
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private CommandExecution m_cmdExec;
	
	public ProgramTask(TaskDescriptor descriptor) {
		super(descriptor);
		
		setLogger(s_logger);
	}

	@Override
	protected Map<String,SubmodelElement> invoke(MDTInstanceManager manager,
												Map<String,SubmodelElement> inputArguments,
												Map<String,SubmodelElement> outputArguments)
		throws TimeoutException, InterruptedException, CancellationException, TaskException {
		m_guard.runChecked(() -> {
			if ( m_cmdExec != null ) {
				throw new IllegalStateException("Task has already started");
			}
			m_cmdExec = buildCommandExecution();
		});
		try {
			m_cmdExec.run();

			// CommandExecution이 정상적으로 종료된 경우:
			// CommandVariable들 중에서 output parameter의 이름과 동일한 varaible의
			// 값을 해당 output arguments의 SubmodelElement을 갱신시킨다.
			for ( Map.Entry<String, SubmodelElement> ent : outputArguments.entrySet() ) {
				String argId = ent.getKey();
				CommandVariable cmdVar = m_cmdExec.getVariableMap().get(argId);
				if ( cmdVar != null ) {
					try {
						ElementValues.updateWithValueJsonString(ent.getValue(), cmdVar.getValue());
					}
					catch ( IOException e ) {
						throw new IOException("Failed to update output argument: argId=" + argId
														+ ", cause=" + e.getMessage(), e);
					}
				}
			}
			
			return outputArguments;
		}
		catch ( InterruptedException | CancellationException e ) {
			throw e;
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
			throw new TaskException("Failed to execute program task: " + cause.getMessage(), cause);
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to execute program task: " + e.getMessage(), e);
		}
		finally {
			m_cmdExec.close();
		}
	}

	@Override
	public boolean cancel() {
		m_guard.lock();
		try {
			if ( m_cmdExec != null ) {
				return m_cmdExec.cancel(true);
			}
			return false;
		}
		finally {
			m_guard.unlock();
		}
	}

	private CommandExecution buildCommandExecution() throws TaskException {
		// TaskDescriptor에서 필요한 정보를 읽어 CommandExecution을 생성한다.
		//
		TaskDescriptor descriptor = getTaskDescriptor();
		
		File workingDir = descriptor.findOptionValue("workingDirectory")
									.map(File::new)
									.orElseGet(FileUtils::getCurrentWorkingDirectory);
		List<String> commandLine = descriptor.findOptionValue("commandLine")
									.map(v -> Arrays.asList(v.split("\n")))
									.orElseThrow(() -> new IllegalArgumentException("Option is not specified: commandLine"));
		Duration timeout = descriptor.findOptionValue("timeout")
									.map(UnitUtils::parseDuration)
									.orElse(null);
		
		CommandExecution.Builder builder = CommandExecution.builder()
															.addCommand(commandLine)
															.setWorkingDirectory(workingDir)
															.setTimeout(timeout);
		
		KeyValueFStream.from(descriptor.getInputArgumentSpecs())
						.map((argId, argSpec) -> {
							try {
                                return newCommandVariable(workingDir, argId, argSpec);
                            }
                            catch ( TaskException e ) {
                            	Throwables.sneakyThrow(e);
                            	throw new AssertionError();
                            }
						})
						.forEachOrThrow(builder::addVariable);
		KeyValueFStream.from(descriptor.getOutputArgumentSpecs())
						.map((argId, argSpec) -> {
							try {
                                return newCommandVariable(workingDir, argId, argSpec);
                            }
                            catch ( TaskException e ) {
                            	Throwables.sneakyThrow(e);
                            	throw new AssertionError();
                            }
						})
						.forEachOrThrow(builder::addVariable);

		// stdout/stderr redirection
		builder.redirectErrorStream();
		builder.redirectStdoutToFile(new File(workingDir, "output.log"));
		
		return builder.build();
	}
	
	private FileVariable newCommandVariable(File workingDir, String argId, ArgumentSpec arg) throws TaskException {
		File file = null;
		try {
			ElementValue value = arg.readValue();
			
			if ( value instanceof FileValue ) {
				if ( arg instanceof ReferenceArgumentSpec refArg ) {
					MDTElementReference dref = (MDTElementReference)refArg.getElementReference();
					
					FileValue fv = dref.readAASFileValue();
					String fileName = String.format("%s.%s", argId, FilenameUtils.getExtension(fv.getValue()));
					file = new File(workingDir, fileName);
					dref.readAttachment(file);
					
					return new FileVariable(argId, file);
				}
				else {
					throw new TaskException("Argument should be a FileValue: arg=" + argId);
				}
			}
			else {
				file = new File(workingDir, argId);
				IOUtils.toFile(value.toValueJsonString(), StandardCharsets.UTF_8, file);
				
				return new FileVariable(argId, file);
			}
		}
		catch ( IOException e ) {
			throw new InternalException("Failed to write value to file: name=" + argId
										+ ", path=" + file.getAbsolutePath(), e);
		}
	}
}
