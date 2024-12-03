package mdt.model.sm;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReference extends AbstractSubmodelElementReference
									implements SubmodelElementReference, MDTSubmodelElementReference {
	private final MDTSubmodelElementReference m_opRef;
	@Nullable private final Kind m_kind;
	@Nullable private final int m_ordinal;
	
	public enum Kind { INPUT, OUTPUT, INOUTPUT };
	
	private OperationVariableReference(MDTSubmodelElementReference opRef, Kind kind, int ordinal) {
		Preconditions.checkNotNull(opRef);
		Preconditions.checkNotNull(ordinal);
		
		m_opRef = opRef;
		m_kind = kind;
		m_ordinal = ordinal;
	}

	@Override
	public boolean isActivated() {
		return m_opRef.isActivated();
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_opRef.activate(manager);
	}
	
	public MDTSubmodelElementReference getOperationReference() {
		return m_opRef;
	}
	
	public Kind getVariableKind() {
		return m_kind;
	}

	@Override
	public String getInstanceId() {
		return m_opRef.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() {
		return m_opRef.getInstance();
	}

	@Override
	public String getSubmodelIdShort() {
		return m_opRef.getSubmodelIdShort();
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_opRef.getSubmodelService();
	}

	@Override
	public String getElementIdShortPath() {
		return m_opRef.getElementIdShortPath();
	}

	@Override
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;

		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		return opv.getValue();
	}

	@Override
	public void write(SubmodelElement newSme) throws IOException {
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;

		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		opv.setValue(newSme);
		m_opRef.write(op);
	}

	@Override
	public void update(SubmodelElementValue value) throws ResourceNotFoundException, IOException {
		Preconditions.checkArgument(value != null);
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;
		
		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		ElementValues.update(opv.getValue(), value);
		m_opRef.write(op);
	}

//	@Override
//	public void update(SubmodelElementValue value) throws ResourceNotFoundException, IOException {
//		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
//		
//		SubmodelElement holder = m_opRef.read();
//		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
//		Operation op = (Operation)holder;
//		
//		List<OperationVariable> opVarList = switch ( m_kind ) {
//			case INPUT -> op.getInputVariables();
//			case INOUTPUT -> op.getInoutputVariables();
//			case OUTPUT -> op.getOutputVariables();
//		};
//		if ( m_ordinalExpr.equals("*") ) {
//			Preconditions.checkArgument(value instanceof SubmodelElementListValue);
//			List<SubmodelElementValue> newValues = Lists.newArrayList();
//			newValues.addAll(((SubmodelElementListValue)value).get());
//
//			FStream.from(parseOrdinalExpr(op, m_kind, m_ordinalExpr))
//					.zipWithIndex()
//					.forEach(idxed -> updateVariable(op, m_kind, idxed.value(), newValues.get(idxed.index())));
//		}
//		else {
//			int ordinal = Integer.parseInt(m_ordinalExpr);
//			ElementValues.update(opVarList.get(ordinal).getValue(), value);
//		}
//		m_opRef.write(op);
//	}
	
	@Override
	public String toString() {
		return String.format("opvar:%s/%s/%s/%s/%d",
								m_opRef.getInstanceId(), m_opRef.getSubmodelIdShort(),
								m_opRef.getElementIdShortPath(), m_kind.name().toLowerCase(), m_ordinal);
	}
	
	public static OperationVariableReference newInstance(String instanceId, String submodelIdShort,
															String opIdShortPath, Kind kind, int ordinal) {
		DefaultSubmodelElementReference smeRef
							= DefaultSubmodelElementReference.newInstance(instanceId, submodelIdShort, opIdShortPath);
		return new OperationVariableReference(smeRef, kind, ordinal);
	}
	
	public static OperationVariableReference newInstance(MDTSubmodelElementReference smeRef,
															Kind kind, int ordinal) {
		return new OperationVariableReference(smeRef, kind, ordinal);
	}
	
	public static OperationVariableReference parseString(String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 5 ) {
			throw new IllegalArgumentException("invalid OperationVariableReference: " + refExpr);
		}

		Kind kind = parseKind(parts[3]);
		int ordinal = Integer.parseInt(parts[4]);
		return newInstance(parts[0], parts[1], parts[2], kind, ordinal);
	}
	
	public static OperationVariableReference parseJson(ObjectNode topNode) {
		String mdtId = topNode.get("mdtId").asText();
		String submodelIdShort = topNode.get("submodelIdShort").asText();
		String idShortPath = topNode.get("opIdShortPath").asText();
		String kindStr = topNode.get("kind").asText().toUpperCase();
		Kind kind = parseKind(kindStr);
		int ordinal = Integer.parseInt(topNode.get("ordinal").asText());

		return newInstance(mdtId, submodelIdShort, idShortPath, kind, ordinal);
	}
	
	private static Kind parseKind(String kindStr) {
		try {
			int ordinal = Integer.parseInt(kindStr);
			Preconditions.checkArgument(ordinal >= 0 && ordinal < 3,
										"OperationVariable's ordinal should be between 0 and 2, but {}", kindStr);
			return Kind.values()[ordinal];
		}
		catch ( NumberFormatException expected ) {
			return Kind.valueOf(kindStr);
		}
	}
	
	private static OperationVariable getVariable(Operation op, Kind kind, int ordinal) {
		List<OperationVariable> opVarList = switch ( kind ) {
			case INPUT -> op.getInputVariables();
			case OUTPUT -> op.getOutputVariables();
			case INOUTPUT -> op.getInoutputVariables();
		};
		Preconditions.checkArgument(ordinal >= 0 && ordinal < opVarList.size());
		return opVarList.get(ordinal);
	}
}
