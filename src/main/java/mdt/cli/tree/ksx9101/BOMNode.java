package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.BOM;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class BOMNode implements Node {
	private BOM m_bom;
	
	public BOMNode(BOM bom) {
		m_bom = bom;
	}

	@Override
	public String getText() {
		String uomStr = m_bom.getItemUOMCode() != null
							? String.format("%s", m_bom.getItemUOMCode()) : "";
		return String.format("BOM (%s): Item=%s, quantity=%s%s",
							m_bom.getBOMType(), m_bom.getItemID(), m_bom.getBOMQuantity(), uomStr);
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}