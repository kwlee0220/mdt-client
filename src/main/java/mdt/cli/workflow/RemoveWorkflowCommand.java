package mdt.cli.workflow;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.HttpMDTManager;
import mdt.model.MDTManager;
import mdt.workflow.WorkflowManager;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "remove",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Remove the MDT Workflow Descriptor of the given id."
)
public class RemoveWorkflowCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(RemoveWorkflowCommand.class);

	@Parameters(index="0..*", paramLabel="ids", description="WorkflowDescriptor ids to remove")
	private List<String> m_wfIds;

	@Option(names={"--model", "-m"}, description="remove workflow model.")
	private boolean m_model = false;
	
	@Option(names={"--model-filter"}, paramLabel="model-id", description="target model id to remove")
	private String m_modelFilter = null;
	
	@Option(names={"--glob", "-g"}, paramLabel="expr", required=false,
			description="glob pattern to filter workflows.")
	private String m_glob = null;
	
	@Option(names={"--all", "-a"}, description="remove all WorkflowDescriptor")
	private boolean m_removeAll;

	public static final void main(String... args) throws Exception {
		main(new RemoveWorkflowCommand(), args);
	}
	
	public RemoveWorkflowCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		WorkflowManager svc = ((HttpMDTManager)mdt).getWorkflowManager();

		if ( m_removeAll ) {
			if ( m_model ) {
				svc.removeWorkflowModelAll();
			}
			else {
				svc.removeWorkflowAll();
			}
		}
		else if ( m_glob != null ) {
			String pattern = "glob:" + m_glob;
	        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
	        
			if ( m_model ) {
		        FStream.from(svc.getWorkflowModelAll())
				        .filter(wf -> matcher.matches(Paths.get(wf.getId())))
				        .forEach(wf -> svc.removeWorkflow(wf.getId()));
			}
			else {
		        FStream.from(svc.getWorkflowAll())
				        .filter(wf -> matcher.matches(Paths.get(wf.getName())))
				        .forEach(wf -> svc.removeWorkflow(wf.getName()));
			}
		}
		else if ( m_modelFilter != null ) {
			FStream.from(svc.getWorkflowAll())
					.filter(wf -> wf.getModelId().equals(m_modelFilter))
					.forEach(wf -> svc.removeWorkflow(wf.getName()));
		}
		else {
			if ( m_wfIds == null || m_wfIds.isEmpty() ) {
				if ( m_model ) {
					throw new IllegalArgumentException("no workflow template id specified");
				}
				else {
					throw new IllegalArgumentException("no workflow id specified");
				}
			}
			
			for ( String wfId: m_wfIds ) {
				if ( m_model ) {
                    svc.removeWorkflowModel(wfId);
                }
                else {
                	svc.removeWorkflow(wfId);
                }
			}
		}
	}
}
