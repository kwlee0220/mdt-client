package mdt.cli.get.instance;

import java.io.IOException;
import java.time.Duration;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MovingAverage;
import utils.StopWatch;
import utils.UnitUtils;
import utils.async.PeriodicLoopExecution;
import utils.async.StartableExecution;
import utils.func.FOption;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstance;

import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractInstanceSubCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractInstanceSubCommand.class);

	@ParentCommand GetInstanceCommand m_parent;
	
	@Option(names={"--output", "-o"}, paramLabel="type", defaultValue="tree", required=false,
			description="output type (candidnates: 'tree' or 'json')")
	private String m_output = "tree";
	
	@Option(names={"--repeat", "-r"}, paramLabel="interval", description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	private final MovingAverage m_mavg = new MovingAverage(0.1f);
	
	abstract protected Node toMDTModelNode(MDTInstance instance) throws IOException;
	protected void displayAsJson(HttpMDTInstanceClient instance) throws SerializationException, IOException { }
	
	protected AbstractInstanceSubCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceClient modelSvc = m_parent.getInstance(mdt);
		
		if ( m_repeat == null ) {
			switch ( m_output ) {
				case "tree":
					displayAsTree(modelSvc, false);
					break;
				case "json":
					displayAsJson(modelSvc);
					break;
				default:
					throw new IllegalArgumentException("unsupported output type: " + m_output);
			}
			return;
		}

		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		StartableExecution<Void> exec = new PeriodicLoopExecution<>(repeatInterval) {
			@Override
			protected FOption<Void> performPeriodicAction(long loopIndex) throws Exception {
				try {
					displayAsTree(modelSvc, true);
				}
				catch ( InvalidResourceStatusException expected ) {
					System.out.println("instance is not running: id=" + m_parent.getInstanceId());
				}
				catch ( Exception e ) {
					System.out.printf("failed to get MDT model info: instance=%s, cause=%s%n", m_parent.getInstanceId(), e);
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
	
	private void displayAsTree(MDTInstance service, boolean clearConsole) throws IOException {
		StopWatch watch = StopWatch.start();
		
		Node mdtModelNode = toMDTModelNode(service);
		String treeString = TextTree.newInstance(TREE_OPTS).render(mdtModelNode);
		if ( clearConsole ) {
			System.out.print(CLEAR_CONSOLE_CONTROL);
		}
		System.out.print(treeString);
		
		if ( m_verbose ) {
			watch.stop();
			
			double avg = m_mavg.observe(watch.getElapsedInFloatingSeconds());
			String secStr = UnitUtils.toMillisString(Math.round(avg * 1000));
			System.out.println("elapsed: " + secStr);
		}
	}
}
