package mdt.ksx9101.model.impl;

import lombok.Getter;
import lombok.Setter;
import mdt.ksx9101.model.Routing;
import mdt.model.AbstractMDTSubmodelElementCollection;
import mdt.model.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultRouting extends AbstractMDTSubmodelElementCollection implements Routing {
	@PropertyField(idShort="RoutingID") private String routingID;
	@PropertyField(idShort="RoutingName") private String routingName;
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="SetupTime") private String setupTime;
	
	public DefaultRouting() {
		super(null, "routingID");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getRoutingID());
	}
}
