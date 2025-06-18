package mdt.model.sm.info;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TwinComposition {
	public static final String SEMANTIC_ID = "https://etri.re.kr/mdt/Submodel/InformationModel/TwinComposition/1/1";
	public static final Reference SEMANTIC_ID_REFERENCE
		= new DefaultReference.Builder()
				.type(ReferenceTypes.EXTERNAL_REFERENCE)
				.keys(new DefaultKey.Builder()
									.type(KeyTypes.GLOBAL_REFERENCE)
									.value(SEMANTIC_ID)
									.build())
				.build();
	
	public String getCompositionID();
	public void setCompositionID(String compositionID);
	
	public default String getKey() {
		return getCompositionID();
	}

	public String getCompositionType();
	public void setCompositionType(String compositionType);

	public List<CompositionItem> getCompositionItems();
	public void setCompositionItems(List<CompositionItem> compositionItems);

	public List<CompositionDependency> getCompositionDependencies();
	public void setCompositionDependencies(List<CompositionDependency> compositionDependencies);
}
