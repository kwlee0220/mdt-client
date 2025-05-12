package mdt.tree.sm.data;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.sm.data.Operation;
import mdt.tree.ArrayNode;
import mdt.tree.TitleUpdatableNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProcessNode extends ParameterCollectionNode implements TitleUpdatableNode {
	private final Operation m_operation;
	private String m_title;
	
	public ProcessNode(Operation operation) {
		super(operation);
		m_operation = operation;

		String nameStr = FOption.getOrElse(operation.getOperationName(), operation.getOperationId());
		m_title = String.format("Operation (%s)", nameStr);
	}

	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public void setTitle(String title) {
		m_title = title;
	}
	
	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = FStream.from(super.getChildren()).cast(Node.class).toList();
		
		List<ProductionOrderNode> orders = FStream.from(m_operation.getProductionOrders())
											        .map(ProductionOrderNode::new)
											        .toList();
		children.add(new ArrayNode("ProductionOrders", orders));
		
		return children;
	}
}
