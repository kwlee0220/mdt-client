package mdt.model.sm.data;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import com.google.common.base.Preconditions;

import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.entity.AasCRUDActions;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ParameterValue extends AasCRUDActions {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/Data/ParameterValue/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
	public String getEntityId();
	public void setEntityId(String containerId);

	/**
	 * 파라미터의 식별자를 반환한다.
	 * 
	 * @return	파라미터 식별자.
	 */
	public String getParameterId();
	/**
	 * 파라미터의 식별자를 설정한다.
	 * 
	 * @param parameterId	설정할 파라미터 식별자.
	 */
	public void setParameterId(String parameterId);

	public SubmodelElement getParameterValue();
	public void setParameterValue(SubmodelElement value);
	public default void setParameterValue(SubmodelElementValue value) {
		SubmodelElement prev = getParameterValue();
		ElementValues.update(prev, value);
		setParameterValue(prev);
	}

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
