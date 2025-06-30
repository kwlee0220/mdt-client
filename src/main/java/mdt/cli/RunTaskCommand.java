package mdt.cli;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.model.MDTManager;
import mdt.model.Qualifiers;
import mdt.model.expr.MDTElementReferenceExpr;
import mdt.model.expr.MDTExpr;
import mdt.model.expr.MDTExprParser;
import mdt.model.expr.MDTSubmodelExpr;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.task.TaskException;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.AASOperationTaskCommand;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.MultiVariablesCommand;
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
	name = "task",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Task execution command."
)
public class RunTaskCommand extends MultiVariablesCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(RunTaskCommand.class);
	private static final String DEFAULT_POLL_INTERVAL = "1s";

	@Parameters(index="0", paramLabel="opRef",
				description="Operation reference (e.g. <instance-id>/<submodel-idshort>)")
	private String m_opRefExpr;

	@Option(names={"--sync"}, defaultValue="false", description="invoke synchronously")
	private boolean m_sync = false;
	
	public RunTaskCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		MDTExpr expr = MDTExprParser.parseExpr(m_opRefExpr);
		if ( expr instanceof MDTSubmodelExpr smExpr ) {
			DefaultSubmodelReference smRef = smExpr.evaluate();
			smRef.activate(manager);
			runSubmodelOperation(smRef);
		}
		else if ( expr instanceof MDTElementReferenceExpr elmExpr ) {
			MDTElementReference opElmRef = elmExpr.evaluate();
			opElmRef.activate(manager);
			runAASOperation(opElmRef);
		}
		else {
			throw new IllegalArgumentException("Invalid operation reference: " + m_opRefExpr);
		}
	}

	public static void main(String... args) throws Exception {
		main(new RunTaskCommand(), args);
		System.exit(0);
	}
	
	private void runAASOperation(MDTElementReference opElmRef)
			throws CancellationException, IOException, TimeoutException, InterruptedException, TaskException {
		Instant started = Instant.now();

		MDTInstanceManager manager = opElmRef.getInstance().getInstanceManager();
		Operation opElm = (Operation)opElmRef.read();
		
		List<Qualifier> qualifiers = opElm.getQualifiers();
		String pollInterval = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_POLL_INTERVAL)
								        	.getOrElse(DEFAULT_POLL_INTERVAL);
		String timeout = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_TIMEOUT)
						        	.getOrNull();
		String updateOpVar = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_UPDATE_OPVAR)
										.getOrElse("false");

		TaskDescriptor descriptor = new TaskDescriptor();
		descriptor.setType(AASOperationTaskCommand.class.getName());

		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
		loadTaskVariablesFromArguments(manager, descriptor);
		
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_OPERATION, opElmRef);
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_POLL_INTERVAL, pollInterval);
		if ( timeout != null ) {
			descriptor.addOrReplaceOption(AASOperationTask.OPTION_TIMEOUT, timeout);
		}
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_UPDATE_OPVARS, updateOpVar);
		descriptor.addOrReplaceOption(AASOperationTask.OPTION_SYNC, ""+m_sync);
		
		AASOperationTask aasOpTask = new AASOperationTask(descriptor);
		aasOpTask.run(manager);
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("AASOperationTask: ref={}, elapsedTime={}", opElmRef, elapsed);
		}
	}
	
	private void runSubmodelOperation(DefaultSubmodelReference smRef)
		throws CancellationException, IOException, TimeoutException, InterruptedException, TaskException {
		Instant started = Instant.now();
		
		MDTInstanceManager manager = smRef.getInstance().getInstanceManager();
		TaskDescriptor descriptor = TaskDescriptors.from(smRef);
		if ( descriptor.getType() == HttpTask.class.getName() ) {
			HttpTask httpTask = new HttpTask(descriptor);
			httpTask.run(manager);
		}
		
		Duration elapsed = Duration.between(started, Instant.now());
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("{}: elapsedTime={}", descriptor.getType(), elapsed);
		}
	}
	
//	private void runHttpOperation(MDTInstanceManager manager, MDTSubmodelReference smRef)
//		throws CancellationException, IOException, TimeoutException, InterruptedException, TaskException {
//		Instant started = Instant.now();
//		
//		Submodel submodel = smRef.get().getSubmodel();
//		List<Qualifier> qualifiers = submodel.getQualifiers();
//		String serverEndpoint
//				= Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_SERVER_ENDPOINT)
//							.getOrThrow(() -> new ModelValidationException("Submodel operation server endpoint not found: submodel idShort="
//																			+ submodel.getIdShort()));
//		String opId = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_OPERATION_ID)
//								.getOrThrow(() -> new ModelValidationException("Submodel operation id is missing: submodel idShort="
//																				+ submodel.getIdShort()));
//		Duration pollInterval = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_POLL_INTERVAL)
//								        	.map(UnitUtils::parseDuration)
//								        	.getOrElse(DEFAULT_POLL_TIMEOUT);
//		Duration timeout = Qualifiers.findQualifierByType(qualifiers, Qualifiers.QUALIFIER_TIMEOUT)
//						        	.map(UnitUtils::parseDuration)
//						        	.getOrNull();
//		
//		TaskDescriptor descriptor = new TaskDescriptor();
//		descriptor.setType(HttpTask.class.getName());
//		TaskDescriptors.loadVariablesFromSubmodel(descriptor, smRef);
//
//		Tuple<String,String> tup = Utilities.split(opId, '/');
//		ByIdShortSubmodelReference opSubmodelRef = ByIdShortSubmodelReference.ofIdShort(tup._1, tup._2);
//		opSubmodelRef.activate(manager);
//		
//		descriptor.addOption(HttpTask.OPTION_OPERATION, opId);
//		descriptor.addOption(HttpTask.OPTION_SERVER_ENDPOINT, serverEndpoint);
//		descriptor.getOptions().add(new DurationOption(HttpTask.OPTION_POLL_INTERVAL, pollInterval));
//		if ( timeout != null ) {
//			descriptor.getOptions().add(new DurationOption(HttpTask.OPTION_TIMEOUT, timeout));
//		}
//		descriptor.getOptions().add(new BooleanOption(HttpTask.OPTION_SYNC, m_sync));
//
//		// 명령어 인자로 지정된 input/output parameter 값을 Task variable들에 반영한다.
//		loadTaskVariablesFromParameters(manager, descriptor);
//		
//		HttpTask httpTask = new HttpTask(descriptor);
//		httpTask.run(manager);
//		
//		Duration elapsed = Duration.between(started, Instant.now());
//		if ( getLogger().isInfoEnabled() ) {
//			getLogger().info("HttpTask: elapsedTime={}", elapsed);
//		}
//	}
}
