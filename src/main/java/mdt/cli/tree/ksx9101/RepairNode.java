package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.Repair;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RepairNode implements Node {
	private Repair m_repair;
	
	public RepairNode(Repair repair) {
		m_repair = repair;
	}

	@Override
	public String getText() {
		return String.format("Repair: 진단: %s:%s: %s, 발생공정: %s, item=%s",
							m_repair.getDefectRegOperationID(), m_repair.getDefectRegEquipmentID(),
							m_repair.getDefectRegDateTime(), m_repair.getDetectedProcess(),
							m_repair.getProductionItemSerialNO());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}