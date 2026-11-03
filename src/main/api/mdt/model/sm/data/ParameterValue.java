package mdt.model.sm.data;

import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.Keyed;

import mdt.model.MDTSemanticIds;
import mdt.model.sm.entity.AasCRUDActions;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;

/**
 * {@code ParameterValue}는 설비 또는 공정에 속한 파라미터의 값을 나타낸다.
 * <p>
 * 파라미터 값은 {@link SubmodelElement}로 표현되며, 같은 파라미터 식별자를 갖는 {@link Parameter}에 대응된다.
 * {@link #key()}는 파라미터 식별자를 반환한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ParameterValue extends AasCRUDActions, Keyed<String> {
	/**
	 * 파라미터 값 SubmodelElementCollection의 semantic id 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.PARAMETER_VALUE;
	/**
	 * {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}.
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 파라미터 값이 속한 엔티티 (설비 또는 공정)의 식별자를 반환한다.
	 *
	 * @return	설비 식별자 또는 공정 식별자.
	 */
	public String getEntityId();
	/**
	 * 파라미터 값이 속한 엔티티 (설비 또는 공정)의 식별자를 설정한다.
	 *
	 * @param containerId	설정할 설비 식별자 또는 공정 식별자.
	 */
	public void setEntityId(String containerId);
	
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
	 * 파라미터 값 ({@code ParameterValue})을 반환한다.
	 *
	 * @return	파라미터 값을 담은 {@link SubmodelElement}.
	 */
	public SubmodelElement getParameterValue();
	/**
	 * 파라미터 값 ({@code ParameterValue})을 설정한다.
	 *
	 * @param value	설정할 파라미터 값 {@link SubmodelElement}.
	 */
	public void setParameterValue(SubmodelElement value);
	/**
	 * 현재 파라미터 값 {@link SubmodelElement}의 내용을 주어진 {@link ElementValue}로 갱신한다.
	 * <p>
	 * {@link #getParameterValue()}로 얻은 {@link SubmodelElement}를 {@link ElementValues#update}로 갱신한 뒤
	 * {@link #setParameterValue(SubmodelElement)}로 다시 설정한다.
	 *
	 * @param value	갱신할 값.
	 * @throws IllegalArgumentException	현재 파라미터 값이나 {@code value}가 {@code null}이거나,
	 * 			두 값의 종류·데이터 타입이 맞지 않는 경우.
	 */
	public default void setParameterValue(ElementValue value) {
		SubmodelElement prev = getParameterValue();
		ElementValues.update(prev, value);
		setParameterValue(prev);
	}

	/**
	 * 파라미터 값이 기록된 시각({@code EventDateTime})을 반환한다.
	 *
	 * @return	파라미터 값이 기록된 시각.
	 */
	public Instant getEventDateTime();
	/**
	 * 파라미터 값이 기록된 시각({@code EventDateTime})을 설정한다.
	 *
	 * @param dateTime	설정할 파라미터 값이 기록된 시각.
	 */
	public void setEventDateTime(Instant dateTime);

	/**
	 * 파라미터 값의 검증 결과 코드({@code ValidationResultCode})를 반환한다.
	 *
	 * @return	파라미터 값의 검증 결과 코드.
	 */
	public String getValidationResultCode();
	/**
	 * 파라미터 값의 검증 결과 코드({@code ValidationResultCode})를 설정한다.
	 *
	 * @param code	설정할 파라미터 값의 검증 결과 코드.
	 */
	public void setValidationResultCode(String code);
}
