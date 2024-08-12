package mdt.ksx9101.model;

import java.util.List;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionItems {
	public List<? extends CompositionItem> getElements();
	
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
