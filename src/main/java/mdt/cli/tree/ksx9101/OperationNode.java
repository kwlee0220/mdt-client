package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.tree.ListNode;
import mdt.ksx9101.model.Operation;
import mdt.ksx9101.model.ParametricEntity;
import mdt.ksx9101.model.ProductionOrder;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class OperationNode extends ParametricEntityNode {
	private final Operation m_operation;
	
	public OperationNode(Operation operation) {
		m_operation = operation;
	}

	@Override
	protected ParametricEntity getParametricEntity() {
		return m_operation;
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
