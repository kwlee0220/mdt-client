package mdt.model.sm.data;

import java.util.List;

/**
 * {@code Equipments}는 {@link Equipment} 객체들의 목록을 표현하는 Data 서브모델 엔티티를 정의한다.
 * <p>
 * AAS 모델에서는 idShort가 {@code Equipments}인 SubmodelElementList에 대응된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Equipments {
	/**
	 * 포함된 모든 {@link Equipment} 객체들의 리스트를 반환한다.
	 *
	 * @return	{@link Equipment} 객체 리스트.
	 */
	public List<? extends Equipment> getElementAll();
}
