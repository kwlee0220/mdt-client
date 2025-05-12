package mdt.model.sm.data;

import java.util.List;

import mdt.model.TopLevelEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPlannings extends TopLevelEntity {
	public List<? extends ProductionPlanning> getElementAll();
	
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
