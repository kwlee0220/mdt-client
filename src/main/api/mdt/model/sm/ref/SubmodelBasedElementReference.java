package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.model.SubmodelService;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class SubmodelBasedElementReference extends AbstractElementReference {
	/**
	 * Returns the SubmodelService that manages the SubmodelElement.
	 * 
	 * @return	SubmodelService object.
	 */
	abstract protected SubmodelService getSubmodelService();
	/**
	 * Returns the idShort path of the SubmodelElement.
	 * 
	 * @return	idShort path of the SubmodelElement.
	 */
	abstract protected String getElementPath();

	@Override
	public SubmodelElement read() throws IOException {
		return getSubmodelService().getSubmodelElementByPath(getElementPath());
	}

	@Override
	public SubmodelElement update(SubmodelElementValue smev) throws IOException {
		SubmodelService service = getSubmodelService();
		service.updateSubmodelElementValueByPath(getElementPath(), smev);
		return service.getSubmodelElementByPath(getElementPath());
	}

	@Override
	public void write(SubmodelElement sme) throws IOException {
		getSubmodelService().setSubmodelElementByPath(getElementPath(), sme);
	}
	
	
	@Override
	public SubmodelElementValue readValue() throws IOException {
		SubmodelElement sme = read();
		if ( sme != null ) {
			return ElementValues.getValue(sme);
		}
		else {
			return null;
		}
	}
}
