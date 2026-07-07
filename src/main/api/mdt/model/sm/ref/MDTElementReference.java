package mdt.model.sm.ref;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.MDTModelSerDe;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.AASFile;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.PropertyValue;


/**
 * MDT 프레임워크에서 관리되는 SubmodelElement를 참조하는 ElementReference의 인터페이스.
 * <p>
 * {@link MDTElementReference}는 다음과 같은 세가지 정보를 가지고 있다.
 * <ul>
 *     <li>instanceId: Element를 포함하고 있는 MDTInstance 객체의 식별자.</li>
 *     <li>Submodel idShort: Element를 포함하고 있는 Submodel의 idShort</li>
 *     <li>Element idShort path: Element의 idShort path</li>
 * </ul>
 * <p>
 * MDTElementReference를 통해 참조된 SubmodelElement를 읽고 쓰는 작업을 수행하기 위해서는
 * {@link #activate(mdt.model.instance.MDTInstanceManager)} 메소드를 통해 활성화되어야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTElementReference extends ElementReference {
	/**
	 * 본 참조가 활성화되어 있는지 여부를 반환한다.
	 * 
	 * @return 활성화 여부.
	 */
	public boolean isActivated();
	
	/**
	 * 본 참조를 활성화시킨다.
	 *
	 * @param manager	객체 활성화에 사용될 {@link MDTInstanceManager} 객체.
	 */
	public void activate(MDTInstanceManager manager);
	
	/**
	 * Element를 포함한 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return		MDTInstance 식별자.
	 */
	public String getInstanceId();
	
	/**
	 * 본 참조에 할당된 MDTInstance를 반환한다.
	 * <p>
	 * 참조가 활성화되지 않은 경우에는 {@link IllegalStateException} 예외가 발생한다.
	 * 
	 * @return	MDTInstance
	 * @throws	IllegalStateException	참조가 활성화되지 않은 경우.
	 */
	public MDTInstance getInstance() throws IllegalStateException;
	
	/**
	 * Element를 포함한 {@link SubmodelService} 객체를 반환한다.
	 * <p>
	 * 참조가 활성화되지 않은 경우에는 {@link IllegalStateException} 예외가 발생한다.
	 * 
	 * @return	SubmodelService
	 * @throws	IllegalStateException	참조가 활성화되지 않은 경우.
	 */
	public SubmodelService getSubmodelService();
	
	/**
	 * 참조된 Element의 idShortPath 문자열을 반환한다.
	 * <p>
	 * 경로가 활성화 시점에 결정되는 구현({@code MDTParameterReference},
	 * {@code MDTArgumentReference} 등)은 활성화 전에 호출하면
	 * {@link IllegalStateException}이 발생한다. {@code DefaultElementReference}처럼
	 * 경로가 생성 시점에 고정되는 구현은 활성화 여부와 무관하게 호출 가능하다.
	 *
	 * @return	idShortPath 문자열.
	 * @throws	IllegalStateException	경로가 확정되기 전(비활성 상태)에 호출된 경우.
	 */
	public String getIdShortPathString();
	
	/**
	 * 참조가 가리키는 'File' {@link SubmodelElement}을 읽어 {@link AASFile}로 반환한다.
	 *
	 * @return	{@link AASFile} 객체. 대상 요소가 존재하지 않는 경우 {@code null}.
	 * @throws	IOException	읽기 과정에서 예외가 발생하거나, 대상이 'File' SubmodelElement가 아닌 경우.
	 */
	public default AASFile readAsFile() throws IOException {
		SubmodelElement sme = read();
		if ( sme == null ) {
			return null;
		}
		else if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File aasFile ) {
			return AASFile.of(aasFile);
		}
		else {
			String json = MDTModelSerDe.toJsonString(sme);
			throw new IOException(String.format("not a File SubmodelElement: prop=%s", json));
		}
	}
	
	/**
	 * 참조가 가리키는 SubmodelElement의 값을 주어진 {@link PropertyValue}로 갱신한다.
	 * <p>
	 * 대상이 MDT 파라미터 값 구조({@code ParameterValue}/{@code EventDateTime} 필드를 가진
	 * SubmodelElementCollection)인 경우에는 {@code ParameterValue} 필드를 주어진 값으로,
	 * {@code EventDateTime} 필드를 현재 시각으로 설정하여 갱신하고, 그 외에는 값 자체를 갱신한다.
	 *
	 * @param propValue	갱신할 Property 값.
	 * @throws IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	public default void updatePropertyValue(PropertyValue<?> propValue) throws IOException {
		SubmodelElement sme = read();
		if ( SubmodelUtils.isParameterValue(sme) ) {
			LinkedHashMap<String,ElementValue> paramValue = new LinkedHashMap<>();
			paramValue.put("ParameterValue", propValue);
			paramValue.put("EventDateTime", PropertyValue.DATE_TIME(Instant.now()));
			updateValue(new ElementCollectionValue(paramValue));
		}
		else {
			updateValue(propValue);
		}
	}
}
