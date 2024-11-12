package mdt.task;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.KeyedValueList;
import utils.LoggerSettable;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.aas.DefaultSubmodelReference;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.AISubmodelService;
import mdt.model.service.SimulationSubmodelService;
import mdt.model.sm.DefaultSubmodelElementReference;
import mdt.model.sm.ai.AI;
import mdt.model.sm.simulation.Simulation;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class MultiParameterTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(MultiParameterTask.class);
	
	protected final KeyedValueList<String,Parameter> m_parameters;
	protected final Set<String> m_outputParameterNames;
	
	private Logger m_logger;
	
	protected MultiParameterTask(KeyedValueList<String,Parameter> parameters,
								Set<String> outputParameterNames) {
		List<String> unmatchedParameterNames = FStream.from(outputParameterNames)
														.filter(n -> !parameters.containsKey(n))
														.toList();
		if ( unmatchedParameterNames.size() > 0 ) {
			String names = FStream.from(unmatchedParameterNames).join(", ");
			String msg = String.format("Unmatched output parameter: %s", names);
			throw new IllegalArgumentException(msg);
		}
		
		m_parameters = parameters;
		m_outputParameterNames = outputParameterNames;
	}
	
	public KeyedValueList<String,Parameter> getParameterList() {
		return m_parameters;
	}
	
	public Set<String> getOutputParameterNameList() {
		return m_outputParameterNames;
	}
	
	public void loadSimulationParameters(MDTInstanceManager manager, DefaultSubmodelReference smRef) {
		smRef.activate(manager);
		
		SimulationSubmodelService svc = new SimulationSubmodelService(smRef.get());
		Simulation simulation = svc.getSimulation();
		
		FStream.from(simulation.getSimulationInfo().getInputs())
				.zipWithIndex()
				.forEach(idxed -> {
					Input input = idxed.value();
					String path = String.format("SimulationInfo.Inputs[%d].InputValue", idxed.index());
					DefaultSubmodelElementReference valRef = DefaultSubmodelElementReference.newInstance(smRef, path);
					valRef.activate(manager);
					
					m_parameters.addIfAbscent(Parameter.of(input.getInputID(), valRef));
				});

		FStream.from(simulation.getSimulationInfo().getOutputs())
				.zipWithIndex()
				.forEach(idxed -> {
					Output output = idxed.value();
					String path = String.format("SimulationInfo.Outputs[%d].OutputValue", idxed.index());
					DefaultSubmodelElementReference valRef = DefaultSubmodelElementReference.newInstance(smRef, path);
					valRef.activate(manager);
					
					m_parameters.addIfAbscent(Parameter.of(output.getOutputID(), valRef));
					m_outputParameterNames.add(output.getOutputID());
				});
	}
	
	public void loadAiParameters(MDTInstanceManager manager, DefaultSubmodelReference smRef) {
		smRef.activate(manager);
		
		AISubmodelService svc = new AISubmodelService(smRef.get());
		AI ai = svc.getAI();
		
		FStream.from(ai.getAIInfo().getInputs())
				.zipWithIndex()
				.forEach(idxed -> {
					Input input = idxed.value();
					String path = String.format("AIInfo.Inputs[%d].InputValue", idxed.index());
					DefaultSubmodelElementReference valRef = DefaultSubmodelElementReference.newInstance(smRef, path);
					valRef.activate(manager);
					
					m_parameters.addIfAbscent(Parameter.of(input.getInputID(), valRef));
				});

		FStream.from(ai.getAIInfo().getOutputs())
				.zipWithIndex()
				.forEach(idxed -> {
					Output output = idxed.value();
					String path = String.format("AIInfo.Outputs[%d].OutputValue", idxed.index());
					DefaultSubmodelElementReference valRef = DefaultSubmodelElementReference.newInstance(smRef, path);
					valRef.activate(manager);
					
					m_parameters.addIfAbscent(Parameter.of(output.getOutputID(), valRef));
					m_outputParameterNames.add(output.getOutputID());
				});
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
}
