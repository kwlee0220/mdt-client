package mdt.cli.tree.ksx9101;

import utils.func.FOption;

import mdt.ksx9101.model.Equipment;
import mdt.ksx9101.model.ParametricEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class EquipmentNode extends ParametricEntityNode {
	private final Equipment m_equipment;
	
	public EquipmentNode(Equipment equipment) {
		m_equipment = equipment;
	}

	@Override
	protected ParametricEntity getParametricEntity() {
		return m_equipment;
	}

	@Override
	public String getText() {
		String nameStr = FOption.getOrElse(m_equipment.getEquipmentName(), m_equipment.getEquipmentId());
		return String.format("Equipment (%s)", nameStr);
	}
}
