package mdt.task;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import utils.Throwables;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.client.operation.ProcessBasedMDTOperation;
import mdt.client.operation.ProcessBasedMDTOperation.Builder;
import mdt.model.instance.MDTInstanceManager;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class LegacyExecutableTask implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(LegacyExecutableTask.class);

	private List<String> m_command;
	private File m_workingDir;
	private Duration m_timeout;

	@Override
	public void setMDTInstanceManager(MDTInstanceManager manager) { }
	
	public void setCommand(List<String> command) {
		m_command = command;
	}
	
	public void setWorkingDirectory(File workingDir) {
		m_workingDir = workingDir;
	}

	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}

	@Override
	public void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options)
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		Preconditions.checkArgument(m_command != null && m_command.size() > 0);
		
		Builder opBuilder = ProcessBasedMDTOperation.builder()
													.setCommand(m_command);
		if ( m_workingDir != null ) {
			opBuilder.setWorkingDirectory(m_workingDir);
		}
		
		// option 정보를 command line의 option으로 추가시킨다.
		FStream.from(options)
				.forEach(kv -> opBuilder.addOption(kv.key(), kv.value()));
		
		FStream.from(inputPorts.values())
				.forEachOrThrow(port -> {
					// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
					String valueString = FOption.getOrElse("" + port.getRawValue(), "");
					opBuilder.addFileArgument(port.getName(), valueString, false);
				});
		FStream.from(Iterables.concat(inoutPorts.values(), outputPorts.values()))
				.forEachOrThrow(port -> {
					// port를 읽어 JSON 형식으로 변환한 후 지정된 file에 저장한다.
					String valueString = FOption.getOrElse("" + port.getRawValue(), "");
					opBuilder.addFileArgument(port.getName(), valueString, true);
				});
		if ( m_timeout != null ) {
			opBuilder.setTimeout(m_timeout);
		}
		ProcessBasedMDTOperation op = opBuilder.build();
		
		try {
			Map<String,String> outputs = op.run();
			FStream.from(inoutPorts)
					.forEach((key, port) -> FOption.accept(outputs.get(key), json -> port.setJson(json)));
			FStream.from(outputPorts)
					.forEach((key, port) -> FOption.accept(outputs.get(key), json -> port.setJson(json)));
		}
		catch ( Exception e ) {
			Throwables.throwIfInstanceOf(e, TimeoutException.class);
			Throwables.throwIfInstanceOf(e, ExecutionException.class);
			
			throw new ExecutionException(e);
		}
	}
	
	public static class Command extends MDTTaskCommand<LegacyExecutableTask> {
		@Option(names={"--command"}, paramLabel="string", required=true, description="command line")
		private List<String> m_command;

		@Option(names={"--workingDir"}, paramLabel="dir", required=false, description="current working directory")
		private File m_workingDir;

		@Override
		protected LegacyExecutableTask newTask() {
			LegacyExecutableTask task = new LegacyExecutableTask();
			task.setCommand(m_command);
			task.setWorkingDirectory(m_workingDir);
			task.setTimeout(m_timeout);
			
			return task;
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
