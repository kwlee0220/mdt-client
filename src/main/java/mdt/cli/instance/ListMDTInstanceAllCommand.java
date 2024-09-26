package mdt.cli.instance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.Utilities;
import utils.stream.FStream;

import mdt.cli.MDTCommand;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.IdPair;
import mdt.model.MDTManager;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "list",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "List all MDT Instances."
)
public class ListMDTInstanceAllCommand extends MDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListMDTInstanceAllCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	@Option(names={"--filter", "-f"}, description="instance filter.")
	private String m_filter = null;
	
	@Option(names={"--long", "-l"}, description="display details about instances.")
	private boolean m_long = false;
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new ListMDTInstanceAllCommand(), args);
	}
	
	public ListMDTInstanceAllCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager manager) throws Exception {
		HttpMDTInstanceManagerClient client = (HttpMDTInstanceManagerClient)manager.getInstanceManager();
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				List<MDTInstance> instances;
				if ( m_filter == null ) {
					instances = client.getAllInstances();
				}
				else {
					instances = client.getAllInstancesByFilter(m_filter);
				}
				
				String outputString = buildOutputString(instances);
				if ( repeatInterval != null ) {
					System.out.print(CLEAR_CONSOLE_CONTROL);
				}
				System.out.print(outputString);
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
	
	private String buildOutputString(List<MDTInstance> instances) {
		if ( m_long ) {
			if ( m_tableFormat ) {
				return buildLongTableString(instances);
			}
			else {
				return buildLongListString(instances);
			}
		}
		else {
			if ( m_tableFormat ) {
				return buildTableString(instances);
			}
			else {
				return buildListString(instances);
			}
		}
	}
	
	private String buildListString(List<MDTInstance> instances) {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(baos) ) {
			int seqNo = 1;
			for ( MDTInstance inst : instances ) {
				out.println(FStream.of(toShortColumns(seqNo, inst)).map(Object::toString).join('|'));
				++seqNo;
			}
			out.close();
			return baos.toString();
		}
		catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	private String buildLongListString(List<MDTInstance> instances) {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(baos) ) {
			int seqNo = 1;
			for ( MDTInstance inst : instances ) {
				System.out.println(FStream.of(toLongColumns(seqNo, inst)).map(Object::toString).join('|'));
				++seqNo;
			}
			out.close();
			return baos.toString();
		}
		catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	private String buildTableString(List<MDTInstance> instances) {
		Table table = new Table(4);
		table.setColumnWidth(1, 20, 70);

		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" ENDPOINT ");

		int seqNo = 1;
		for ( MDTInstance inst : instances ) {
			FStream.of(toShortColumns(seqNo, inst))
					.map(Object::toString)
					.forEach(table::addCell);
			++seqNo;
		}
		return table.render() + Utilities.getLineSeparator();
	}
	
	private String buildLongTableString(List<MDTInstance> instances) {
		Table table = new Table(6);
		table.setColumnWidth(2, 20, 70);
		table.setColumnWidth(3, 20, 45);

		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_IDs ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" STATUS ");
		table.addCell(" ENDPOINT ");
		
		int seqNo = 1;
		for ( MDTInstance inst : instances ) {
			FStream.of(toLongColumns(seqNo, inst))
					.map(Object::toString)
					.forEach(table::addCell);
			++seqNo;
		}
		return table.render() + Utilities.getLineSeparator();
	}
	
	private Object[] toShortColumns(int seqNo, MDTInstance instance) {
		String submodelIdCsv = FStream.from(instance.getAllInstanceSubmodelDescriptors())
										.map(InstanceSubmodelDescriptor::getIdShort)
										.join(',');
		
		return new Object[] {
			String.format("%3d", seqNo),
			instance.getId(),
//			instance.getAasId(),
			submodelIdCsv,
			getOrEmpty(instance.getBaseEndpoint())
		};
	}
	
	private Object[] toLongColumns(int seqNo, MDTInstance instance) {
		String submodelIdCsv = FStream.from(instance.getAllInstanceSubmodelDescriptors())
										.map(InstanceSubmodelDescriptor::getIdShort)
										.join(',');
		
		String serviceEndpoint = ObjectUtils.defaultIfNull(instance.getBaseEndpoint(), "");
		return new Object[] {
			String.format("%3d", seqNo),
			instance.getId(),
			IdPair.of(instance.getAasId(), instance.getAasIdShort()),
			submodelIdCsv,
			instance.getStatus(), serviceEndpoint
		};
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
