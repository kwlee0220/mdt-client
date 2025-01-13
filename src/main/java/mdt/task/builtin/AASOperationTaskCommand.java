package mdt.task.builtin;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.UnitUtils;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.task.MultiParameterTaskCommand;
import mdt.task.Parameter;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class AASOperationTaskCommand extends MultiParameterTaskCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AASOperationTaskCommand.class);
	private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(3);

	private DefaultElementReference m_operationRef;
	@Parameters(index="0", arity="1", paramLabel="operation-ref",
				description="the mdt-reference to the target operation")
	public void setOperation(String refString) {
		m_operationRef = DefaultElementReference.parseString(refString);
	}

	@Option(names={"--async"}, description="invoke asynchronously")
	private boolean m_async = true;

	private Duration m_poll = DEFAULT_POLL_INTERVAL;
	@Option(names={"--poll"}, paramLabel="duration", description="Status polling interval (e.g. \"1s\", \"500ms\"")
	public void setPollInterval(String intvStr) {
		m_poll = UnitUtils.parseDuration(intvStr);
	}

	@Option(names={"--update", "-u"}, description="update Operation variables")
	private boolean m_updateOperation = false;

	@Option(names={"--show", "-s"}, description="show output/inoutput variables")
	private boolean m_show = false;
	
	public AASOperationTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		AASOperationTask.Builder builder = AASOperationTask.builder()
															.operationReference(m_operationRef)
															.pollInterval(m_poll)
															.timeout(m_timeout)
															.updateOperation(m_updateOperation)
															.showOutputVariables(m_show);
		
		Tuple<List<Parameter>, List<Parameter>> paramsPair = loadParameters();
		FStream.from(paramsPair._1).forEach(builder::addInputParameter);
		FStream.from(paramsPair._2).forEach(builder::addOutputParameter);
		
		AASOperationTask task = builder.build();
		
		task.run(manager);
	}
	
	public static void main(String... args) throws Exception {
		main(new AASOperationTaskCommand(), args);
	}
}
