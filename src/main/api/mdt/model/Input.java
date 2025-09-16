package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Input {
	public static final String SEMANTIC_ID = MDTSemanticIds.ARGUMENT_INPUT;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public String getInputID();
	public void setInputID(String inputID);

	public String getInputName();
	public void setInputName(String inputName);

	public SubmodelElement getInputValue();
	public void setInputValue(SubmodelElement inputValue);

	public String getInputType();
	public void setInputType(String inputType);
}
