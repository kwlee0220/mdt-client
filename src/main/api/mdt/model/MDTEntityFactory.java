package mdt.model;

import mdt.model.sm.entity.SubmodelElementEntity;

/**
 * 식별자로부터 {@link SubmodelElementEntity} 객체를 생성하는 팩토리 인터페이스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTEntityFactory {
	/**
	 * 주어진 식별자에 해당하는 {@link SubmodelElementEntity} 객체를 생성한다.
	 *
	 * @param id	생성할 엔티티의 식별자.
	 * @return	생성된 {@link SubmodelElementEntity} 객체.
	 */
	public SubmodelElementEntity newInstance(String id);
}
