package mdt.ksx9101.model;

import java.util.List;

import mdt.ksx9101.TopLevelEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ProductionPlannings extends TopLevelEntity {
	public List<? extends ProductionPlanning> getElements();
	
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
