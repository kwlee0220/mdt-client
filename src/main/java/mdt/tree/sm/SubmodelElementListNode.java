package mdt.tree.sm;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.func.FOption;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubmodelElementListNode implements Node {
	private final String m_prefix;
	private SubmodelElementList m_list;
	
	public SubmodelElementListNode(String prefix, SubmodelElementList smel) {
		m_prefix = prefix;
		m_list = smel;
	}

	@Override
	public String getText() {
		return String.format("%s%s (SML)", m_prefix, FOption.getOrElse(m_list.getIdShort(), ""));
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return FStream.from(m_list.getValue())
						.zipWithIndex(0)
						.map(idxed -> SubmodelElementNodeFactory.toNode(
															String.format("[#%02d] ", idxed.index()),
															idxed.value()));
	}
}
