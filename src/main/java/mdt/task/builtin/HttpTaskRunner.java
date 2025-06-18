package mdt.task.builtin;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpTaskRunner extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpTaskRunner.class);
	
	@Parameters(index="0", arity = "1..*",  paramLabel="json", description="Task descriptor (JSoN string)")
	private List<String> m_encodedTaskDescriptorChunks;
	
	public HttpTaskRunner() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		Instant started = Instant.now();
		
		MDTInstanceManager manager = mdt.getInstanceManager();

		TaskDescriptor descriptor = TaskDescriptor.parseEncodedString(m_encodedTaskDescriptorChunks);
		HttpTask httpTask = new HttpTask(descriptor);
		httpTask.run(manager);
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("HttpTask: elapsedTime={}", elapsed);
		}
	}

	public static void main(String... args) throws Exception {
		main(new HttpTaskRunner(), args);
		System.exit(0);
	}
}
