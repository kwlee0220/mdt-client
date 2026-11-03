package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import org.jetbrains.annotations.Nullable;

import utils.Keyed;

import mdt.model.MDTSemanticIds;

/**
 * {@code Parameter}는 설비 또는 공정에 속한 파라미터의 정의 (이름, 타입, 단위, 규격 한계 등)를
 * 나타낸다.
 * <p>
 * 파라미터의 실제 값은 같은 파라미터 식별자를 갖는 {@link ParameterValue}로 표현된다.
 * {@link #key()}는 파라미터 식별자를 반환한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Parameter extends Keyed<String> {
	/**
	 * 파라미터 SubmodelElementCollection의 semantic id 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.PARAMETER;
	/**
	 * {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}.
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 파라미터가 속한 엔티티 (설비 또는 공정)의 식별자를 반환한다.
	 *
	 * @return	설비 식별자 또는 공정 식별자.
	 */
	public String getEntityId();
	/**
	 * 파라미터가 속한 엔티티 (설비 또는 공정)의 식별자를 설정한다.
	 *
	 * @param containerId	설정할 설비 식별자 또는 공정 식별자.
	 */
	public void setEntityId(String containerId);
	
	/**
	 * 파라미터의 semantic id를 반환한다.
	 *
	 * @return	{@link #SEMANTIC_ID_REFERENCE}.
	 */
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

	/**
	 * 파라미터 값의 타입 이름({@code ParameterType})을 반환한다.
	 *
	 * @return	파라미터 값의 타입 이름.
	 */
	public String getParameterType();
	/**
	 * 파라미터 값의 타입 이름({@code ParameterType})을 설정한다.
	 *
	 * @param typeName	설정할 파라미터 값의 타입 이름.
	 */
	public void setParameterType(String typeName);

	/**
	 * 파라미터 등급({@code ParameterGrade})을 반환한다.
	 *
	 * @return	파라미터 등급.
	 */
	public String getParameterGrade();
	/**
	 * 파라미터 등급({@code ParameterGrade})을 설정한다.
	 *
	 * @param grade	설정할 파라미터 등급.
	 */
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
	
	/**
	 * 주기적 데이터 수집 여부 표시자({@code PeriodicDataCollectionIndicator})를 반환한다.
	 *
	 * @return	주기적 데이터 수집 여부 표시자.
	 */
	public String getPeriodicDataCollectionIndicator();
	/**
	 * 주기적 데이터 수집 여부 표시자({@code PeriodicDataCollectionIndicator})를 설정한다.
	 *
	 * @param indicator	설정할 주기적 데이터 수집 여부 표시자.
	 */
	public void setPeriodicDataCollectionIndicator(String indicator);
	
	/**
	 * 데이터 수집 주기({@code DataCollectionPeriod})를 반환한다.
	 *
	 * @return	데이터 수집 주기.
	 */
	public String getDataCollectionPeriod();
	/**
	 * 데이터 수집 주기({@code DataCollectionPeriod})를 설정한다.
	 *
	 * @param period	설정할 데이터 수집 주기.
	 */
	public void setDataCollectionPeriod(String period);
}
