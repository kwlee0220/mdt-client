package mdt.cli.tree;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubmodelElementCollectionNode implements Node {
	private final String m_prefix;
	private SubmodelElementCollection m_coll;
	
	public SubmodelElementCollectionNode(String prefix, SubmodelElementCollection prop) {
		m_prefix = prefix;
		m_coll = prop;
	}

	@Override
	public String getText() {
		return String.format("%s%s (SMC)", m_prefix, m_coll.getIdShort());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return FStream.from(m_coll.getValue())
						.flatMapNullable(sme -> SubmodelElementNodeFactory.toNode("", sme));
	}
}
