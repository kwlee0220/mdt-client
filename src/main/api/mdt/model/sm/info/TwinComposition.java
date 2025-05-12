package mdt.model.sm.info;

import java.util.List;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TwinComposition {
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
