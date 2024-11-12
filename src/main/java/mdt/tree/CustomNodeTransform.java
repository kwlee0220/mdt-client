package mdt.tree;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@FunctionalInterface
public interface CustomNodeTransform {
	public Node toNode(String prefix, SubmodelElement sme);
}
