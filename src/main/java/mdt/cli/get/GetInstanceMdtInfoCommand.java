package mdt.cli.get;

import java.time.Duration;

import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.async.PeriodicLoopExecution;
import utils.async.StartableExecution;
import utils.func.FOption;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.DefaultMDTInstanceInfo;
import mdt.tree.state.InstanceStatesNode;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "mdt-info",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get MDT information (parameter-infos & operation-infos) of an instance"
)
public class GetInstanceMdtInfoCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceMdtInfoCommand.class);
	
	@ParentCommand GetInstanceCommand m_parent;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: 'tree' or 'json')")
	private String m_output = "tree";
	
	@Option(names={"--skip-parameters"}, description="skip paramerter-infos")
	private boolean m_skipParameters = false;
	
	@Option(names={"--skip-operations"}, description="show operation-infos")
	private boolean m_skipOperations = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new GetInstanceMdtInfoCommand(), args);
	}
	
	public GetInstanceMdtInfoCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		HttpMDTInstanceClient instance = client.getInstance(m_parent.getInstanceId());
		
		if ( m_repeat == null ) {
			display(instance);
			return;
		}

		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		StartableExecution<Void> exec = new PeriodicLoopExecution<>(repeatInterval) {
			@Override
			protected FOption<Void> performPeriodicAction(long loopIndex) throws Exception {
				display(instance);
				return FOption.empty();
			}
		};
		exec.start();
		exec.waitForFinished();
	}

	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	private static final TreeOptions TREE_OPTS = new TreeOptions();
	static {
		TREE_OPTS.setStyle(TreeStyles.UNICODE_ROUNDED);
		TREE_OPTS.setMaxDepth(5);
	}
	
	private void display(HttpMDTInstanceClient instance) {
		switch ( m_output ) {
			case "tree":
				displayAsTree(instance);
				break;
			case "json":
				displayMDTInfo(instance);
				break;
			default:
				throw new IllegalArgumentException("unknown output type: " + m_output);
		}
	}
	
	private void displayAsTree(HttpMDTInstanceClient instance) {
		StopWatch watch = StopWatch.start();
		InstanceStatesNode statesNode = new InstanceStatesNode(instance, !m_skipParameters, !m_skipOperations);
		String treeString = TextTree.newInstance(TREE_OPTS).render(statesNode);
		
		System.out.print(CLEAR_CONSOLE_CONTROL);
		System.out.print(treeString);
		if ( m_verbose ) {
			System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
		}
	}
	
	private void displayMDTInfo(HttpMDTInstanceClient instance) {
		DefaultMDTInstanceInfo info = DefaultMDTInstanceInfo.builder(instance).build();
		System.out.println(MDTModelSerDe.toJsonString(info));
	}
}
