package mdt.cli.list.push;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.func.Try;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstanceManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "list",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "List all MDT entities (MDTInstances, Shells, Submodels, Simulations, or AIs)."
)
public class ListCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	public static enum TargetType { instances, shells, submodels, simulations, ais };
	
	@Parameters(index="0", arity="0..1", paramLabel="type", defaultValue = "instances",
				description="target type: ${COMPLETION-CANDIDATES}")
	private TargetType m_type = TargetType.instances;
	
	@Option(names={"--filter", "-f"}, description="instance filter.")
	private String m_filter = null;
	
	@Option(names={"--table", "-t" }, description="display instances in a table format.")
	private boolean m_tableFormat = false;
	
	@Option(names={"--dependency", "-d" }, description="display instances in a table format.")
	private boolean m_showDependency = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new ListCommand(), args);
	}
	
	public ListCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		MDTInstanceManager instMgr = manager.getInstanceManager();

		ListBuilder builder = switch ( m_type ) {
			case instances -> new InstanceListBuilder(instMgr, m_filter);
			case shells -> new ShellListBuilder(instMgr, m_filter);
			case submodels -> new SubmodelListBuilder(instMgr, m_filter);
			case simulations -> new SimulationListBuilder(instMgr, m_filter);
			case ais -> new AIListBuilder(instMgr, m_filter);
			default -> throw new IllegalArgumentException("Unsupported MDT object type: " + m_type);
		};
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				String listStr;
				if ( m_showDependency ) {
					Node node = builder.buildTreeNode();

					TreeOptions opts = new TreeOptions();
					opts.setStyle(TreeStyles.UNICODE_ROUNDED);
					opts.setMaxDepth(5);
					listStr = TextTree.newInstance(opts).render(node);
				}
				else if ( m_tableFormat ) {
					listStr = builder.buildTableString();
				}
				else {
					listStr = builder.buildListString();
				}
				
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.print(listStr);
				if ( m_tableFormat ) {
					System.out.println();
				}
			}
			catch ( Exception e ) {
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.println("" + e);
			}
			if ( m_verbose ) {
				System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
			}
			
			if ( repeatInterval == null ) {
				break;
			}
			
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}

	public interface ListCollector {
		public void collectLine(Object[] cols);
		public String getFinalString();
	}
	
	public interface ListBuilder {
		public String buildTableString();
		public String buildListString();
		public Node buildTreeNode();
	}
	
	static class SimpleListCollector implements ListCollector {
		private ByteArrayOutputStream m_baos;
		private PrintWriter m_writer;
		
		SimpleListCollector() {
			m_baos = new ByteArrayOutputStream();
			m_writer = new PrintWriter(m_baos);
		}

		@Override
		public void collectLine(Object[] cols) {
			m_writer.println(FStream.of(cols).join('|'));
		}
		
		void addLine(String line) {
		}
		
		@Override
		public String getFinalString() {
			m_writer.close();
			Try.run(m_baos::close);
			return m_baos.toString();
		}
	}
	
	static class TableCollector implements ListCollector {
		private Table m_table;
		
		TableCollector(Table table) {
			m_table = table;
		}

		@Override
		public void collectLine(Object[] cols) {
			FStream.of(cols).forEach(c -> m_table.addCell(""+c));
		}
		
		@Override
		public String getFinalString() {
			return m_table.render();
		}
	}
}
