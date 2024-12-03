package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultRouting extends SubmodelElementCollectionEntity implements Routing {
	@PropertyField(idShort="RoutingID") private String routingID;
	@PropertyField(idShort="RoutingName") private String routingName;
	@PropertyField(idShort="ItemID") private String itemID;
	@PropertyField(idShort="SetupTime") private String setupTime;
	
	@Override
	public String getIdShort() {
		return "" + this.routingID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getRoutingID());
	}
}
