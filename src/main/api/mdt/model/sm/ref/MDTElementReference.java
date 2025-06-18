package mdt.model.sm.ref;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.MDTModelSerDe;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.AASFile;
import mdt.model.sm.DefaultAASFile;
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
	 *
	 * @return	idShortPath 문자열.
	 */
	public String getIdShortPathString();
	
	/**
	 * Returns the value of the SubmodelElement referred to by the ElementReference
	 * and cast it to  as {@link AASFile}.
	 * 
	 * @return	AASFile object.
	 * @throws	IOException If an exception occurs during the reading process
	 */
	public default AASFile readAsFile() throws IOException {
		SubmodelElement sme = read();
		if ( sme == null ) {
			return null;
		}
		else if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File aasFile ) {
			DefaultAASFile file = new DefaultAASFile();
			String path = aasFile.getValue();
			
			file.setPath(path);
			file.setContentType(aasFile.getContentType());
			if ( path != null && path.length() > 0 ) {
				file.setContent(getSubmodelService().getFileContentByPath(path));
			}
			
			return file;
		}
		else {
			String json = MDTModelSerDe.toJsonString(sme);
			throw new IOException(String.format("not a File SubmodelElement: prop=%s", json));
		}
	}
	
	public default void updatePropertyValue(PropertyValue propValue) throws IOException {
		SubmodelElement sme = read();
		if ( SubmodelUtils.isParameterValue(sme) ) {
			Map<String,ElementValue> paramValue = Map.of(
				"ParameterValue", propValue,
				"EventDateTime", PropertyValue.DATE_TIME(Instant.now())
			);
			updateValue(new ElementCollectionValue(paramValue));
		}
		else {
			updateValue(propValue);
		}
	}
	
	public default void uploadFile(File file) throws IOException {
		SubmodelElement sme = read();
		if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File ) {
			SubmodelService svc = getSubmodelService();
			DefaultAASFile mdtFile = DefaultAASFile.from(file);
			svc.putFileByPath(getIdShortPathString(), mdtFile);
		}
		else if ( SubmodelUtils.isParameterValue(sme) ) {
			SubmodelService svc = getSubmodelService();
			DefaultAASFile mdtFile = DefaultAASFile.from(file);

			svc.putFileByPath(getIdShortPathString() + ".ParameterValue", mdtFile);
//			Map<String,ElementValue> paramValue = Map.of(
//				"ParameterValue", mdtFile.getValue(),
//				"EventDateTime", PropertyValue.DATE_TIME(Instant.now())
//			);
//			updateValue(new ElementCollectionValue(paramValue));
		}
		else {
			throw new IllegalArgumentException("ElementReference is not a File or Parameter: " + this);
		}
	}
}
