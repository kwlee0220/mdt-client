package mdt.ksx9101;

import mdt.ksx9101.model.Parameter;
import mdt.ksx9101.model.ParameterValue;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface KSX9101DataService extends SubmodelService {
	public String getParameterIdShortPath(String paramId) throws ResourceNotFoundException;
	public String getParameterValueIdShortPath(String paramId) throws ResourceNotFoundException;
	
	public Parameter getParameter(String id) throws ResourceNotFoundException;
	public ParameterValue getParameterValue(String id) throws ResourceNotFoundException;
}
