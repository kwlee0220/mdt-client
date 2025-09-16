package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InformationModel {
	public static final String IDSHORT = "InformationModel";
	public static final String SEMANTIC_ID = MDTSemanticIds.SUBMODEL_INFO;
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	public MDTInfo getMDTInfo();
	public void setMDTInfo(MDTInfo info);
	
	public TwinComposition getTwinComposition();
	public void setTwinComposition(TwinComposition composition);
}
