package mdt.model.sm.ref;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.base.Preconditions;

import lombok.experimental.Delegate;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.MDTArgumentReference.Kind;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
final class MDTArgumentCollection implements MDTElementReference {
	private final MDTOperationReference m_opRef;
	private final Kind m_kind;
	
	@Delegate private volatile DefaultElementReference m_argListRef;
	private volatile List<SubmodelElement> m_argSmeList;
	
	private MDTArgumentCollection(MDTOperationReference opRef, Kind kind) {
		Preconditions.checkArgument(opRef != null, "Null OperationReference");
		
		m_opRef = opRef;
		m_kind = kind;
	}
	
	public MDTOperationReference getOperationReference() {
		return m_opRef;
	}
	
	public MDTSubmodelReference getSubmodelReference() {
		return m_opRef.getSubmodelReference();
	}
	
	public Kind getKind() {
        return m_kind;
    }

	@Override
	public boolean isActivated() {
		return m_argListRef != null;
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_opRef.activate(manager);
		
		if ( m_kind == null ) {
			String path = m_opRef.getElementPath();
			m_argListRef = DefaultElementReference.newInstance(m_opRef.getSubmodelReference(), path);
			
			return;
		}
		
		String argKindStr = switch ( m_kind ) {
			case INPUT -> "Input";
			case OUTPUT -> "Output";
		};
		String path = String.format("%s.%ss", m_opRef.getElementPath(), argKindStr);
		m_argListRef = DefaultElementReference.newInstance(m_opRef.getSubmodelReference(), path);
		
		SubmodelElementList sml = (SubmodelElementList)m_argListRef.read();
		m_argSmeList = sml.getValue();
	}
	
	@Override
	public String toString() {
		if ( m_kind == null ) {
			return String.format("%s/*", m_opRef.toString());
		}
		
		String kindStr = switch (m_kind) {
			case INPUT -> "in";
			case OUTPUT -> "out";
		};
		return String.format("%s/%s", m_opRef.toString(), kindStr);
	}
	
	static MDTArgumentCollection newInstance(String instanceId, String submodelIdShort, String kindStr) {
		DefaultSubmodelReference smeRef = DefaultSubmodelReference.newInstance(instanceId, submodelIdShort);
		Kind kind = Kind.fromString(kindStr);
		return new MDTArgumentCollection(MDTOperationReference.newInstance(smeRef), kind);
	}
	
	static MDTArgumentCollection newInstance(MDTSubmodelReference smRef, String kindStr) {
		MDTOperationReference opRef = MDTOperationReference.newInstance(smRef);
		Kind kind = Kind.fromString(kindStr);
		return new MDTArgumentCollection(opRef, kind);
	}
	
	int getArgumentIndex(String argName) {
		String key = switch ( m_kind ) {
			case INPUT -> "InputID";
			case OUTPUT -> "OutputID";
		};
		
		return SubmodelUtils.getFieldSMCByIdValue(m_argSmeList, key, argName).index();
	}
}
