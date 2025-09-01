package mdt.model.instance;

import java.io.IOException;

import utils.func.Lazy;

import mdt.model.sm.ref.MDTParameterReference;
import mdt.model.sm.value.ElementValue;


/**
 * MDT 파라미터의 인터페이스를 기술한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterService {
	private final MDTInstance m_instance;
	private final MDTParameterDescriptor m_descriptor;
	private final Lazy<MDTParameterReference> m_ref = Lazy.of(() -> createReference());
	
	public MDTParameterService(MDTInstance instance, MDTParameterDescriptor descriptor) {
		m_instance = instance;
		m_descriptor = descriptor;
	}
	
	/**
	 * MDT 파라미터의 등록정보를 반환한다.
	 * 
	 * @return {@link MDTParameterDescriptor} 객체.
	 */
	public MDTParameterDescriptor getDescriptor() {
		return m_descriptor;
	}
	
	/**
	 * MDT 파라미터의 참조 객체를 반환한다.
	 * 
	 * @return {@link MDTParameterReference} 객체.
	 */
	public MDTParameterReference getReference() {
		return m_ref.get();
	}
	
	/**
	 * MDT 파라미터의 값을 읽는다.
	 * 
	 * @throws IOException	파라미터 값 읽기 실패시. 
	 */
	public ElementValue read() throws IOException {
		return getReference().readValue();
	}
	
	private MDTParameterReference createReference() {
		MDTParameterReference ref = MDTParameterReference.newInstance(m_instance.getId(), m_descriptor.getId());
		ref.activate(m_instance.getInstanceManager());
		return ref;
	}
}