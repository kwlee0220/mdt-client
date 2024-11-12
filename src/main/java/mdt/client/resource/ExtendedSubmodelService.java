package mdt.client.resource;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.value.PropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ExtendedSubmodelService implements SubmodelService {
	@Delegate private final SubmodelService m_submodelService;
	
	private ExtendedSubmodelService(SubmodelService svc) {
		m_submodelService = svc;
	}
	
	public static ExtendedSubmodelService from(SubmodelService svc) {
		return new ExtendedSubmodelService(svc);
	}
	
	public <T> T getPropertyValueByPath(String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = SubmodelUtils.cast(getSubmodelElementByPath(idShortPath), Property.class);
		return SubmodelUtils.getPropertyValue(prop, valueClass);
	}

	public void setPropertyValueByPath(String idShortPath, String value) {
		Preconditions.checkArgument(idShortPath != null);
		
		SubmodelElement sme = getSubmodelElementByPath(idShortPath);
		if ( sme instanceof Property ) {
			patchSubmodelElementValueByPath(idShortPath, new PropertyValue(value));
		}
		else {
			throw new ResourceNotFoundException("Property", "path=" + idShortPath);
		}
	}
}
