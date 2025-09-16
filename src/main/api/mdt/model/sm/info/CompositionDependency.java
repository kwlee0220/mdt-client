package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionDependency {
	public static final String SEMANTIC_ID = MDTSemanticIds.COMPOSITION_DEPENDENCY;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public String getSourceId();
	public void setSourceId(String src);
	
	public String getTargetId();
	public void setTargetId(String tar);
	
	public String getDependencyType();
	public void setDependencyType(String type);
}
