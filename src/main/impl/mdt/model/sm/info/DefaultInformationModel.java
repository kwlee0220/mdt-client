package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultInformationModel extends SubmodelEntity implements InformationModel {
	@SMCollectionField(idShort="MDTInfo", adaptorClass=DefaultMDTInfo.class)
	private MDTInfo MDTInfo;
	@SMCollectionField(idShort="TwinComposition", adaptorClass=DefaultTwinComposition.class)
	private TwinComposition twinComposition = new DefaultTwinComposition();
	
	public static DefaultInformationModel from(Submodel submodel) {
		DefaultInformationModel infoModel = new DefaultInformationModel();
		infoModel.updateFromAasModel(submodel);
		
		return infoModel;
	}
	
	public DefaultInformationModel() {
		setIdShort(IDSHORT);
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), this.MDTInfo.getAssetID());
	}
}
