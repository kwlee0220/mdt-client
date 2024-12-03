package mdt.tree.sm.data;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ProductionOrder;
import mdt.tree.ListNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProcessNode extends ParameterCollectionNode {
	private final Operation m_operation;
	
	public ProcessNode(Operation operation) {
		super(operation);
		
		m_operation = operation;
	}

	@Override
	public String getText() {
		String nameStr = FOption.getOrElse(m_operation.getOperationName(), m_operation.getOperationId());
		return String.format("Operation (%s)", nameStr);
	}
	
	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = FStream.from(super.getChildren()).cast(Node.class).toList();
		ListNode<ProductionOrder> orders = new ListNode<>("ProductionOrders",
														m_operation.getProductionOrders(),
														ProductionOrderNode::new);
		children.add(orders);
		
		return children;
	}
}
