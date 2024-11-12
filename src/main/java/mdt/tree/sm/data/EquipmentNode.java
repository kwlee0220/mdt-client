package mdt.tree.sm.data;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.func.FOption;

import mdt.model.sm.data.DefaultEquipment;
import mdt.model.sm.data.Equipment;
import mdt.tree.CustomNodeTransform;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class EquipmentNode extends ParameterCollectionNode {
	private final String m_prefix;
	private final Equipment m_equipment;
	
	public static class Transform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultEquipment pv = new DefaultEquipment();
			pv.updateFromAasModel(sme);
			return new EquipmentNode(prefix, pv);
		}
	}
	
	public EquipmentNode(String prefix, Equipment equipment) {
		super(equipment);
		
		m_prefix = prefix;
		m_equipment = equipment;
	}

	@Override
	public String getText() {
		String nameStr = FOption.getOrElse(m_equipment.getEquipmentName(), m_equipment.getEquipmentId());
		return String.format("%sEquipment (%s)", m_prefix, nameStr);
	}
}
