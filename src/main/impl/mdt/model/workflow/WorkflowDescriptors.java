package mdt.model.workflow;

import lombok.experimental.UtilityClass;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.service.AISubmodelService;
import mdt.model.service.SimulationSubmodelService;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.simulation.Simulation;
import mdt.task.builtin.CopyTask;
import mdt.task.builtin.SetTask;
import mdt.workflow.model.TaskDescriptor;
import mdt.workflow.model.ValueReferenceDescriptor;
import mdt.workflow.model.VariableDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class WorkflowDescriptors {
	public static TaskDescriptor newSetTask(String id, String value, String toRefString) {
		TaskDescriptor set = new TaskDescriptor();
		set.setId(id);
		set.setType(SetTask.class.getName());
		
		set.getOptions().add(new StringOption("value", value));

		VariableDescriptor to = VariableDescriptor.declare("to");
		to.setValueReference(ValueReferenceDescriptor.parseString(toRefString));
		set.getOutputVariables().add(to);
		
		return set;
	}
	
	public static TaskDescriptor newCopyTask(String id, String fromRefString, String toRefString) {
		TaskDescriptor copy = new TaskDescriptor();
		copy.setId(id);
		copy.setType(CopyTask.class.getName());
		
		VariableDescriptor from = VariableDescriptor.declare("from");
		from.setValueReference(ValueReferenceDescriptor.parseString(fromRefString));
		copy.getInputVariables().add(from);

		VariableDescriptor to = VariableDescriptor.declare("to");
		to.setValueReference(ValueReferenceDescriptor.parseString(toRefString));
		copy.getOutputVariables().add(to);
		
		return copy;
	}
	
	public static void addSimulationInputOutputVariables(TaskDescriptor task, DefaultSubmodelReference ref) {
		Simulation simulation = new SimulationSubmodelService(ref.get()).getSimulation();
		
		int idx = 0;
		for ( Input input: simulation.getSimulationInfo().getInputs() ) {
			VariableDescriptor var = VariableDescriptor.declare(input.getInputID());
			
			ValueReferenceDescriptor valDesc = new ValueReferenceDescriptor();
			valDesc.setTwinId(ref.getInstanceId());
			valDesc.setSubmodelIdShort(ref.getSubmodelIdShort());
			valDesc.setIdShortPath(String.format("SimulationInfo.Inputs[%d].InputValue", idx));
			var.setValueReference(valDesc);
			task.getInputVariables().add(var);
			
			++idx;
		}
		
		idx = 0;
		for ( Output input: simulation.getSimulationInfo().getOutputs() ) {
			VariableDescriptor var = VariableDescriptor.declare(input.getOutputID());
			
			ValueReferenceDescriptor valDesc = new ValueReferenceDescriptor();
			valDesc.setTwinId(ref.getInstanceId());
			valDesc.setSubmodelIdShort(ref.getSubmodelIdShort());
			valDesc.setIdShortPath(String.format("SimulationInfo.Outputs[%d].OutputValue", idx));
			var.setValueReference(valDesc);
			task.getOutputVariables().add(var);
			
			++idx;
		}
	}
	
	public static void addAIInputOutputVariables(TaskDescriptor task, DefaultSubmodelReference ref) {
		AI ai = new AISubmodelService(ref.get()).getAI();
		
		int idx = 0;
		for ( Input input: ai.getAIInfo().getInputs() ) {
			VariableDescriptor var = VariableDescriptor.declare(input.getInputID());
			
			ValueReferenceDescriptor valDesc = new ValueReferenceDescriptor();
			valDesc.setTwinId(ref.getInstanceId());
			valDesc.setSubmodelIdShort(ref.getSubmodelIdShort());
			valDesc.setIdShortPath(String.format("AIInfo.Inputs[%d].InputValue", idx));
			var.setValueReference(valDesc);
			task.getInputVariables().add(var);
			
			++idx;
		}
		
		idx = 0;
		for ( Output input: ai.getAIInfo().getOutputs() ) {
			VariableDescriptor var = VariableDescriptor.declare(input.getOutputID());
			
			ValueReferenceDescriptor valDesc = new ValueReferenceDescriptor();
			valDesc.setTwinId(ref.getInstanceId());
			valDesc.setSubmodelIdShort(ref.getSubmodelIdShort());
			valDesc.setIdShortPath(String.format("AIInfo.Outputs[%d].OutputValue", idx));
			var.setValueReference(valDesc);
			task.getOutputVariables().add(var);
			
			++idx;
		}
	}
}
