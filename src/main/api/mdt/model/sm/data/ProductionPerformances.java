package mdt.model.sm.data;

import java.util.List;

import mdt.model.TopLevelEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPerformances  extends TopLevelEntity {
	public List<? extends ProductionPerformance> getMemberList();

	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
