package mdt.tree.sm.data;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.model.sm.data.Andon;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class AndonNode implements Node {
	private Andon m_andon;
	
	public AndonNode(Andon andon) {
		m_andon = andon;
	}

	@Override
	public String getText() {
		return String.format("Andon: Operation=%s, Start=%s, End=%s, Cause=%s",
							m_andon.getOperationID(),
							m_andon.getStartDateTime().trim(),
							m_andon.getEndDateTime().trim(),
							m_andon.getCauseName());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}