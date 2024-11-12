package mdt.model.sm.data;

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
public class DefaultData extends SubmodelEntity implements Data {
	private static final String IDSHORT = "Data";
	
	@SMCollectionField(idShort="SubModelInfo", adaptorClass=DefaultSubModelInfo.class)
	private SubModelInfo subModelInfo;

	@SMCollectionField(idShort="DataInfo", adaptorClass=DefaultDataInfo.class)
	private DataInfo dataInfo;
	
	public static DefaultData from(Submodel submodel) {
		DefaultData data = new DefaultData();
		data.updateFromAasModel(submodel);
		
		return data;
	}
	
	public DefaultData() {
		setIdShort(IDSHORT);
		setSemanticId(Data.SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format(IDSHORT);
	}
}
