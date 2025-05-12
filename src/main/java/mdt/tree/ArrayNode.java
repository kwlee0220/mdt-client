package mdt.tree;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ArrayNode implements TitleUpdatableNode {
	private String m_title;
	private final List<? extends TitleUpdatableNode> m_children;
	
	public ArrayNode(String title, List<? extends TitleUpdatableNode> elements) {
		m_title = title;
		m_children = FStream.from(elements)
							.zipWithIndex()
							.map(idxed -> {
								TitleUpdatableNode node = idxed.value();
								node.setTitle(String.format("[#%02d] %s", idxed.index(), node.getTitle()));
								return node;
							})
							.toList();
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
