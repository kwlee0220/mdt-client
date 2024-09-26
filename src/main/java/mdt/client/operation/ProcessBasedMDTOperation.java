package mdt.client.operation;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.KeyValue;
import utils.async.AbstractThreadedExecution;
import utils.async.AsyncState;
import utils.async.CancellableWork;
import utils.async.Guard;
import utils.func.FOption;
import utils.io.IOUtils;
import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ProcessBasedMDTOperation extends AbstractThreadedExecution<Map<String,String>>
										implements CancellableWork {
	private static final Logger s_logger = LoggerFactory.getLogger(ProcessBasedMDTOperation.class);

	private final List<String> m_command;
	private final File m_workingDirectory;
	private final List<FileArgument> m_fileArguments;
	private final Duration m_timeout;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private Process m_process;
	
	private ProcessBasedMDTOperation(Builder builder) {
		m_command = builder.m_command;
		m_workingDirectory = builder.m_workingDirectory;
		m_fileArguments = builder.m_fileArguments;
		m_timeout = builder.m_timeout;
		
		setLogger(s_logger);
	}

	@Override
	protected Map<String,String> executeWork() throws InterruptedException, CancellationException,
														TimeoutException, ExecutionException {
		ProcessBuilder builder = new ProcessBuilder(m_command);
		if ( m_workingDirectory != null ) {
			if ( !m_workingDirectory.isDirectory() ) {
				throw new IllegalArgumentException("Invalid working directory: " + m_workingDirectory);
			}
			builder.directory(m_workingDirectory);
			if ( getLogger().isDebugEnabled() ) {
				getLogger().debug("set working directory: " + m_workingDirectory);
			}
			
			File stdoutLogFile = new File(m_workingDirectory, "stdout.log");
			builder.redirectOutput(Redirect.to(stdoutLogFile));
			File stderrLogFile = new File(m_workingDirectory, "stderr.log");
			builder.redirectError(stderrLogFile);
		}
		
		try {
			if ( getLogger().isDebugEnabled() ) {
				String toStr = ( m_timeout != null ) ? ", timeout=" + m_timeout : "";
				getLogger().debug("starting program: {}{}", m_command, toStr);
			}
			Process process = m_guard.getOrThrow(() -> m_process = builder.start());
			if ( m_timeout != null ) {
				boolean completed = process.waitFor(m_timeout.toMillis(), TimeUnit.MILLISECONDS);
				checkAfterTerminated();
				if ( completed ) {
					// 제한시간 내에 성공적으로 마친 경우
					return collectOutput();
				}
				else {
					// 제한시간이 경과한 경우
					process.destroyForcibly();
					throw new TimeoutException(m_timeout.toString());
				}
			}
			else {
				int retCode = process.waitFor();
				checkAfterTerminated();
				if ( retCode == 0 ) {
					// 프로그램 수행이 성공적으로 마친 경우
					return collectOutput();
				}
				else {
					// 프로그램 수행이 실패한 경우
					throw new ExecutionException("Process failed: retCode=" + retCode,
													new Exception());
				}
			}
		}
		catch ( IOException e ) {
			throw new ExecutionException("Failed to start process: command-line: " + m_command, e);
		}
		finally {
			cleanArgFiles();
		}
	}

	@Override
	public boolean cancelWork() {
		return m_guard.get(() -> {
			if ( m_process != null ) {
				if ( getLogger().isInfoEnabled() ) {
					getLogger().debug("killing process: pid={}", m_process.toHandle().pid());
				}
				m_process.destroy();
			}
			return true;
		});
	}
	
	private Map<String,String> collectOutput() throws IOException {
		return FStream.from(m_fileArguments)
						.filter(FileArgument::isOutput)
						.mapOrThrow(fa -> KeyValue.of(fa.getName(), fa.read()))
						.toMap(KeyValue::key, KeyValue::value);
	}
	
	private void checkAfterTerminated() throws CancellationException {
		if ( getState() == AsyncState.CANCELLED ) {
			throw new CancellationException();
		}
	}
	
	private void cleanArgFiles() {
		FStream.from(m_fileArguments)
				.map(FileArgument::getFile)
				.forEach(File::delete);
	}
	
	public static class FileArgument {
		private final String m_name;
		private final File m_argFile;
		private final boolean m_isOutput;
		
		public FileArgument(String name, File argFile, boolean isOutput) {
			m_name = name;
			m_argFile = argFile;
			m_isOutput = isOutput;
		}
		
		public String getName() {
			return m_name;
		}
		
		public File getFile() {
			return m_argFile;
		}
		
		public boolean isOutput() {
			return m_isOutput;
		}
		
		public String read() throws IOException {
			try {
				return IOUtils.toString(m_argFile);
			}
			catch ( IOException e ) {
				throw new IOException("Failed to read output file: " + m_argFile + ", cause=" + e.getMessage());
			}
		}
		
		@Override
		public String toString() {
			String inoutStr = (m_isOutput) ? "out" : "in";
			return String.format("%s: %s (%s)", m_name, m_argFile, inoutStr);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static final class Builder {
		private List<String> m_command = Lists.newArrayList();
		private File m_workingDirectory;
		private List<FileArgument> m_fileArguments = Lists.newArrayList();
		private Duration m_timeout;
		private boolean m_addPortFileToCommandLine = true;
		
		public ProcessBasedMDTOperation build() {
			return new ProcessBasedMDTOperation(this);
		}
		
		public Builder setCommand(List<String> command) {
			m_command.addAll(0, command);
			return this;
		}
		
		public Builder setCommand(String command) {
			m_command.add(0, command);
			return this;
		}
		
		public Builder addPortFileToCommandLine(boolean flag) {
			m_addPortFileToCommandLine = flag;
			return this;
		}
		
		public Builder setWorkingDirectory(File dir) {
			m_workingDirectory = dir;
			return this;
		}
		
		public Builder setTimeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
		
		public Builder addArgument(String arg) {
			m_command.add(arg);
			return this;
		}

		public Builder addFileArgument(String argName, String argValue, boolean output) {
			try {
				File file = toFile(argName);
				if ( file.getParentFile() != null ) {
					file.getParentFile().mkdirs();
				}
				
				argValue = FOption.getOrElse(argValue, "");
				Files.writeString(file.toPath(), argValue, StandardCharsets.UTF_8);
				
				if ( m_addPortFileToCommandLine ) {
					m_command.add(file.getPath());
				}
				m_fileArguments.add(new FileArgument(argName, file, output));
				
				return this;
			}
			catch ( IOException e ) {
				String msg = String.format("Failed to create a file-argument: "
											+ "arg-name=%s, arg-value=%s, output=%s, cause=%s",
											argName, argValue, output, e);
				throw new IllegalArgumentException(msg);
			}
		}
		
		public Builder addOption(String name, String value) {
			m_command.add("--" + name);
			m_command.add(value);
			return this;
		}
		
		private File toFile(String name) {
			return (m_workingDirectory != null) ? new File(m_workingDirectory, name) : new File(name);
		}
	}
}
