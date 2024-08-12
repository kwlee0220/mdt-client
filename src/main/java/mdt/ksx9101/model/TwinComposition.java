package mdt.ksx9101.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;

import mdt.ksx9101.TopLevelEntity;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TwinComposition extends TopLevelEntity {
	public String getCompositionID();
	public void setCompositionID(String compositionID);

	public String getCompositionType();
	public void setCompositionType(String compositionType);

	public List<ReferenceElement> getCompositionItems();
	public void setCompositionItems(List<ReferenceElement> compositionItems);

	public List<CompositionDependency> getCompositionDependencies();
	public void setCompositionDependencies(List<CompositionDependency> compositionDependencies);
}
