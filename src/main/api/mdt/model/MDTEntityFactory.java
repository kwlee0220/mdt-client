package mdt.model;

import mdt.model.sm.entity.SubmodelElementEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTEntityFactory {
	public SubmodelElementEntity newInstance(String id);
}
