package mdt.tree;

import org.barfuin.texttree.api.Node;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TitleUpdatableNode extends Node {
	public String getTitle();
	public void setTitle(String title);

	public default String getText() {
		return getTitle();
	}
}
