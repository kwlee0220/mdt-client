package mdt.model.sm.ai;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.AASModelEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AI extends AASModelEntity<Submodel> {
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_AI;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public SubModelInfo getSubModelInfo();
	public AIInfo getAIInfo();
}
