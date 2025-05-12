package mdt.model.sm.info;

import java.util.List;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionItems {
	public List<? extends CompositionItem> getElementAll();
	
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
