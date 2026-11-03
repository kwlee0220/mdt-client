package mdt.client.resource;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.Delegate;

import utils.Preconditions;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.SubmodelUtils;

/**
 * 주어진 {@link SubmodelService}에 Property 값을 경로로 읽고 쓰는 편의 메소드를 추가한 래퍼.
 * <p>
 * {@link SubmodelService}의 모든 메소드는 Lombok {@code @Delegate}에 의해 감싼 객체로 위임된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ExtendedSubmodelService implements SubmodelService {
	@Delegate private final SubmodelService m_submodelService;
	
	private ExtendedSubmodelService(SubmodelService svc) {
		m_submodelService = svc;
	}
	
	/**
	 * 주어진 {@link SubmodelService}를 감싸는 객체를 생성한다.
	 *
	 * @param svc	감쌀 {@link SubmodelService} 객체.
	 * @return	생성된 {@link ExtendedSubmodelService} 객체.
	 */
	public static ExtendedSubmodelService from(SubmodelService svc) {
		return new ExtendedSubmodelService(svc);
	}
	
	/**
	 * 주어진 경로의 Property 값을 주어진 타입으로 변환하여 반환한다.
	 *
	 * @param <T>	값의 타입.
	 * @param idShortPath	대상 Property의 idShort 경로.
	 * @param valueClass	값의 타입 클래스.
	 * @return	Property 값.
	 * @throws ResourceNotFoundException	주어진 경로의 SubmodelElement가 존재하지 않는 경우.
	 * @throws IllegalArgumentException	주어진 경로의 SubmodelElement가 Property가 아닌 경우.
	 * @see SubmodelUtils#getPropertyValue(Property, Class)
	 */
	public <T> T getPropertyValueByPath(String idShortPath, Class<T> valueClass)
		throws ResourceNotFoundException {
		Property prop = SubmodelUtils.cast(getSubmodelElementByPath(idShortPath), Property.class);
		return SubmodelUtils.getPropertyValue(prop, valueClass);
	}

	/**
	 * 주어진 경로의 Property 값을 갱신한다.
	 * <p>
	 * 대상 SubmodelElement를 먼저 읽어 Property인지 확인한 후
	 * {@link SubmodelService#updateSubmodelElementValueByPath}로 값을 갱신한다.
	 *
	 * @param idShortPath	대상 Property의 idShort 경로.
	 * @param value	새 값의 value-only JSON 문자열.
	 * @throws IllegalArgumentException	{@code idShortPath}가 {@code null}인 경우.
	 * @throws ResourceNotFoundException	주어진 경로의 SubmodelElement가 존재하지 않거나 Property가 아닌 경우.
	 */
	public void setPropertyValueByPath(String idShortPath, String value) {
		Preconditions.checkNotNullArgument(idShortPath, "idShortPath must not be null");
		
		SubmodelElement sme = getSubmodelElementByPath(idShortPath);
		if ( sme instanceof Property ) {
			updateSubmodelElementValueByPath(idShortPath, value);
		}
		else {
			throw new ResourceNotFoundException("Property", "path=" + idShortPath);
		}
	}
}
