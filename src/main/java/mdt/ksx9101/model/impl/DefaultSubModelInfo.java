package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.SubModelInfo;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSubModelInfo extends AbstractMDTSubmodelElementCollection implements SubModelInfo {
	@PropertyField(idShort="Title") private String title;
	@PropertyField(idShort="Creator") private String creator;
	@PropertyField(idShort="Type") private String type;
	@PropertyField(idShort="Format") private String format;
	@PropertyField(idShort="Identifier") private String identifier;
	
	public DefaultSubModelInfo() {
		super("SubModelInfo", null);
	}
	
	@Override
	public String toString() {
		return String.format("SubModelInfo[%s]", this.title);
	}
}
