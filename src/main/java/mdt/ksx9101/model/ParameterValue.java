package mdt.ksx9101.model;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.common.base.Preconditions;

import mdt.ksx9101.AasCRUDActions;
import mdt.model.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ParameterValue extends AasCRUDActions {
	public String getEntityId();
	public void setEntityId(String containerId);

	public String getParameterId();
	public void setParameterId(String parameterId);

	public String getParameterValue();
	public void setParameterValue(String value);

	public String getEventDateTime();
	public void setEventDateTime(String dateTime);

	public String getValidationResultCode();
	public void setValidationResultCode(String code);

	public default void update(String idShortPath, Object value) {
		List<String> pathSegs = SubmodelUtils.parseIdShortPath(idShortPath).toList();
		Preconditions.checkArgument(pathSegs.size() <= 1,
									"Unexpected idShortPath: path={}", idShortPath);
		
		if ( pathSegs.size() == 0 ) {
			
		}
		else {
			try {
				PropertyUtils.setSimpleProperty(this, pathSegs.get(0), value);
			}
			catch ( Exception e ) {
//				String msg = String.format("Failed to update ParameterValue: %s", value);
			}
		}
	}
}
