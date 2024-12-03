package mdt.tree;

import java.util.Collections;

import org.barfuin.texttree.api.Node;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TextNode implements Node {
	private String m_text;
	
	public TextNode(String text) {
		m_text = text;
	}

	@Override
	public String getText() {
		return m_text;
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
