package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.InformationModel;
import mdt.ksx9101.model.MDTInfo;
import mdt.ksx9101.model.TwinComposition;
import mdt.model.AbstractMDTSubmodel;
import mdt.model.SMCField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultInformationModel extends AbstractMDTSubmodel implements InformationModel {
	@SMCField(idShort="MDTInfo", adaptorClass=DefaultMDTInfo.class)
	private MDTInfo MDTInfo;
	@SMCField(idShort="TwinComposition", adaptorClass=DefaultTwinComposition.class)
	private TwinComposition twinComposition = new DefaultTwinComposition();
	
	public DefaultInformationModel() {
		setIdShort("InformationModel");
		setSemanticId(SEMANTIC_ID);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), this.MDTInfo.getAssetID());
	}
}
