package mdt.cli.list;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.cli.list.ListCommands.CSVCollector;
import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.model.MDTManager;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTOperationDescriptor;
import mdt.model.sm.value.NamedValueType;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "operations",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all operations (AI or Simulations) submodels."
)
public class ListOperationCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListOperationCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;
	
	@Option(names={"--table", "-t"}, description="'table' format output")
	private boolean m_table = false; 

	@Option(names={"--delimiter", "-d"}, paramLabel="delimiter",
					description="delimiter (for 'simple' output only)")
	private String m_delimiter = ListCommands.DELIM;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\")")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;
	
	public ListOperationCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		MDTInstanceManager manager = mdt.getInstanceManager();
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				List<? extends MDTInstance> instances = (m_filter != null)
														? manager.getInstanceAllByFilter(m_filter)
														: manager.getInstanceAll();
				
				String listStr = (m_table) ? buildTableString(instances) + System.lineSeparator()
											: buildCsvString(instances);
				
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.print(listStr);
				System.out.flush();
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
			
			long remainMillis = repeatInterval.minus(watch.getElapsed()).toMillis();
			if ( remainMillis > 0 ) {
				TimeUnit.MILLISECONDS.sleep(remainMillis);
			}
		}
	}

	private String buildCsvString(List<? extends MDTInstance> instances) {
		return collect(instances, new CSVCollector(m_delimiter));
	}

	private String buildTableString(List<? extends MDTInstance> instances) {
		Table table = new Table(6);
		table.setColumnWidth(4, 20, 70);
		table.setColumnWidth(5, 10, 40);

		table.addCell(" # ");
		table.addCell(" MDT ");
		table.addCell(" OPERATION ");
		table.addCell(" TYPE ");
		table.addCell(" INPUTS ");
		table.addCell(" OUTPUTS ");
		
		return collect(instances, new TableCollector(table));
	}
	
	private String collect(List<? extends MDTInstance> instances, ListCollector collector) {
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			for ( MDTOperationDescriptor opDesc: inst.getInstanceDescriptor().getMDTOperationDescriptorAll() ) {
				String[] cols = toOperationColumns(seqNo, inst, opDesc);
				collector.collectLine(cols);
				++seqNo;
			}
		}
		
		return collector.getFinalString();
	}
	
	private String[] toOperationColumns(int seqNo, MDTInstance inst, MDTOperationDescriptor opDesc) {
		String inArgNameCsv = FStream.from(opDesc.getInputArguments())
					                .map(NamedValueType::getName)
					                .join(",");
		String outArgNameCsv = FStream.from(opDesc.getOutputArguments())
				                    .map(NamedValueType::getName)
                                    .join(",");
		return new String[] { String.format("%3d", seqNo), inst.getId(), opDesc.getName(), opDesc.getOperationType(),
								inArgNameCsv, outArgNameCsv };
	}
}
