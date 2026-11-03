package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link Routing} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
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
