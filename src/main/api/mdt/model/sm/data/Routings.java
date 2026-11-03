package mdt.model.sm.data;

import java.util.List;

import mdt.model.TopLevelEntity;

/**
 * {@code Routings}는 {@link Routing} 객체들의 목록을 표현하는 Data 서브모델의 최상위 엔티티를 정의한다.
 * <p>
 * AAS 모델에서는 idShort가 {@code Routings}인 SubmodelElementList에 대응된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Routings extends TopLevelEntity {
	/**
	 * 포함된 모든 {@link Routing} 객체들의 리스트를 반환한다.
	 *
	 * @return	{@link Routing} 객체 리스트.
	 */
	public List<? extends Routing> getElementAll();

	/**
	 * 주어진 idShort 경로에 해당하는 하위 요소의 값을 갱신한다.
	 * <p>
	 * 기본 구현은 이 기능을 지원하지 않으며 항상 {@link UnsupportedOperationException}을 발생시킨다.
	 *
	 * @param idShortPath	갱신할 하위 요소의 idShort 경로.
	 * @param value	갱신할 값.
	 * @throws UnsupportedOperationException	항상 발생된다.
	 */
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
