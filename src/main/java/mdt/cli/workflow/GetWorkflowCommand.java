package mdt.cli.workflow;

import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.workflow.NodeTask;
import mdt.workflow.Workflow;
import mdt.workflow.WorkflowManager;
import mdt.workflow.WorkflowModel;
import mdt.workflow.model.TaskDescriptor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "get",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get an MDT Workflow instance (or model).",
	subcommands = {
		GetWorkflowScriptCommand.class,
		GetWorkflowLogCommand.class,
	}
)
public class GetWorkflowCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetWorkflowCommand.class);
	
	@Parameters(index="0", paramLabel="id", description="Workflow id to get")
	private String m_id;

	@Option(names={"--model", "-m"}, description="show workflow model.")
	private boolean m_model = false;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: table (default) or json)")
	private String m_output = "table";

	public static final void main(String... args) throws Exception {
		main(new GetWorkflowCommand(), args);
	}
	
	public GetWorkflowCommand() {
		setLogger(s_logger);
	}
	
	public String getWorkflowModelId() {
		return m_id;
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager wfMgr = ((HttpMDTManager)mdt).getWorkflowManager();
		
		if ( m_model ) {
			WorkflowModel wfModel = wfMgr.getWorkflowModel(m_id);
			switch ( m_output ) {
				case "table":
					displayAsTable(wfModel);
					break;
				case "json":
					displayAsJson(wfModel);
					break;
				default:
					throw new IllegalArgumentException("invalid output type: " + m_output);
			}
		}
		else {
			Workflow workflow = wfMgr.getWorkflow(m_id);
			switch (m_output) {
				case "table":
					displayAsTable(workflow);
					break;
				case "json":
					displayAsJson(workflow);
					break;
				default:
					throw new IllegalArgumentException("invalid output type: " + m_output);
			}
		}
	}
	
	private void displayAsTable(Workflow workflow) {
		Table table = new Table(2);

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" NAME "); table.addCell(" " + workflow.getName());
		table.addCell(" MODEL "); table.addCell(" " + workflow.getModelId());
		table.addCell(" STATUS "); table.addCell(" " + workflow.getStatus());
		table.addCell(" CREATION_TIME "); table.addCell(" " + workflow.getCreationTime());
		table.addCell(" START_TIME "); table.addCell(" " + FOption.getOrElse(workflow.getStartTime(), ""));
		table.addCell(" FINISH_TIME "); table.addCell(" " + FOption.getOrElse(workflow.getFinishTime(), ""));
		
		FStream.from(workflow.getNodeTasks()).zipWithIndex().forEach(tup -> {
			NodeTask task = tup.value();
			
			table.addCell(String.format(" TASK[%02d] ", tup.index()));
			String depListStr = FStream.from(task.getDependents())
										.join(", ", "[", "]");
			String msg = String.format(" %s (%s) <- %s", task.getTaskName(), task.getStatus(), depListStr);
			table.addCell(msg);
		});
		
		System.out.println(table.render());
	}
	
	private void displayAsTable(WorkflowModel wfModel) {
		Table table = new Table(2);

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + wfModel.getId());
		table.addCell(" NAME "); table.addCell(" " + wfModel.getName());
		table.addCell(" DEESCRIPTION "); table.addCell(" " + FOption.getOrElse(wfModel.getDescription(), ""));
		
		FStream.from(wfModel.getTaskDescriptors()).zipWithIndex().forEach(tup -> {
			TaskDescriptor task = tup.value();
			
			table.addCell(String.format(" TASK[%02d] ", tup.index()));
			table.addCell(" " + task.toSignatureString());
		});
		
		System.out.println(table.render());
	}
	
	private void displayAsJson(WorkflowModel wfModel) {
		System.out.println(wfModel.toJsonString());
	}
	
	private void displayAsJson(Workflow workflow) throws JsonProcessingException {
		JsonMapper mapper = MDTModelSerDe.getJsonMapper();
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(workflow));
	}
}
