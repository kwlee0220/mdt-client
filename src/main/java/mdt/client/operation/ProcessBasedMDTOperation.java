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
import utils.thread.Guard;
import utils.io.IOUtils;
import utils.stream.FStream;
import utils.func.Nullables;

/**
 * 외부 프로그램(프로세스)을 실행하여 MDT 연산을 수행하는 비동기 실행.
 * <p>
 * 연산의 입력/출력 인자는 파일({@link FileArgument})로 전달된다. {@link Builder#addFileArgument}로
 * 등록된 인자는 등록 시점에 파일로 기록되고, 설정에 따라 그 파일 경로가 명령행에 추가된다.
 * 프로세스가 성공적으로 종료되면 출력 인자 파일들의 내용을 읽어 인자 이름을 키로 하는 맵으로 반환한다.
 * 실행이 끝나면(성공/실패와 관계없이) 모든 인자 파일은 삭제된다.
 * <p>
 * 작업 디렉토리가 지정된 경우에는 프로세스의 표준 출력과 표준 오류가 각각 그 디렉토리의
 * {@code stdout.log}, {@code stderr.log} 파일로 기록된다.
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

	/**
	 * 설정된 명령으로 프로세스를 실행하고 종료될 때까지 대기한 뒤, 출력 인자 파일의 내용을 반환한다.
	 * <p>
	 * 제한 시간이 설정되지 않은 경우에는 프로세스의 종료 코드가 0일 때만 성공으로 간주한다.
	 * 제한 시간이 설정된 경우에는 제한 시간 내에 종료되기만 하면 종료 코드와 관계없이 출력을 수집하며,
	 * 제한 시간이 경과하면 프로세스를 강제 종료한다.
	 *
	 * @return	출력 인자 이름을 키로, 출력 파일 내용을 값으로 하는 맵.
	 * @throws InterruptedException	프로세스 종료 대기 중 인터럽트된 경우.
	 * @throws CancellationException	프로세스 종료 시점에 실행이 취소된 상태인 경우.
	 * @throws TimeoutException	제한 시간 내에 프로세스가 종료되지 않은 경우.
	 * @throws ExecutionException	프로세스 시작 또는 출력 파일 읽기에 실패했거나, 종료 코드가 0이 아닌 경우.
	 * @throws IllegalArgumentException	지정된 작업 디렉토리가 디렉토리가 아닌 경우.
	 */
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
			Process process = m_guard.getChecked(() -> m_process = builder.start());
			int retCode;
			if ( m_timeout != null ) {
				boolean completed = process.waitFor(m_timeout.toMillis(), TimeUnit.MILLISECONDS);
				checkAfterTerminated();
				if ( !completed ) {
					// 제한시간이 경과한 경우
					process.destroyForcibly();
					throw new TimeoutException(m_timeout.toString());
				}
				retCode = process.exitValue();
			}
			else {
				retCode = process.waitFor();
				checkAfterTerminated();
			}
			
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
		catch ( IOException e ) {
			throw new ExecutionException("Failed to start process: command-line: " + m_command, e);
		}
		finally {
			cleanArgFiles();
		}
	}

	/**
	 * 실행 중인 프로세스에 종료를 요청한다 ({@link Process#destroy()}).
	 * <p>
	 * 프로세스가 아직 시작되지 않았더라도 항상 {@code true}를 반환한다.
	 *
	 * @return	항상 {@code true}.
	 */
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
						.mapToKeyValueOrThrow(fa -> KeyValue.of(fa.getName(), fa.read()))
						.toMap();
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
	
	/**
	 * 파일을 통해 전달되는 연산 인자.
	 * <p>
	 * 인자 이름, 인자 값을 담은 파일, 출력 인자 여부로 구성된다.
	 */
	public static class FileArgument {
		private final String m_name;
		private final File m_argFile;
		private final boolean m_isOutput;
		
		/**
		 * 파일 인자를 생성한다.
		 *
		 * @param name	인자 이름.
		 * @param argFile	인자 값을 담은 파일.
		 * @param isOutput	출력 인자 여부.
		 */
		public FileArgument(String name, File argFile, boolean isOutput) {
			m_name = name;
			m_argFile = argFile;
			m_isOutput = isOutput;
		}
		
		/**
		 * 인자 이름을 반환한다.
		 *
		 * @return	인자 이름.
		 */
		public String getName() {
			return m_name;
		}
		
		/**
		 * 인자 값을 담은 파일을 반환한다.
		 *
		 * @return	인자 파일.
		 */
		public File getFile() {
			return m_argFile;
		}
		
		/**
		 * 출력 인자 여부를 반환한다.
		 *
		 * @return	출력 인자이면 {@code true}, 입력 인자이면 {@code false}.
		 */
		public boolean isOutput() {
			return m_isOutput;
		}
		
		/**
		 * 인자 파일의 내용을 문자열로 읽어 반환한다.
		 *
		 * @return	파일 내용.
		 * @throws IOException	파일 읽기에 실패한 경우.
		 */
		public String read() throws IOException {
			try {
				return IOUtils.readString(m_argFile);
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
	
	/**
	 * {@link ProcessBasedMDTOperation} 객체를 생성하기 위한 빌더를 반환한다.
	 *
	 * @return	새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link ProcessBasedMDTOperation} 객체 생성을 위한 빌더.
	 * <p>
	 * 파일 인자는 {@link #addFileArgument(String, String, boolean)} 호출 시점의 작업 디렉토리를 기준으로
	 * 생성되므로, 작업 디렉토리는 파일 인자를 추가하기 전에 설정해야 한다.
	 */
	public static final class Builder {
		private List<String> m_command = Lists.newArrayList();
		private File m_workingDirectory;
		private List<FileArgument> m_fileArguments = Lists.newArrayList();
		private Duration m_timeout;
		private boolean m_addPortFileToCommandLine = true;
		
		/**
		 * 설정된 값으로 {@link ProcessBasedMDTOperation} 객체를 생성한다.
		 *
		 * @return	생성된 {@link ProcessBasedMDTOperation} 객체.
		 */
		public ProcessBasedMDTOperation build() {
			return new ProcessBasedMDTOperation(this);
		}
		
		/**
		 * 실행할 명령(프로그램과 고정 인자들)을 명령행의 앞에 추가한다.
		 * <p>
		 * 기존 명령행을 대체하지 않으며, 이미 추가된 인자들 앞에 삽입된다.
		 *
		 * @param command	명령행 앞에 삽입할 문자열 목록.
		 * @return	빌더 자신.
		 */
		public Builder setCommand(List<String> command) {
			m_command.addAll(0, command);
			return this;
		}
		
		/**
		 * 실행할 프로그램을 명령행의 맨 앞에 추가한다.
		 * <p>
		 * 기존 명령행을 대체하지 않으며, 이미 추가된 인자들 앞에 삽입된다.
		 *
		 * @param command	실행할 프로그램.
		 * @return	빌더 자신.
		 */
		public Builder setCommand(String command) {
			m_command.add(0, command);
			return this;
		}
		
		/**
		 * 파일 인자 추가 시 그 파일 경로를 명령행 인자로도 추가할지 여부를 설정한다.
		 * <p>
		 * 기본값은 {@code true}이며, 이후에 추가되는 파일 인자에만 적용된다.
		 *
		 * @param flag	{@code true}이면 파일 경로를 명령행에 추가.
		 * @return	빌더 자신.
		 */
		public Builder addPortFileToCommandLine(boolean flag) {
			m_addPortFileToCommandLine = flag;
			return this;
		}
		
		/**
		 * 프로세스의 작업 디렉토리를 설정한다.
		 * <p>
		 * 설정된 경우 파일 인자와 {@code stdout.log}/{@code stderr.log}가 이 디렉토리에 생성된다.
		 *
		 * @param dir	작업 디렉토리.
		 * @return	빌더 자신.
		 */
		public Builder setWorkingDirectory(File dir) {
			m_workingDirectory = dir;
			return this;
		}
		
		/**
		 * 프로세스 수행 제한 시간을 설정한다.
		 *
		 * @param timeout	제한 시간. {@code null}이면 무제한.
		 * @return	빌더 자신.
		 */
		public Builder setTimeout(Duration timeout) {
			m_timeout = timeout;
			return this;
		}
		
		/**
		 * 명령행 끝에 인자를 추가한다.
		 *
		 * @param arg	추가할 인자.
		 * @return	빌더 자신.
		 */
		public Builder addArgument(String arg) {
			m_command.add(arg);
			return this;
		}

		/**
		 * 파일로 전달되는 인자를 추가한다.
		 * <p>
		 * 작업 디렉토리(설정되지 않은 경우 현재 디렉토리) 아래에 {@code argName} 이름의 파일을 만들고
		 * {@code argValue}를 UTF-8로 기록한다 ({@code null}이면 빈 문자열).
		 * {@link #addPortFileToCommandLine(boolean)}이 {@code true}이면 파일 경로를 명령행 끝에 추가한다.
		 *
		 * @param argName	인자 이름. 파일 이름으로도 사용된다.
		 * @param argValue	인자 값.
		 * @param output	출력 인자 여부.
		 * @return	빌더 자신.
		 * @throws IllegalArgumentException	인자 파일 생성 또는 기록에 실패한 경우.
		 */
		public Builder addFileArgument(String argName, String argValue, boolean output) {
			try {
				File file = toFile(argName);
				if ( file.getParentFile() != null ) {
					file.getParentFile().mkdirs();
				}
				
				argValue = Nullables.orElse(argValue, "");
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
		
		/**
		 * 명령행 끝에 {@code --name value} 형태의 옵션을 추가한다.
		 *
		 * @param name	옵션 이름 ({@code --} 제외).
		 * @param value	옵션 값.
		 * @return	빌더 자신.
		 */
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
