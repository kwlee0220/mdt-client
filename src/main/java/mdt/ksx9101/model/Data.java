package mdt.ksx9101.model;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import mdt.model.MDTAASModel;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Data extends MDTAASModel<Submodel> {
	public static final Reference SEMANTIC_ID
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value("https://etri.re.kr/mdt/Submodel/Data/1/1")
									.build())
				.build();
	
	public String getId();
	
	public Reference getSemanticId();
	
	public SubModelInfo getSubModelInfo();
	public DataInfo getDataInfo();
}
