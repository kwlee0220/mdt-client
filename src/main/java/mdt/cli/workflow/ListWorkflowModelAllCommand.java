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
import mdt.cli.list.ListCommands;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.workflow.Workflow;
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
	description = "List all MDT Workflow instances (or models)."
)
public class ListWorkflowModelAllCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListWorkflowModelAllCommand.class);
	private static final String CLEAR_CONSOLE_CONTROL = "\033[2J\033[1;1H";

	@Option(names={"--model", "-m"}, description="show workflow model.")
	private boolean m_model = false;
	
	@Option(names={"--table", "-t"}, description="display instances in a table format.")
	private boolean m_tableFormat = false;

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
			
			String outputString;
			try {
				if ( m_model ) {
					List<WorkflowModel> wfModelList = svc.getWorkflowModelAll();
					outputString = buildModelOutputString(wfModelList);
                }
                else {
                    List<Workflow> wfList = svc.getWorkflowAll();
                    if ( m_long ) {
    					if ( m_tableFormat ) {
    						printInstancesLongTable(wfList, new PrintWriter(System.out));
    					}
    					else {
                        	printInstancesLongList(wfList, new PrintWriter(System.out));
    					}
                    }
                    else {
						if ( m_tableFormat ) {
							printInstancesShortTable(wfList, new PrintWriter(System.out));
						}
						else {
							printInstancesShortList(wfList, new PrintWriter(System.out));
						}
                    }
                    outputString = buildInstanceOutputString(wfList);
                }
			}
			catch ( Exception e ) {
				outputString = "" + e;
			}
			
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
			
			Duration remains = repeatInterval.minus(watch.getElapsed());
			if ( !(remains.isNegative() || remains.isZero()) ) {
				TimeUnit.MILLISECONDS.sleep(remains.toMillis());
			}
		}
	}
	
	private void printInstancesShortList(List<Workflow> wfList, PrintWriter pw) {
		int seqNo = 1;
		for ( Workflow workflow : wfList ) {
			pw.append(""+seqNo).append(m_delimiter);
			pw.append(workflow.getName()).append(m_delimiter);
			pw.append(workflow.getModelId()).append(m_delimiter);
			pw.append(""+workflow.getTasks().size()).append(m_delimiter);
			pw.append(workflow.getStatus().toString());
			pw.println();
			++seqNo;
		}
	}
	
	private void printInstancesShortTable(List<Workflow> wfList, PrintWriter pw) {
		Table table = new Table(5);
		table.addCell(" # ");
		table.addCell(" NAME ");
		table.addCell(" MODEL ");
		table.addCell(" COUNT ");
		table.addCell(" STATUS ");
		
		int seqNo = 1;
		for ( Workflow workflow : wfList ) {
			table.addCell(String.format("%3d", seqNo));
			table.addCell(workflow.getName());
			table.addCell(workflow.getModelId());
			table.addCell(""+workflow.getTasks().size());
			table.addCell(workflow.getStatus().toString());
			++seqNo;
		}
		pw.println(table.render());
	}
	
	private void printInstancesLongList(List<Workflow> wfList, PrintWriter pw) {
		int seqNo = 1;
		for ( Workflow workflow : wfList ) {
			pw.append(""+seqNo).append(m_delimiter);
			pw.append(workflow.getName()).append(m_delimiter);
			pw.append(workflow.getModelId()).append(m_delimiter);
			pw.append(workflow.getStatus().toString());
			pw.append(String.format(" %3d ", workflow.getTasks().size())).append(m_delimiter);
			String tasksStr = FStream.from(workflow.getTasks())
										.map(task -> task.getTaskId())
										.join(", ");
			pw.append(tasksStr);
			pw.println();
			
			++seqNo;
		}
	}
	
	private void printInstancesLongTable(List<Workflow> wfList, PrintWriter pw) {
		Table table = new Table(6);
		table.setColumnWidth(2, 20, 50);

		table.addCell(" # ");
		table.addCell(" NAME ");
		table.addCell(" MODEL ");
		table.addCell(" STATUS ");
		table.addCell(" COUNT ");
		table.addCell(" TASKS ");
		
		int seqNo = 1;
		for ( Workflow workflow : wfList ) {
			table.addCell(String.format("%3d", seqNo));
			table.addCell(workflow.getName());
			table.addCell(workflow.getModelId());
			table.addCell(workflow.getStatus().toString());
			table.addCell(String.format(" %3d ", workflow.getTasks().size()));
			
			String tasksStr = FStream.from(workflow.getTasks())
									.map(task -> task.getTaskId())
									.join(", ");
			table.addCell(tasksStr);
			++seqNo;
		}
		pw.println(table.render());
	}
	
	private String buildInstanceOutputString(List<Workflow> wfList) {
		List<String[]> rows = FStream.from(wfList)
									.zipWithIndex(1)
									.map(idxed -> toInstanceRow(idxed.index(), idxed.value()))
									.toList();
		
		if ( m_tableFormat ) {
			return (m_long) ? buildLongInstanceTableString(rows) : buildShortInstanceTableString(rows);
		}
		else {
			return buildListString(rows);
		}
	}
	
	private String buildListString(List<String[]> rows) {
		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos) ) {
			for ( String[] row: rows ) {
				pw.println(FStream.of(row).join(m_delimiter));
			}
			pw.close();
			
			return baos.toString();
		}
		catch ( IOException e ) {
			throw new AssertionError();
		}
	}
	
	private String buildModelOutputString(List<WorkflowModel> wfModelList) {
		List<String[]> rows = FStream.from(wfModelList)
									.zipWithIndex(1)
									.map(idxed -> toModelRow(idxed.index(), idxed.value()))
									.toList();
		
		if ( m_tableFormat ) {
			return (m_long) ? buildLongModelTableString(rows) : buildShortModelTableString(rows);
		}
		else {
			return buildListString(rows);
		}
	}
	
	private String buildLongModelTableString(List<String[]> rows) {
		Table table = new Table(4);
		table.setColumnWidth(2, 20, 50);
		table.setColumnWidth(3, 20, 50);

		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" NAME ");
		table.addCell(" NODES ");
		
		for ( String[] row : rows ) {
			FStream.of(row).forEach(table::addCell);
		}
		return table.render() + System.lineSeparator();
	}
	
	private String buildShortModelTableString(List<String[]> rows) {
		Table table = new Table(1);

		table.addCell(" ID ");
		
		for ( String[] row : rows ) {
			FStream.of(row).forEach(table::addCell);
		}
		return table.render() + System.lineSeparator();
	}
	
	private String buildLongInstanceTableString(List<String[]> rows) {
		Table table = new Table(5);
		table.setColumnWidth(2, 20, 50);

		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" MODEL ");
		table.addCell(" STATUS ");
		table.addCell(" COUNT ");
		table.addCell(" NODES ");
		
		for ( String[] row : rows ) {
			FStream.of(row).forEach(table::addCell);
		}
		return table.render() + System.lineSeparator();
	}
	
	private String buildShortInstanceTableString(List<String[]> rows) {
		Table table = new Table(5);

		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" MODEL ");
		table.addCell(" COUNT ");
		table.addCell(" STATUS ");
		
		FStream.from(rows)
				.forEach(row -> FStream.of(row).forEach(table::addCell));
		return table.render() + System.lineSeparator();
	}
	
	private String[] toModelRow(int seqNo, WorkflowModel wfModel) {
		String tasksStr = FStream.from(wfModel.getTaskDescriptors())
								.map(task -> task.getId())
								.join(", ");
		if ( m_long ) {
			return new String[] { ""+seqNo,
									wfModel.getId(),
									FOption.getOrElse(wfModel.getName(), ""),
									tasksStr };
		}
		else {
			return new String[] { ""+seqNo,
									wfModel.getId() };
		}
	}
	
	private String[] toInstanceRow(int seqNo, Workflow workflow) {
		if ( m_long ) {
			String tasksStr = FStream.from(workflow.getTasks())
									.map(task -> task.getTaskId())
									.join(", ");
			return new String[] { ""+seqNo,
									workflow.getName(),
									workflow.getModelId(),
									workflow.getStatus().toString(),
									""+workflow.getTasks().size(),
									tasksStr };
		}
		else {
			return new String[] { ""+seqNo,
									workflow.getName(),
									workflow.getModelId(),
									" "+workflow.getTasks().size(),
									workflow.getStatus().toString() };
		}
	}
}
