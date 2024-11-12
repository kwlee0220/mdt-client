package mdt.model.sm.info;

import java.util.List;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TwinComposition {
	public String getCompositionID();
	public void setCompositionID(String compositionID);

	public String getCompositionType();
	public void setCompositionType(String compositionType);

	public List<ComponentItem> getComponentItems();
	public void setComponentItems(List<ComponentItem> componentItems);

	public List<CompositionDependency> getCompositionDependencies();
	public void setCompositionDependencies(List<CompositionDependency> compositionDependencies);
}
