package mdt.tree.sm.data;

import mdt.model.sm.data.ItemMaster;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ItemMasterNode extends DefaultNode {
	public ItemMasterNode(ItemMaster itemMaster) {
		String title = String.format("Item: %s (%s)", itemMaster.getItemName(), itemMaster.getItemType());
		setTitle(title);
	}
}