package mdt.tree.sm.data;

import mdt.model.sm.data.BOM;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class BOMNode extends DefaultNode {
	public BOMNode(BOM bom) {
		String uomStr = ( bom.getItemUOMCode() != null ) ? String.format("%s", bom.getItemUOMCode()) : "";
		String title = String.format("BOM (%s): Item=%s, quantity=%s%s",
									bom.getBOMType(), bom.getItemID(), bom.getBOMQuantity(), uomStr);
		setTitle(title);
	}
}