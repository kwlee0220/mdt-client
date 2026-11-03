package mdt.model.sm.info;

import java.util.List;

/**
 * Twin composition의 구성 항목({@link CompositionItem}) 목록 SubmodelElementList 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionItems {
	/**
	 * 구성 항목 리스트를 반환한다.
	 *
	 * @return	구성 항목 리스트.
	 */
	public List<? extends CompositionItem> getElementAll();
	
	/**
	 * 주어진 idShort 경로의 값을 갱신한다.
	 * <p>
	 * 기본 구현은 지원하지 않는다.
	 *
	 * @param idShortPath	갱신 대상의 idShort 경로.
	 * @param value	새 값.
	 * @throws UnsupportedOperationException	항상 (기본 구현).
	 */
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
