package mdt.tree.sm;

import java.util.Collections;
import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.collect.Lists;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class AASOperationNode implements Node {
	private final Operation m_operation;
	
	public AASOperationNode(Operation operation) {
		m_operation = operation;
	}

	@Override
	public String getText() {
		return String.format("%s", m_operation.getIdShort());
	}
	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = Lists.newArrayList();
		
		children.add(new VariableListNode("InputVariables", m_operation.getInputVariables()));
		children.add(new VariableListNode("InoutputVariables", m_operation.getInoutputVariables()));
		children.add(new VariableListNode("OutputVariables", m_operation.getOutputVariables()));
		
		return children;
	}
	
	public static class VariableNode implements Node {
		private final String m_prefix;
		private final OperationVariable m_opv;
		
		public VariableNode(String prefix, OperationVariable opv) {
			m_prefix = prefix;
			m_opv = opv;
		}

		@Override
		public String getText() {
			SubmodelElement val = m_opv.getValue();
//			String name = (val != null) ? val.getIdShort() : null;
			String text = (val != null) ? SubmodelElementNodeFactory.toNode("", val).getText() : null;
			return String.format("%s%s", m_prefix, text);
		}

		@Override
		public Iterable<? extends Node> getChildren() {
			return Collections.emptyList();
		}
	}

	public static class VariableListNode implements Node {
		private final String m_title;
		private final List<OperationVariable> m_vars;
		
		private VariableListNode(String title, List<OperationVariable> vars) {
			m_title = title;
			m_vars = vars;
		}
		
		@Override
		public String getText() {
			return m_title;
		}
		
		@Override
		public Iterable<? extends Node> getChildren() {
			return FStream.from(m_vars)
							.zipWithIndex(0)
							.map(t -> new VariableNode(String.format("[#%02d] ", t.index()),
																t.value()))
							.toList();
		}
	}
}
