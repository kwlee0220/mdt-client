package mdt.model;

import mdt.model.sm.entity.AasCRUDActions;

/**
 * Submodel 수준에서 직접 읽고 쓸 수 있는 최상위 엔티티를 나타내는 표식(marker) 인터페이스이다.
 * <p>
 * 별도의 메소드 없이 {@link AasCRUDActions}의 동작을 그대로 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface TopLevelEntity extends AasCRUDActions {
}
