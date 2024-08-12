package mdt.cli.tree;

import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Lists;

import utils.InternalException;
import utils.stream.FStream;

import mdt.model.resource.value.ElementValues;
import mdt.model.resource.value.SubmodelElementValue;


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

	public static final JsonMapper MAPPER = new JsonMapper();
	private static class VariableListNode implements Node {
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
							.zipWithIndex(1)
							.map(t -> new TextNode(String.format("%02d: %s", t.index(), toVariableValue(t.value()))))
							.toList();
		}
		
		private String toVariableValue(OperationVariable var) {
			SubmodelElementValue smev = ElementValues.getValue(var.getValue());
			try {
				return MAPPER.writeValueAsString(smev.toJsonObject());
			}
			catch ( JsonProcessingException e ) {
				throw new InternalException("Failed to write Json for SubmodelElementValue: " + smev);
			}
		}
	}
}
