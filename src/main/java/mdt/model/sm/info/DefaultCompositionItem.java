package mdt.model.sm.info;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link CompositionItem}의 기본 구현 엔티티.
 * <p>
 * {@code ID}, {@code Reference}, {@code Description} Property를 필드로 갖는다.
 * 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultCompositionItem extends SubmodelElementCollectionEntity
										implements CompositionItem {
	@PropertyField(idShort="ID") private String ID;
	@PropertyField(idShort="Reference") private String reference;
	@PropertyField(idShort="Description") private String description;
	
	/**
	 * 구성 항목 식별자({@code ID} 필드 값)를 idShort로 반환한다.
	 *
	 * @return	구성 항목 식별자.
	 */
	@Override
	public String getIdShort() {
		return ID;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getID());
	}
}
