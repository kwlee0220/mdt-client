package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import com.google.common.collect.Lists;

import mdt.ksx9101.model.Line;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class LineNode implements Node {
	private Line m_line;
	
	public LineNode(Line line) {
		m_line = line;
	}

	@Override
	public String getText() {
		return String.format("Line (%s), %s", m_line.getLineID(), m_line.getLineStatus());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = Lists.newArrayList();
		
//		if ( m_line.getBOMs().size() > 0 ) {
//			ListNode<BOM> boms = new ListNode<>("BOMs", m_line.getBOMs(), BOMNode::new);
//			children.add(boms);
//		}
//		if ( m_line.getItemMasters().size() > 0 ) {
//			ListNode<ItemMaster> repairs = new ListNode<>("ItemMasters", m_line.getItemMasters(), ItemMasterNode::new);
//			children.add(repairs);
//		}
//		if ( m_line.getRoutings().size() > 0 ) {
//			ListNode<Routing> repairs = new ListNode<>("Routings", m_line.getRoutings(), RoutingNode::new);
//			children.add(repairs);
//		}
		
		return children;
	}
}
