package mdt.model.sm.ref;

import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;


/**
 * Submodel에 대한 참조(reference)를 추상화한 인터페이스이다.
 * <p>
 * 참조가 가리키는 Submodel에 접근하기 위한 {@link SubmodelService}를 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelReference {
	/**
	 * 참조가 가리키는 Submodel에 접근하기 위한 {@link SubmodelService}를 반환한다.
	 *
	 * @return	{@link SubmodelService} 객체.
	 * @throws ResourceNotFoundException	참조가 가리키는 Submodel을 찾을 수 없는 경우.
	 */
	public SubmodelService get() throws ResourceNotFoundException;
}
