package mdt.task.builtin;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.TaskDescriptor;

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
public class ProgramTaskCommand extends AbstractTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskCommand.class);

	@Parameters(index="0", arity="1", paramLabel="path", description="Path to the operation descriptor")
	private File m_opDescFile;
	
	@Option(names={"--workingDirectory"}, paramLabel="path", description="Working directory")
	private File m_workingDir;
	
	public ProgramTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(ProgramTask.class.getName());
		loadTaskDescriptor(descriptor, manager);

		if ( m_opDescFile != null ) {
			ProgramOperationDescriptor opDesc = ProgramOperationDescriptor.load(m_opDescFile, MDTModelSerDe.MAPPER);
			update(manager, descriptor, opDesc);
		}
		FOption.accept(m_workingDir, dir -> descriptor.addOption(ProgramTask.OPTION_WORKING_DIRECTORY,
																			dir.getAbsolutePath()));

		ProgramTask task = new ProgramTask(descriptor);
		task.run(manager);
		
		Duration elapsed = Duration.between(started, Instant.now());
		getLogger().info("ProgramTask: elapsedTime={}", elapsed);
	}

	public static void main(String... args) throws Exception {
		main(new ProgramTaskCommand(), args);
	}
	
	private void update(MDTInstanceManager manager, TaskDescriptor descriptor, ProgramOperationDescriptor opDesc) {
		String multiLine = FStream.from(opDesc.getCommandLine()).join('\n');
		descriptor.addOption("commandLine", multiLine);
		FOption.accept(opDesc.getWorkingDirectory(), workDir -> {
			descriptor.addOption("workingDirectory", workDir.getAbsolutePath());
		});
		
		FStream.from(opDesc.getInputVariables()).forEach(descriptor.getInputVariables()::addOrReplace);
		FStream.from(opDesc.getOutputVariables()).forEach(descriptor.getOutputVariables()::addOrReplace);
		FOption.accept(opDesc.getTimeout(), to -> descriptor.addOption("timeout", to.toString()));
	}
}
