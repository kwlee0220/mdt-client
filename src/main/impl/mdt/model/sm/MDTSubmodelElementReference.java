package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTSubmodelElementReference extends SubmodelElementReference, MDTInstanceManagerAwareReference {
	public String getInstanceId();
	
	public String getSubmodelIdShort();
	public String getElementIdShortPath();
	
	public MDTInstance getInstance();
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
}
