package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Output {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/Operation/Output/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
	public String getOutputID();
	public void setOutputID(String outputID);

	public String getOutputName();
	public void setOutputName(String outputName);

	public SubmodelElement getOutputValue();
	public void setOutputValue(SubmodelElement outputValue);

	public String getOutputType();
	public void setOutputType(String outputType);
}
