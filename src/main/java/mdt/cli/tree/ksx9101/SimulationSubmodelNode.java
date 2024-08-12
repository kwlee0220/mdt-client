package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.cli.tree.AASOperationNode;
import mdt.cli.tree.TextNode;
import mdt.ksx9101.simulation.Simulation;
import mdt.ksx9101.simulation.SimulationInfo;
import mdt.ksx9101.simulation.SimulationInput;
import mdt.ksx9101.simulation.SimulationOutput;
import mdt.ksx9101.simulation.SimulationTool;
import mdt.model.DataTypes;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SimulationSubmodelNode implements Node {
	private Simulation m_simulation;
	
	public SimulationSubmodelNode(Simulation simulation) {
		m_simulation = simulation;
	}

	@Override
	public String getText() {
		return "Simulation";
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = Lists.newArrayList();

		SimulationInfo simInfo = m_simulation.getSimulationInfo();
		
		SimulationTool tool = simInfo.getSimulationTool();
		children.add(new TextNode(String.format("SimulationTool: %s", tool.getSimToolName())));
		if ( tool.getSimulatorEndpoint() != null ) {
			children.add(new TextNode(String.format("SimulatorEndpoint: %s", tool.getSimulatorEndpoint())));
		}
		children.add(new TextNode(String.format("SimulationTimeout: %s",
											DataTypes.DURATION.toValueString(tool.getSimulationTimeout()))));
		children.add(new TextNode(String.format("SessionRetainTimeout: %s",
											DataTypes.DURATION.toValueString(tool.getSessionRetainTimeout()))));
		
		children.add(new InputsNode(simInfo.getInputs()));
		children.add(new OutputsNode(simInfo.getOutputs()));
		
		children.add(new AASOperationNode(m_simulation.getSimulationOperation()));
		
		return children;
	}
	
	private static final class InputsNode implements Node {
		private final List<SimulationInput> m_inputs;
		
		public InputsNode(List<SimulationInput> inputs) {
			m_inputs = inputs;
		}

		@Override
		public String getText() {
			return String.format("Inputs");
		}
		
		@Override
		public Iterable<? extends Node> getChildren() {
			return FStream.from(m_inputs)
							.map(in -> String.format("%s (%s): %s",
										in.getInputID(), in.getInputType(), in.getInputValue()))
							.map(TextNode::new)
							.toList();
		}
	}
	
	private static final class OutputsNode implements Node {
		private final List<SimulationOutput> m_outputs;
		
		public OutputsNode(List<SimulationOutput> outputs) {
			m_outputs = outputs;
		}

		@Override
		public String getText() {
			return String.format("Outputs");
		}
		
		@Override
		public Iterable<? extends Node> getChildren() {
			return FStream.from(m_outputs)
							.map(in -> String.format("%s (%s): %s",
										in.getOutputID(), in.getOutputType(), in.getOutputValue()))
							.map(TextNode::new)
							.toList();
		}
	}
}