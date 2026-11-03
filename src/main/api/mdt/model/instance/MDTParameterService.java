package mdt.model.instance;

import java.io.IOException;

import utils.func.Lazy;

import mdt.model.sm.ref.MDTParameterReference;
import mdt.model.sm.value.ElementValue;


/**
 * 특정 MDTInstance에 속한 MDT 파라미터에 접근하기 위한 서비스 객체.
 * <p>
 * 파라미터 값의 읽기/갱신은 {@link MDTParameterReference}를 통해 수행되며, 이 참조 객체는
 * 처음 사용될 때 생성되어 소속 MDTInstance의 {@link MDTInstanceManager}로 활성화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterService {
	private final MDTInstance m_instance;
	private final MDTParameterDescriptor m_descriptor;
	private final Lazy<MDTParameterReference> m_ref = Lazy.of(() -> createReference());
	
	/**
	 * MDT 파라미터 서비스 객체를 생성한다.
	 *
	 * @param instance		파라미터가 속한 MDTInstance.
	 * @param descriptor	파라미터 등록정보.
	 */
	public MDTParameterService(MDTInstance instance, MDTParameterDescriptor descriptor) {
		m_instance = instance;
		m_descriptor = descriptor;
	}
	
	/**
	 * MDT 파라미터의 식별자를 반환한다.
	 *
	 * @return	파라미터 식별자.
	 */
	public String getId() {
		return m_descriptor.getId();
	}
	
	/**
	 * MDT 파라미터의 등록정보를 반환한다.
	 * 
	 * @return	{@link MDTParameterDescriptor} 객체.
	 */
	public MDTParameterDescriptor getDescriptor() {
		return m_descriptor;
	}
	
	/**
	 * MDT 파라미터의 참조 객체를 반환한다.
	 * <p>
	 * 참조 객체는 최초 호출 시 생성/활성화되며, 이후에는 같은 객체가 반환된다.
	 * 
	 * @return	{@link MDTParameterReference} 객체.
	 */
	public MDTParameterReference getReference() {
		return m_ref.get();
	}
	
	/**
	 * MDT 파라미터의 값을 읽는다.
	 * 
	 * @return	파라미터 값.
	 * @throws IOException	파라미터 값 읽기 실패시.
	 */
	public ElementValue read() throws IOException {
		return getReference().readValue();
	}
	
	/**
	 * MDT 파라미터의 값을 갱신한다.
	 * 
	 * @param newValue	갱신할 파라미터 값.
	 * @throws IOException	파라미터 값 갱신 실패시.
	 */
	public void update(ElementValue newValue) throws IOException {
		getReference().updateValue(newValue);
	}
	
	private MDTParameterReference createReference() {
		MDTParameterReference ref = MDTParameterReference.newInstance(m_instance.getId(), getId());
		ref.activate(m_instance.getInstanceManager());
		return ref;
	}
}