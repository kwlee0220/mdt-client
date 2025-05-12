package mdt.tree;

import java.util.List;

import org.barfuin.texttree.api.Node;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class CompositeNode implements TitleUpdatableNode {
	private String m_title;
	private final List<? extends Node> m_children;
	
	public CompositeNode(String title, List<? extends Node> elements) {
		m_title = title;
		m_children = elements;
	}

	@Override
	public String getText() {
		return String.format("%s", m_title);
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return m_children;
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
