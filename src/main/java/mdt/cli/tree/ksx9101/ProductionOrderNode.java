package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import utils.func.FOption;

import mdt.ksx9101.model.ProductionOrder;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProductionOrderNode implements Node {
	private ProductionOrder m_plan;
	
	public ProductionOrderNode(ProductionOrder plan) {
		m_plan = plan;
	}

	@Override
	public String getText() {
		String uomStr = FOption.getOrElse("" + m_plan.getItemUOMCode(), ""); 
		return String.format("%s: 공정(%s), %s(%s%s), schedule: %s~%s",
							m_plan.getProductionOrderID(), m_plan.getOperationID(),
							m_plan.getItemID(), m_plan.getProductionOrderQuantity(), uomStr,
							m_plan.getScheduleStartDateTime(), m_plan.getScheduleEndDateTime());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}