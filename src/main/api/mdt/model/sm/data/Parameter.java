package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import javax.annotation.Nullable;

import utils.Keyed;

import mdt.model.MDTSemanticIds;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Parameter extends Keyed<String> {
	public static final String SEMANTIC_ID = MDTSemanticIds.PARAMETER;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public String getEntityId();
	public void setEntityId(String containerId);
	
	public default Reference getSemanticId() {
		return SEMANTIC_ID_REFERENCE;
	}
	
	public default String key() {
		return getParameterId();
	}

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

	/**
	 * 파라미터의 이름을 반환한다.
	 * 
	 * @return	파라미터 이름.
	 */
	public String getParameterName();
	/**
	 * 파라미터의 이름을 설정한다.
	 * 
	 * @param parameterName	설정할 파라미터 이름.
	 */
	public void setParameterName(String parameterName);

	public String getParameterType();
	public void setParameterType(String typeName);

	public String getParameterGrade();
	public void setParameterGrade(String grade);

	/**
	 * 파라미터의 측정 단위 (Unit of Measurement) 코드를 반환한다.
	 * 
	 * @return	측정 단위 코드
	 */
	public @Nullable String getParameterUOMCode();
	/**
	 * 파라미터의 측정 단위 (Unit of Measurement) 코드를 설정한다.
	 * 
	 * @param uomCode	설정할 측정 단위 코드
	 */
	public void setParameterUOMCode(String uomCode);

	/**
	 * 파라미터의 규격 하한 (Lower Specification Limit)을 반환한다.
	 * 
	 * @return 규격 하한
	 */
	public @Nullable String getLSL();
	/**
	 * 파라미터의 규격 하한을 설정한다.
	 * 
	 * @param lsl	설정할 규격 하한
	 */
	public void setLSL(String lsl);

	/**
	 * 파라미터의 규격 상한 (Upper Specification Limit)을 반환한다.
	 * 
	 * @return 규격 상한
	 */
	public @Nullable String getUSL();
	/**
	 * 파라미터의 규격 상한을 설정한다.
	 * 
	 * @param usl	설정할 규격 상한
	 */
	public void setUSL(String usl);
	
	public String getPeriodicDataCollectionIndicator();
	public void setPeriodicDataCollectionIndicator(String indicator);
	
	public String getDataCollectionPeriod();
	public void setDataCollectionPeriod(String period);
}
