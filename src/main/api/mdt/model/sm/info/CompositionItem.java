package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import utils.Keyed;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionItem extends Keyed<String> {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/InformationModel/CompositionItem/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
	public default String key() {
		return getID();
	}
	
	public String getID();
	public void setID(String id);
	
	public String getReference();
	public void setReference(String ref);
	
	public String getDescription();
	public void setDescription(String desc);
}
