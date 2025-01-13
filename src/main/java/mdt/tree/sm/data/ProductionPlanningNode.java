package mdt.tree.sm.data;

import mdt.model.sm.data.ProductionPlanning;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProductionPlanningNode extends DefaultNode {
	private ProductionPlanning m_plan;
	
	public ProductionPlanningNode(ProductionPlanning plan) {
		String title = String.format("ProductionPlanning: 물품: %s, 수량: %s, 계획: %s ~ %s",
										m_plan.getItemID(), m_plan.getProductionPlanQuantity(),
										m_plan.getScheduleStartDateTime(), m_plan.getScheduleEndDateTime());
		setTitle(title);
	}
}