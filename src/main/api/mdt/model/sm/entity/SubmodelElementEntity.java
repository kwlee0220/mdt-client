package mdt.model.sm.entity;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelElementEntity extends AASModelEntity<SubmodelElement> {
	public String getIdShort();
	
	public SubmodelElement newSubmodelElement();
}
