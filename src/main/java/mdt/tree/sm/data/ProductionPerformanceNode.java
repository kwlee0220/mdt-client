package mdt.tree.sm.data;

import mdt.model.sm.data.ProductionPerformance;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ProductionPerformanceNode extends DefaultNode {
	public ProductionPerformanceNode(ProductionPerformance plan) {
		String title = String.format("ProductionPerformance: operation=%s, item=%s, quantity=%s",
										plan.getOperationID(), plan.getItemID(),
										plan.getProducedQuantity());
		setTitle(title);
	}
}