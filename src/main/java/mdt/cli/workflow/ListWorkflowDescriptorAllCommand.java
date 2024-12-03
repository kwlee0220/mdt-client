package mdt.cli.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.StopWatch;
import utils.UnitUtils;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManagerClient;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowDescriptorService;
import mdt.workflow.model.WorkflowDescriptor;
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
	description = "List all MDT Workflow Descriptors."
)
public class ListWorkflowDescriptorAllCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListWorkflowDescriptorAllCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new ListWorkflowDescriptorAllCommand(), args);
	}
	
	public ListWorkflowDescriptorAllCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowDescriptorService svc = ((HttpMDTManagerClient)mdt).createClient(WorkflowDescriptorService.class);
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try {
				List<WorkflowDescriptor> wfDescList = svc.getWorkflowDescriptorAll();
				
				String outputString = buildOutputString(wfDescList);
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
	
	private String buildOutputString(List<WorkflowDescriptor> wfDescList) {
		if ( m_tableFormat ) {
			return buildTableString(wfDescList);
		}
		else {
			return buildListString(wfDescList);
		}
	}
	
	private String buildListString(List<WorkflowDescriptor> wfDescList) {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos) ) {
			for ( WorkflowDescriptor wfDesc: wfDescList ) {
				pw.println(wfDesc.getId());
			}
			pw.close();
			
			return baos.toString();
		}
		catch ( IOException e ) {
			throw new AssertionError();
		}
	}
	
	private String buildTableString(List<WorkflowDescriptor> wfDescList) {
		Table table = new Table(4);
		table.setColumnWidth(2, 20, 50);
		table.setColumnWidth(3, 20, 80);

		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" NAME ");
		table.addCell(" DESCRIPTION ");
		
		int seqNo = 1;
		for ( WorkflowDescriptor wfDesc : wfDescList ) {
			FStream.of(toLongColumns(seqNo, wfDesc))
					.map(Object::toString)
					.forEach(table::addCell);
			++seqNo;
		}
		return table.render();
	}
	
	private Object[] toLongColumns(int seqNo, WorkflowDescriptor wfDesc) {
		return new Object[] {
			String.format("%2d", seqNo),
			wfDesc.getId(),
			FOption.getOrElse(wfDesc.getName(), ""),
			FOption.getOrElse(wfDesc.getDescription(), "")
		};
	}
}
