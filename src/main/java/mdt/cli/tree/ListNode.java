package mdt.cli.tree;

import java.util.List;
import java.util.function.Function;

import org.barfuin.texttree.api.Node;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ListNode<T> implements Node {
	private final String m_title;
	private final List<? extends T> m_list;
	private final Function<T,Node> m_toNode;
	
	public ListNode(String title, List<? extends T> list, Function<T,Node> toNode) {
		m_title = title;
		m_list = list;
		m_toNode = toNode;
	}

	@Override
	public String getText() {
		return String.format("%s (%d)", m_title, m_list.size());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return FStream.from(m_list).map(m_toNode).toList();
	}
}
