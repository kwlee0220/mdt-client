package mdt.model.sm.data;

import java.util.List;

import mdt.model.TopLevelEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface BOMs extends TopLevelEntity {
	public List<? extends BOM> getElementAll();

	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
