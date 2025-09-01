package mdt.cli.workflow;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
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
import mdt.cli.list.ListCommands;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;

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
	description = "List all MDT Workflow models."
)
public class ListWorkflowModelAllCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListWorkflowModelAllCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;
	
	@Option(names={"--glob", "-g"}, paramLabel="expr", required=false,
			description="glob pattern to filter workflows.")
	private String m_glob = null;

	@Option(names={"--long", "-l"}, description="show detailed information.")
	private boolean m_long = false;

	@Option(names={"--delimiter", "-d"}, paramLabel="delimiter",
					description="delimiter (for 'csv' output only)")
	private String m_delimiter = ListCommands.DELIM;

	@Option(names={"--repeat", "-r"}, paramLabel="interval",
			description="repeat interval (e.g. \"1s\", \"500ms\"")
	private String m_repeat = null;
	
	@Option(names={"-v"}, description="verbose")
	private boolean m_verbose = false;

	public static final void main(String... args) throws Exception {
		main(new ListWorkflowModelAllCommand(), args);
	}
	
	public ListWorkflowModelAllCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager svc = ((HttpMDTManager)mdt).getWorkflowManager();
		
		Duration repeatInterval = (m_repeat != null) ? UnitUtils.parseDuration(m_repeat) : null;
		while ( true ) {
			StopWatch watch = StopWatch.start();
			
			try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos) ) {
				List<WorkflowModel> wfModelList = listWorkflowModels(svc);
                if ( m_long ) {
					if ( m_tableFormat ) {
						printModelsLongTable(wfModelList, pw);
					}
					else {
                    	printModelsLongList(wfModelList, pw);
					}
                }
                else {
					if ( m_tableFormat ) {
						printModelsShortTable(wfModelList, pw);
					}
					else {
						printModelsShortList(wfModelList, pw);
					}
                }
				pw.flush();
				String outputString = baos.toString();
				if ( repeatInterval == null ) {
					System.out.print(outputString);
					break;
				}
				else {
					System.out.print(CLEAR_CONSOLE_CONTROL);
					System.out.print(outputString);
				}
				if ( m_verbose ) {
					System.out.println("elapsed: " + watch.stopAndGetElpasedTimeString());
				}
			}
			
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}
	
	private List<WorkflowModel> listWorkflowModels(WorkflowManager wfMgr) {
		if ( m_glob != null ) {
			String pattern = "glob:" + m_glob;
	        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
	        
	        return FStream.from(wfMgr.getWorkflowModelAll())
					        .filter(wf -> matcher.matches(Paths.get(wf.getName())))
					        .toList();
		}
		else {
			return wfMgr.getWorkflowModelAll();
		}
	}
	
	private void printModelsShortList(List<WorkflowModel> wfModelList, PrintWriter pw) {
		int seqNo = 1;
		for ( WorkflowModel wfModel : wfModelList ) {
			pw.append(""+seqNo).append(m_delimiter);
			pw.append(wfModel.getId()).append(m_delimiter);
			pw.append(""+wfModel.getTaskDescriptors().size());
			pw.println();
			++seqNo;
		}
	}
	
	private void printModelsShortTable(List<WorkflowModel> wfModelList, PrintWriter pw) {
		Table table = new Table(3);
		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" COUNT ");
		
		int seqNo = 1;
		for ( WorkflowModel wfModel : wfModelList ) {
			table.addCell(String.format("%3d", seqNo));
			table.addCell(wfModel.getId());
			table.addCell(String.format("%6d", wfModel.getTaskDescriptors().size()));
			++seqNo;
		}
		pw.println(table.render());
	}
	
	private void printModelsLongList(List<WorkflowModel> wfModelList, PrintWriter pw) {
		int seqNo = 1;
		for ( WorkflowModel wfModel : wfModelList ) {
			pw.append(""+seqNo).append(m_delimiter);
			pw.append(wfModel.getId()).append(m_delimiter);
			pw.append(wfModel.getName()).append(m_delimiter);
			pw.append(""+wfModel.getTaskDescriptors().size());
			pw.println();
			++seqNo;
		}
	}
	
	private void printModelsLongTable(List<WorkflowModel> wfModelList, PrintWriter pw) {
		Table table = new Table(4);
		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" NAME ");
		table.addCell(" COUNT ");
		
		int seqNo = 1;
		for ( WorkflowModel wfModel : wfModelList ) {
			table.addCell(String.format("%3d", seqNo));
			table.addCell(wfModel.getId());
			table.addCell(wfModel.getName());
			table.addCell(String.format("%6d", wfModel.getTaskDescriptors().size()));
			++seqNo;
		}
		pw.println(table.render());
	}
}
