package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.ProductionPlanning;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProductionPlanningNode implements Node {
	private ProductionPlanning m_plan;
	
	public ProductionPlanningNode(ProductionPlanning plan) {
		m_plan = plan;
	}

	@Override
	public String getText() {
		return String.format("ProductionPlanning: 물품: %s, 수량: %s, 계획: %s ~ %s",
							m_plan.getItemID(), m_plan.getProductionPlanQuantity(),
							m_plan.getScheduleStartDateTime(), m_plan.getScheduleEndDateTime());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}