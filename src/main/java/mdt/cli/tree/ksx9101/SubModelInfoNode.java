package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.SubModelInfo;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubModelInfoNode implements Node {
	private SubModelInfo m_submodelInfo;
	
	public SubModelInfoNode(SubModelInfo info) {
		m_submodelInfo = info;
	}

	@Override
	public String getText() {
		return String.format("SubModelInfo (%s): creator=%s, format=%s",
							m_submodelInfo.getTitle(), m_submodelInfo.getCreator(), m_submodelInfo.getFormat());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}