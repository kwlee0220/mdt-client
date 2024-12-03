package mdt.tree.sm;

import java.util.Collections;

import org.barfuin.texttree.api.Node;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class TerminalNode implements Node {
	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
