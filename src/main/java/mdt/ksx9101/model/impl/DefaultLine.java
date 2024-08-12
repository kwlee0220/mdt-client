package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Line;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultLine extends AbstractMDTSubmodelElementCollection implements Line {
	@PropertyField(idShort="LineID") private String lineID;
	@PropertyField(idShort="LineName") private String lineName;
	@PropertyField(idShort="LineType") private String lineType;
	@PropertyField(idShort="UseIndicator") private String useIndicator;
	@PropertyField(idShort="LineStatus") private String lineStatus;
	
//	@SMLField(idShort="BOMs", elementClass=DefaultBOM.class)
//	private List<BOM> BOMs = Lists.newArrayList();
//	
//	@SMLField(idShort="ItemMasters", elementClass=DefaultItemMaster.class)
//	private List<ItemMaster> itemMasters = Lists.newArrayList();
//	
//	@SMLField(idShort="Routings", elementClass=DefaultRouting.class)
//	private List<Routing> routings = Lists.newArrayList();
	
	public DefaultLine() {
		super("Line", null);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getLineID());
	}
}
