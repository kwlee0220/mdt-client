package mdt.tree.sm.data;

import utils.func.FOption;

import mdt.model.sm.data.ProductionOrder;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProductionOrderNode extends DefaultNode {
	private ProductionOrder m_plan;
	
	public ProductionOrderNode(ProductionOrder plan) {
		String uomStr = FOption.getOrElse("" + m_plan.getItemUOMCode(), ""); 
		String title = String.format("%s: 공정(%s), %s(%s%s), schedule: %s~%s",
									m_plan.getProductionOrderID(), m_plan.getOperationID(),
									m_plan.getItemID(), m_plan.getProductionOrderQuantity(), uomStr,
									m_plan.getScheduleStartDateTime(), m_plan.getScheduleEndDateTime());
		setTitle(title);
	}
}