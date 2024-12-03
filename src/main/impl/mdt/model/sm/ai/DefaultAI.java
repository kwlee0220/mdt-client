package mdt.model.sm.ai;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultSubModelInfo;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultAI extends SubmodelEntity implements AI {
	@SMCollectionField(idShort="SubModelInfo", adaptorClass=DefaultSubModelInfo.class)
	private SubModelInfo subModelInfo;
	
	@SMCollectionField(idShort="AIInfo")
	private DefaultAIInfo AIInfo;
	
	public static DefaultAI from(Submodel submodel) {
		DefaultAI AI = new DefaultAI();
		AI.updateFromAasModel(submodel);
		
		return AI;
	}

	public DefaultAI() {
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), subModelInfo.getTitle());
	}
}
