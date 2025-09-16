package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import utils.Keyed;

import mdt.model.MDTSemanticIds;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionItem extends Keyed<String> {
	public static final String SEMANTIC_ID = MDTSemanticIds.COMPOSITION_ITEM;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
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
