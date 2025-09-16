package mdt.model.sm.info;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TwinComposition {
	public static final String SEMANTIC_ID = MDTSemanticIds.TWIN_COMPOSITION;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
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
