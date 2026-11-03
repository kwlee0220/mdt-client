package mdt.model.sm.data;


/**
 * {@code DependencyType}은 구성 요소들 사이의 의존 관계 유형을 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum DependencyType {
	/**
	 * 한 요소가 다른 요소를 포함하는 관계.
	 */
	CONTAINS,
	/**
	 * 한 요소가 다른 요소에 앞서 수행되는 순서 관계.
	 */
	SEQUENCE,
}
