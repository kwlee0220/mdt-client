package mdt.cli.list;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.InternalException;
import utils.StopWatch;
import utils.UnitUtils;

import mdt.cli.AbstractMDTCommand;
import mdt.cli.list.ListCommands.CSVCollector;
import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.client.instance.HttpMDTInstance;
import mdt.model.MDTManager;
import mdt.model.ReferenceUtils;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.Simulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "submodels",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all Submodels."
)
public class ListSubmodelCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListSubmodelCommand.class);
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
	
	public ListSubmodelCommand() {
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
		table.setColumnWidth(2, 20, 50);
		table.setColumnWidth(5, 5, 70);

		table.addCell(" # ");
		table.addCell(" MDT_ID ");
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" SEMANTIC_ID ");
		table.addCell(" ENDPOINT ");
		
		return collect(instances, new TableCollector(table));
	}

	private String collect(List<? extends MDTInstance> instances, ListCollector collector) {
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			for ( SubmodelDescriptor smDesc: inst.getSubmodelDescriptorAll() ) {
				String[] cols = toColumns(seqNo, (HttpMDTInstance)inst, smDesc);
				collector.collectLine(cols);
				
				++seqNo;
			}
		}
		
		return collector.getFinalString();
	}

	private String[] toColumns(int seqNo, HttpMDTInstance inst, SubmodelDescriptor smDesc) {
		String[] cols = new String[] {
			String.format("%3d", seqNo), inst.getId(), smDesc.getId(), smDesc.getIdShort(),
			getSemanticIdString(smDesc), inst.getSubmodelServiceEndpoint(smDesc.getId())
		};
		return cols;
	}
	
	private String getSemanticIdString(SubmodelDescriptor smDesc) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(smDesc.getSemanticId());
		if ( semanticId == null ) {
			return "";
		}
		return switch ( semanticId ) {
			case Data.SEMANTIC_ID -> "Data";
			case InformationModel.SEMANTIC_ID -> "InformationModel";
			case Simulation.SEMANTIC_ID -> "Simulation";
			case AI.SEMANTIC_ID -> "AI";
			default -> throw new InternalException("Unsupported SemanticID: " + semanticId);
		};
	}
}
