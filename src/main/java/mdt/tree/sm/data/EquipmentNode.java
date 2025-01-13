package mdt.tree.sm.data;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.func.FOption;

import mdt.model.sm.data.DefaultEquipment;
import mdt.model.sm.data.Equipment;
import mdt.tree.CustomNodeTransform;
import mdt.tree.TitleUpdatableNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class EquipmentNode extends ParameterCollectionNode implements TitleUpdatableNode {
	private String m_title;
	
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

		String nameStr = FOption.getOrElse(equipment.getEquipmentName(), equipment.getEquipmentId());
		m_title = String.format("%sEquipment (%s)", prefix, nameStr);
	}

	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public void setTitle(String title) {
		m_title = title;
	}
}
