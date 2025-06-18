package mdt.tree.node;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class ListNode extends DefaultNode {
	private List<? extends DefaultNode> m_children;
	
	abstract protected List<? extends DefaultNode> getElementNodes();

	public ListNode() {
		setValue("");
	}
	public ListNode(String title) {
		super(title, null, "");
	}

	@Override
	public List<? extends Node> getChildren() {
		if ( m_children == null ) {
			m_children = FStream.from(getElementNodes())
								.zipWithIndex()
								.map(idxed -> {
									DefaultNode node = idxed.value();
									node.setPrefix(String.format("[#%02d] ", idxed.index()));
									return node;
								})
								.toList();
		}
		
		return m_children;
	}
}