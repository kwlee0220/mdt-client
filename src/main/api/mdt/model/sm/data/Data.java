package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;
import mdt.model.SubModelInfo;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Data {
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_DATA;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public SubModelInfo getSubModelInfo();
	public DataInfo getDataInfo();
}
