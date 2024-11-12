package mdt.model;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSubModelInfo extends SubmodelElementCollectionEntity implements SubModelInfo {
	@PropertyField(idShort="Title") private String title;
	@PropertyField(idShort="Creator") private String creator;
	@PropertyField(idShort="Type") private String type;
	@PropertyField(idShort="Format") private String format;
	@PropertyField(idShort="Identifier") private String identifier;
	
	public DefaultSubModelInfo() {
		setIdShort("SubModelInfo");
	}
	
	@Override
	public String toString() {
		return String.format("SubModelInfo[%s]", this.title);
	}
}
