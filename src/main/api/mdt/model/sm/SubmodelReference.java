package mdt.model.sm;

import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelReference {
	public SubmodelService get() throws ResourceNotFoundException;
}
