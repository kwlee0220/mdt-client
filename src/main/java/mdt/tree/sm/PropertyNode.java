package mdt.tree.sm;

import java.util.Collections;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class PropertyNode implements Node {
	private final String m_prefix;
	private Property m_prop;
	
	public PropertyNode(String prefix, Property prop) {
		m_prefix = prefix;
		m_prop = prop;
	}

	@Override
	public String getText() {
		if ( m_prop.getValueType() != null ) {
			return String.format("%s%s (%s): %s", m_prefix, m_prop.getIdShort(),
								m_prop.getValueType(), m_prop.getValue());
		}
		else {
			return String.format("%s%s: %s", m_prefix, m_prop.getIdShort(), m_prop.getValue());
		}
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
