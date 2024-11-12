package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InformationModel {
	public static final String IDSHORT = "Data";
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/InformationModel/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
	public MDTInfo getMDTInfo();
	public void setMDTInfo(MDTInfo info);
	
	public TwinComposition getTwinComposition();
	public void setTwinComposition(TwinComposition composition);
}