package mdt.tree;

import java.util.Collections;

import org.barfuin.texttree.api.Node;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultNode implements TitleUpdatableNode {
	private String m_title;
	
	public DefaultNode(String title) {
		m_title = title;
	}
	
	public DefaultNode() {
		m_title = "";
	}

	@Override
	public String getText() {
		return m_title;
	}

	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public void setTitle(String title) {
		m_title = title;
	}
	
	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
