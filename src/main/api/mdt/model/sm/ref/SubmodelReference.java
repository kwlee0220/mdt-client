package mdt.model.sm.ref;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelReference {
	public SubmodelService get() throws ResourceNotFoundException;
}
