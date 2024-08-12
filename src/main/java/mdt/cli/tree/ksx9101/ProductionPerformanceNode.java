package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.ProductionPerformance;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProductionPerformanceNode implements Node {
	private ProductionPerformance m_plan;
	
	public ProductionPerformanceNode(ProductionPerformance plan) {
		m_plan = plan;
	}

	@Override
	public String getText() {
		return String.format("ProductionPerformance: operation=%s, item=%s, quantity=%s",
							m_plan.getOperationID(), m_plan.getItemID(),
							m_plan.getProducedQuantity());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}