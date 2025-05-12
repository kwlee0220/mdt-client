package mdt.cli.get;

import java.io.IOException;
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
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.tree.CompositeNode;
import mdt.tree.MDTModelInfoNodes;

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
public class GetInstanceMdtModelInfoCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetInstanceMdtModelInfoCommand.class);
	
	@ParentCommand GetInstanceCommand m_parent;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new GetInstanceMdtModelInfoCommand(), args);
	}
	
	public GetInstanceMdtModelInfoCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		MDTInstance instance = manager.getInstance(m_parent.getInstanceId());
		
		if ( m_repeat == null ) {
			display(instance);
			return;
		}

		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		StartableExecution<Void> exec = new PeriodicLoopExecution<>(repeatInterval) {
			@Override
			protected FOption<Void> performPeriodicAction(long loopIndex) throws Exception {
				try {
					MDTInstance instance = manager.getInstance(m_parent.getInstanceId());
					display(instance);
				}
				catch ( InvalidResourceStatusException expected ) {
					System.out.println("instance is not running: id=" + instance.getId());
				}
				catch ( Exception e ) {
					System.out.printf("failed to get MDT model info: instance=%s, cause=%s%n", instance.getId(), e);
				}
				return FOption.empty();
			}
		};
		exec.start();
		exec.get();
	}

	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	private static final TreeOptions TREE_OPTS = new TreeOptions();
	static {
		TREE_OPTS.setStyle(TreeStyles.UNICODE_ROUNDED);
		TREE_OPTS.setMaxDepth(5);
	}
	
	private void display(MDTInstance instance) throws IOException {
		StopWatch watch = StopWatch.start();
		CompositeNode node = MDTModelInfoNodes.newInstanceMDTModelNode(instance);
		String treeString = TextTree.newInstance(TREE_OPTS).render(node);
		
		System.out.print(CLEAR_CONSOLE_CONTROL);
		System.out.print(treeString);
		if ( m_verbose ) {
			System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
		}
	}
}
