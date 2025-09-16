package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Output {
	public static final String SEMANTIC_ID = MDTSemanticIds.ARGUMENT_OUTPUT;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public String getOutputID();
	public void setOutputID(String outputID);

	public String getOutputName();
	public void setOutputName(String outputName);

	public SubmodelElement getOutputValue();
	public void setOutputValue(SubmodelElement outputValue);

	public String getOutputType();
	public void setOutputType(String outputType);
}
