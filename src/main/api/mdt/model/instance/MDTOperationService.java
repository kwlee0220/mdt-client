package mdt.model.instance;

import java.io.IOException;
import java.util.List;

import utils.stream.FStream;

import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.MDTArgumentKind;
import mdt.model.sm.ref.MDTArgumentReference;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTOperationService {
	private final MDTInstance m_instance;
	private final MDTOperationDescriptor m_descriptor;
	
	public MDTOperationService(MDTInstance instance, MDTOperationDescriptor descriptor) {
		m_instance = instance;
		m_descriptor = descriptor;
	}
	
	public MDTInstance getInstance() {
		return m_instance;
	}
	
	public MDTOperationDescriptor getDescriptor() {
		return m_descriptor;
	}

	public List<ElementValue> readArgumentValueAll(MDTArgumentKind kind) throws IOException {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(m_instance.getId(), m_descriptor.getId());
		MDTArgumentReference argsRef = MDTArgumentReference.builder()
															.submodelReference(smRef)
															.kind(kind)
															.argument("*")
															.build();
		argsRef.activate(m_instance.getInstanceManager());
		
		String valueField = kind == MDTArgumentKind.INPUT ? "InputValue" : "OutputValue";
		return FStream.from(((ElementListValue) argsRef.readValue()).getElementAll())
				        .cast(ElementCollectionValue.class)
				        .map(smcv -> smcv.getField(valueField))
				        .toList();
    }
}
