package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.MDTModelSerDe;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.AASFile;
import mdt.model.sm.DefaultAASFile;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTElementReference extends ElementReference, MDTInstanceManagerAwareReference {
	/**
	 * Element를 포함한 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return		MDTInstance 식별자.
	 */
	public String getInstanceId();
	
	/**
	 * Element를 포함한 Submodel의 idShort를 반환한다.
	 * 
	 * @return		Submodel의 idShort.
	 */
	public String getSubmodelIdShort();
	/**
	 * Element가 위한 path를 반환한다.
	 * 
	 * @return		Element의 idShort path.
	 */
	public String getElementPath();
	
	/**
	 * Element를 포함한 MDTInstance를 반환한다.
	 * <p>
	 * Reference가 {@link #activate(mdt.model.instance.MDTInstanceManager)}에 의해 activate되지 않은
	 * 경우에는 {@link IllegalStateException} 예외가 발생한다.
	 * 
	 * @return		MDTInstance
	 * @throws	IllegalStateException	reference가 activate되지 않은 경우.
	 * @see	#activate(mdt.model.instance.MDTInstanceManager)
	 */
	public MDTInstance getInstance();
	
	/**
	 * Element를 포함한 Submodel 객체를 반환한다.
	 * <p>
	 * Reference가 {@link #activate(mdt.model.instance.MDTInstanceManager)}에 의해 activate되지 않은
	 * 경우에는 {@link IllegalStateException} 예외가 발생한다.
	 * 
	 * @return		Submodel
	 * @throws	IllegalStateException	reference가 activate되지 않은 경우.
	 * @see	#activate(mdt.model.instance.MDTInstanceManager)
	 */
	public SubmodelService getSubmodelService();
	
	public default Operation getAsOperation() throws IOException {
		SubmodelElement sme = read();
		if ( sme instanceof Operation op ) {
			return op;
		}
		else {
			throw new IllegalStateException("Target SubmodelElement is not an Operation: " + sme);
		}
	}
	
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
}
