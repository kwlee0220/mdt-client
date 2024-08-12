package mdt.cli.tree;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementNodeFactory {
	public static Node toNode(String prefix, SubmodelElement smElm) {
		if ( smElm instanceof Property p ) {
			String value = p.getValue() != null ?  p.getValue().trim() : null;
			return (value == null || value.length() == 0) ? null : new PropertyNode(prefix, p);
		}
		else if ( smElm instanceof SubmodelElementCollection sec) {
			return new SubmodelElementCollectionNode(prefix, sec);
		}
		else if ( smElm instanceof SubmodelElementList smel) {
			return new SubmodelElementListNode(prefix, smel);
		}
		else if ( smElm instanceof Operation op) {
			return new AASOperationNode(op);
		}

		return null;
	}
}
