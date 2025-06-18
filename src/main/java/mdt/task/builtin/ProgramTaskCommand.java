package mdt.task.builtin;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;

import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.TaskDescriptors;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "program",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Program task execution command."
)
public class ProgramTaskCommand extends MultiVariablesCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskCommand.class);

	@Parameters(index="0", arity="1", paramLabel="path", description="Path to the operation descriptor")
	private File m_opDescFile;
	
	@Option(names={"--workingDirectory"}, paramLabel="path", description="Working directory")
	private File m_workingDir;
	
	@Option(names={"--timeout"}, paramLabel="duration", description="Invocation timeout (e.g. \"30s\", \"1m\")")
	private String m_timeout = null;
	
	public ProgramTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();

		Instant started = Instant.now();
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(ProgramTask.class.getName());

		if ( m_opDescFile != null ) {
			ProgramOperationDescriptor opDesc = ProgramOperationDescriptor.load(m_opDescFile, MDTModelSerDe.MAPPER);
			TaskDescriptors.update(manager, descriptor, opDesc);
		}
		FOption.accept(m_workingDir, dir -> descriptor.addOrReplaceOption(ProgramTask.OPTION_WORKING_DIRECTORY, dir.getAbsolutePath()));
		FOption.accept(m_timeout, to -> descriptor.addOrReplaceOption(ProgramTask.OPTION_TIMEOUT, to));

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromArguments(manager, descriptor);

		ProgramTask task = new ProgramTask(descriptor);
		task.run(manager);
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("HttpTask: elapsedTime={}", elapsed);
		}
	}

	public static void main(String... args) throws Exception {
		main(new ProgramTaskCommand(), args);
	}
}
