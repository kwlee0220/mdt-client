package mdt.workflow.model.argo;

import java.util.List;

import com.google.common.collect.Lists;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.NameValue;
import mdt.task.builtin.CopyTask;
import mdt.task.builtin.SetTask;
import mdt.workflow.model.Option;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.VariableDescriptor;
import mdt.workflow.model.WorkflowDescriptor;
import mdt.workflow.model.argo.ArgoContainerTemplateDescriptor.ContainerDescriptor;
import mdt.workflow.model.argo.ArgoDagTemplateDescriptor.DagDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ArgoTemplateDescriptorLoader {
//	private static final String MDT_CLIENT_IMAGE_ID = "kwlee0220/mdt-client";
	private static final List<String> COMMAND_JAVA = List.of("java");
	
	private final WorkflowDescriptor m_wfDesc;
	private final String m_mdtEndpoint;
	private final String m_mdtClientImageName;
	
	public ArgoTemplateDescriptorLoader(WorkflowDescriptor wfDesc, String mdtEndpoint, String mdtClientImageName) {
		m_wfDesc = wfDesc;
		m_mdtEndpoint = mdtEndpoint;
		m_mdtClientImageName = mdtClientImageName;
	}
	
	public List<ArgoTemplateDescriptor> load() {
		List<ArgoTemplateDescriptor> argoTemplates = Lists.newArrayList();
		FStream.from(m_wfDesc.getTasks())
				.map(this::toArgoContainerTemplate)
				.toCollection(argoTemplates);
		
		List<ArgoTaskDescriptor> argoTasks = Funcs.map(m_wfDesc.getTasks(), this::toArgoTask);
		ArgoDagTemplateDescriptor dagTemplate = new ArgoDagTemplateDescriptor("dag",
																		new DagDescriptor(argoTasks));
		argoTemplates.add(0, dagTemplate);
		
		return argoTemplates;
	}
	
	private ArgoTaskDescriptor toArgoTask(TaskDescriptor task) {
		String tmpltId = task.getId() + "-template";
		return new ArgoTaskDescriptor(task.getId(), tmpltId, task.getDependencies());
	}
	
	private ArgoContainerTemplateDescriptor toArgoContainerTemplate(TaskDescriptor task) {
		ContainerDescriptor container = toContainerDescriptor(task);
		return new ArgoContainerTemplateDescriptor(task.getId()+"-template", container);
	}
	
	private ContainerDescriptor toContainerDescriptor(TaskDescriptor task) {
		String taskType = task.getType();
		
		List<String> args = Lists.newArrayList("-cp", "mdt-client-all.jar", taskType + "Command");

		if ( taskType.equals(SetTask.class.getName()) ) {
			addSetTaskParameters(task, args);
		}
		else if ( taskType.equals(CopyTask.class.getName()) ) {
			addCopyTaskParameters(task, args);
		}
		
		FStream.from(task.getOptions())
				.flatMapIterable(Option::toCommandOptionSpec)
				.forEach(args::add);
		
		// 'VARIABLE' type의 input port의 경우에는 dependent task에서 생성한 output parameter 값을
		// command line 인자로 전달된다.
		
		if ( taskType.equals(SetTask.class.getName()) ) {
			addSetTaskOptions(task, args);
		}
		else if ( taskType.equals(CopyTask.class.getName()) ) {
			addCopyTaskOptions(task, args);
		}
		else {
			for ( VariableDescriptor var: task.getInputVariables() ) {
				args.add(String.format("--in.%s", var.getName()));
				args.add(var.getValueReference().toStringExpr());
			}
			for ( VariableDescriptor var: task.getOutputVariables() ) {
				args.add(String.format("--out.%s", var.getName()));
				args.add(var.getValueReference().toStringExpr());
			}
		}
		
		List<NameValue> environs = List.of(
			new NameValue("MDT_ENDPOINT", m_mdtEndpoint)
		);

		return new ContainerDescriptor(m_mdtClientImageName, COMMAND_JAVA, args, environs);
	}
	
	private void addSetTaskParameters(TaskDescriptor task, List<String> args) {
		VariableDescriptor to = task.getOutputVariables().getOfKey("to");
		args.add(to.getValueReference().toStringExpr());
	}
	private void addSetTaskOptions(TaskDescriptor task, List<String> args) { }
	
	private void addCopyTaskParameters(TaskDescriptor task, List<String> args) {
		VariableDescriptor from = task.getInputVariables().getOfKey("from");
		args.add(from.getValueReference().toStringExpr());
		
		VariableDescriptor to = task.getOutputVariables().getOfKey("to");
		args.add(to.getValueReference().toStringExpr());
	}
	private void addCopyTaskOptions(TaskDescriptor task, List<String> args) { }
}
