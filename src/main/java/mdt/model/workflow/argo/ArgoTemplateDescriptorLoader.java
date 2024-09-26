package mdt.model.workflow.argo;

import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.Lists;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.model.NameValue;
import mdt.model.workflow.argo.ArgoContainerTemplateDescriptor.ContainerDescriptor;
import mdt.model.workflow.argo.ArgoContainerTemplateDescriptor.InputsDescriptor;
import mdt.model.workflow.argo.ArgoContainerTemplateDescriptor.NameDescriptor;
import mdt.model.workflow.argo.ArgoContainerTemplateDescriptor.OutputsDescriptor;
import mdt.model.workflow.argo.ArgoDagTemplateDescriptor.DagDescriptor;
import mdt.model.workflow.argo.OutputParameterBinding.ValueFrom;
import mdt.model.workflow.descriptor.OptionDescriptor;
import mdt.model.workflow.descriptor.ParameterDescriptor;
import mdt.model.workflow.descriptor.TaskDescriptor;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;
import mdt.model.workflow.descriptor.port.FilePortDescriptor;
import mdt.model.workflow.descriptor.port.LiteralPortDescriptor;
import mdt.model.workflow.descriptor.port.PortDescriptor;
import mdt.model.workflow.descriptor.port.PortType;
import mdt.model.workflow.descriptor.port.WorkflowVariablePortDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ArgoTemplateDescriptorLoader {
	private static final String IMAGE_ID = "kwlee0220/mdt-client";
	private static final List<String> COMMAND_JAVA = List.of("java");
	
	private final WorkflowDescriptor m_wfDesc;
	private final String m_mdtEndpoint;
	
	public ArgoTemplateDescriptorLoader(WorkflowDescriptor wfDesc, String mdtEndpoint) {
		m_wfDesc = wfDesc;
		m_mdtEndpoint = mdtEndpoint;
	}
	
	public List<ArgoTemplateDescriptor> load() {
		List<ArgoTemplateDescriptor> argoTemplates = Lists.newArrayList();
		FStream.from(m_wfDesc.getTaskTemplates())
				.filter(tmlt -> !m_wfDesc.isBuiltInTaskTemplate(tmlt.getId()))
				.map(this::toArgoContainerTemplate)
				.toCollection(argoTemplates);
		
		List<ArgoTaskDescriptor> argoTasks = Funcs.map(m_wfDesc.getTasks(), this::toArgoTask);
		ArgoDagTemplateDescriptor dagTemplate = new ArgoDagTemplateDescriptor("dag",
																		new DagDescriptor(argoTasks));
		argoTemplates.add(0, dagTemplate);
		
		return argoTemplates;
	}
	
	private ArgoTaskDescriptor toArgoTask(TaskDescriptor task) {
		TaskTemplateDescriptor taskTmplt = m_wfDesc.getTaskTemplates().getOfKey(task.getTemplate());
		Map<String,String> variables = buildVariables(task);
		StringSubstitutor subst = new StringSubstitutor(variables);

		List<NameValue> args = task.getArguments();
		FStream.from(taskTmplt.findInputPortsOfType(PortType.VARIABLE))
				.map(pd -> {
					WorkflowVariablePortDescriptor wpd = (WorkflowVariablePortDescriptor)pd;
					String value = String.format("{{tasks.%s.outputs.parameters.%s}}",
													wpd.getHolder(), wpd.getVariable());
					value = subst.replace(value);
					
					return new NameValue(String.format("varport-%s", pd.getName()), value);
				})
				.forEach(nv -> args.add(nv));

		ArgoArgumentsDescriptor argsDesc = new ArgoArgumentsDescriptor(args);
		return new ArgoTaskDescriptor(task.getId(), task.getTemplate(), task.getDependencies(), argsDesc);
	}
	
	private ArgoContainerTemplateDescriptor toArgoContainerTemplate(TaskTemplateDescriptor taskTmplt) {
		InputsDescriptor inputs = new InputsDescriptor();

		List<NameDescriptor> paramNames = FStream.from(taskTmplt.getParameters())
												.map(ParameterDescriptor::getName)
												.map(NameDescriptor::new)
												.toList();
		FStream.from(taskTmplt.findInputPortsOfType(PortType.VARIABLE))
				.forEach(pd -> {
					String varPortName = String.format("varport-%s", pd.getName());
					paramNames.add(new NameDescriptor(varPortName));
				});
		if ( paramNames.size() > 0 ) {
			inputs.setParameters(paramNames);
		}
		
		OutputsDescriptor outputs = new OutputsDescriptor();
		List<OutputParameterBinding> outParams
			= FStream.from(taskTmplt.findOutputPortsOfType(PortType.VARIABLE))
					.map(pd -> {
//						VariablePortDescriptor varDesc = (VariablePortDescriptor)pd.getValueReference();
						ValueFrom from = new ValueFrom(String.format("/tmp/%s", pd.getName()));
						return new OutputParameterBinding(pd.getName(), from);
					})
					.toList();
		if ( outParams.size() > 0 ) {
			outputs.setParameters(outParams);
		}
		
		ContainerDescriptor container = toContainerDescriptor(taskTmplt);
		return new ArgoContainerTemplateDescriptor(taskTmplt.getId(), inputs, outputs, container);
	}
	
	private ContainerDescriptor toContainerDescriptor(TaskTemplateDescriptor taskTmplt) {
		Map<String,String> variables = buildVariables(taskTmplt);
		StringSubstitutor subst = new StringSubstitutor(variables);
		
		String taskCommand = taskTmplt.getType() + "Command";
		List<String> args = Lists.newArrayList("-cp", "mdt-client-all.jar", taskCommand);
		
		for ( OptionDescriptor opt: taskTmplt.getOptions() ) {
			args.add("--" + opt.getName());
			args.add(subst.replace(opt.getValue()));
		}
		
		// 'VARIABLE' type의 input port의 경우에는 dependent task에서 생성한 output parameter 값을
		// command line 인자로 전달된다.
		
		for ( PortDescriptor port: taskTmplt.getInputPorts() ) {
			args.add(String.format("--in.%s", port.getName()));

			String refStr = port.toStringExpr();
			if ( port.getPortType() == PortType.VARIABLE ) {
				String literal = String.format("{{varport-%s}}", port.getName());
				refStr = new LiteralPortDescriptor(port.getName(), port.getDescription(), literal,
													port.isValueOnly()).toStringExpr();
			}
			args.add(subst.replace(refStr));
		}

		for ( PortDescriptor port: taskTmplt.getOutputPorts() ) {
			args.add(String.format("--out.%s", port.getName()));

			String refStr = port.toStringExpr();
			if ( port.getPortType() == PortType.VARIABLE ) {
				String varFilePath = String.format("/tmp/%s", port.getName());
				refStr = new FilePortDescriptor(port.getName(), port.getDescription(), varFilePath,
												port.isValueOnly()).toStringExpr();
			}
			args.add(subst.replace(refStr));
		}
		
		List<NameValue> environs = List.of(new NameValue("MDT_ENDPOINT", m_mdtEndpoint));

		return new ContainerDescriptor(IMAGE_ID, COMMAND_JAVA, args, environs);
	}
	
	private Map<String,String> buildVariables(TaskTemplateDescriptor taskTmplt) {
		// WorkflowDescriptor에서 정의된 parameter를 먼저 반영한다.
		Map<String,String> variables = FStream.from(m_wfDesc.getParameters())
											.map(ParameterDescriptor::getName)
											.toMap(n -> n, n -> String.format("{{workflow.parameters.%s}}", n));
		
		// TaskTemplateDescriptor에 정의된 parameter를 추가한다.
		// WorkflowDescriptor에 동일 이름의 parameter가 존재하는 경우에는 override하게 됨.
		if ( taskTmplt.getParameters() != null ) {
			FStream.from(taskTmplt.getParameters())
					.map(ParameterDescriptor::getName)
					.forEach(n -> variables.put(n,  String.format("{{inputs.parameters.%s}}", n)));
		}
		
		return variables;
	}
	
	private Map<String,String> buildVariables(TaskDescriptor task) {
		TaskTemplateDescriptor taskTemplate = m_wfDesc.getTaskTemplates().getOfKey(task.getTemplate());
		Map<String,String> variables = buildVariables(taskTemplate);
		for ( NameValue nv: task.getArguments() ) {
			variables.put(nv.getName(), nv.getValue());
		}
		
		return variables;
	}
}
