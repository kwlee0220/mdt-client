package mdt.task.builtin;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.task.TaskException;

import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class ProgramTaskCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskCommand.class);

	@Parameters(index="0", arity="1", paramLabel="path",
				description="Path to the program-based operation descriptor")
	private String m_opDescFilePath;
	
	public ProgramTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();

		File opDescFile = new File(m_opDescFilePath);
		try {
			ProgramOperationDescriptor descriptor = MDTModelSerDe.getJsonMapper()
																.readValue(opDescFile, ProgramOperationDescriptor.class);
			ProgramTask task = new ProgramTask(descriptor);
			task.run(manager);
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to read Task command file=" + opDescFile, e);
		}
	}

	public static void main(String... args) throws Exception {
		main(new ProgramTaskCommand(), args);
	}
}
