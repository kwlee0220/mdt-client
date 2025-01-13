package mdt.tree.sm.data;

import mdt.model.sm.data.Repair;
import mdt.tree.DefaultNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RepairNode extends DefaultNode {
	public RepairNode(Repair repair) {
		String title = String.format("Repair: 진단: %s:%s: %s, 발생공정: %s, item=%s",
										repair.getDefectRegOperationID(), repair.getDefectRegEquipmentID(),
										repair.getDefectRegDateTime(), repair.getDetectedProcess(),
										repair.getProductionItemSerialNO());
		setTitle(title);
	}
}