package mdt.model.sm;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementListValue;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReference extends AbstractSubmodelElementReference
									implements SubmodelElementReference, MDTInstanceManagerAwareReference {
	private final MDTSubmodelElementReference m_opRef;
	private final Kind m_kind;
	private final String m_ordinalExpr;
	
	public enum Kind { INPUT, OUTPUT, INOUTPUT };
	
	private OperationVariableReference(MDTSubmodelElementReference opRef, Kind kind, String ordinalExpr) {
		Preconditions.checkNotNull(opRef);
		Preconditions.checkNotNull(ordinalExpr);
		
		m_opRef = opRef;
		m_kind = kind;
		m_ordinalExpr = ordinalExpr;
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
	
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement sme = m_opRef.read();
		Preconditions.checkState(sme instanceof Operation, "target reference is not Operation: {}", sme);
		Operation op = (Operation)sme;
		
		List<SubmodelElement> elms = FStream.from(parseOrdinalExpr(op, m_kind, m_ordinalExpr))
											.map(ord -> getVariable(op, m_kind, ord).getValue())
											.toList();
		if ( elms.size() == 1 ) {
			return elms.get(0);
		}
		else {
			return new DefaultSubmodelElementList.Builder()
						.idShort(op.getIdShort())
						.value(elms)
						.build();
		}
	}

	@Override
	public void write(SubmodelElement newSme) throws IOException {
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;
		
		List<Integer> ordinals = parseOrdinalExpr(op, m_kind, m_ordinalExpr);
		List<SubmodelElement> newSmeList = Lists.newArrayList();
		if ( ordinals.size() > 1 ) {
			Preconditions.checkArgument(newSme instanceof SubmodelElementList);
			newSmeList.addAll(((SubmodelElementList)newSme).getValue());
		}
		else {
			newSmeList.add(newSme);
		}
		
		FStream.from(parseOrdinalExpr(op, m_kind, m_ordinalExpr))
				.zipWithIndex()
				.forEach(idxed -> writeVariable(op, m_kind, idxed.value(), newSmeList.get(idxed.index())));
		m_opRef.write(op);
	}

	@Override
	public void update(SubmodelElementValue value) throws ResourceNotFoundException, IOException {
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;

		List<SubmodelElementValue> newValues = Lists.newArrayList();
		Preconditions.checkArgument(value instanceof SubmodelElementListValue);
		newValues.addAll(((SubmodelElementListValue)value).get());
		
		FStream.from(parseOrdinalExpr(op, m_kind, m_ordinalExpr))
				.zipWithIndex()
				.forEach(idxed -> updateVariable(op, m_kind, idxed.value(), newValues.get(idxed.index())));
		m_opRef.write(op);
	}
	
	@Override
	public String toString() {
		return String.format("opvar:%s/%s/%s/%s/%s",
								m_opRef.getInstanceId(), m_opRef.getSubmodelIdShort(),
								m_opRef.getElementIdShortPath(), m_kind.name().toLowerCase(), m_ordinalExpr);
	}
	
	public static OperationVariableReference newInstance(String instanceId, String submodelIdShort,
															String opIdShortPath, Kind kind, String ordinalExpr) {
		DefaultSubmodelElementReference smeRef
							= DefaultSubmodelElementReference.newInstance(instanceId, submodelIdShort, opIdShortPath);
		return new OperationVariableReference(smeRef, kind, ordinalExpr);
	}
	
	public static OperationVariableReference newInstance(MDTSubmodelElementReference smeRef,
															Kind kind, String ordinalExpr) {
		return new OperationVariableReference(smeRef, kind, ordinalExpr);
	}
	
	public static OperationVariableReference parseString(String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 5 ) {
			throw new IllegalArgumentException("invalid OperationVariableReference: " + refExpr);
		}

		Kind kind = parseKind(parts[3]);
		return newInstance(parts[0], parts[1], parts[2], kind, parts[4]);
	}
	
	public static OperationVariableReference parseJson(ObjectNode topNode) {
		String mdtId = topNode.get("mdtId").asText();
		String submodelIdShort = topNode.get("submodelIdShort").asText();
		String idShortPath = topNode.get("opIdShortPath").asText();
		String kindStr = topNode.get("kind").asText().toUpperCase();
		Kind kind = parseKind(kindStr);
		String ordinalExpr = topNode.get("ordinal").asText();

		return newInstance(mdtId, submodelIdShort, idShortPath, kind, ordinalExpr);
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
	
	private OperationVariable getVariable(Operation op, Kind kind, int ordinal) {
		return switch ( kind ) {
			case INPUT -> op.getInputVariables().get(ordinal);
			case OUTPUT -> op.getOutputVariables().get(ordinal);
			case INOUTPUT -> op.getInoutputVariables().get(ordinal);
		};
	}
	
	private void writeVariable(Operation op, Kind kind, int ordinal, SubmodelElement elm) {
		OperationVariable opv = getVariable(op, kind, ordinal);
		opv.setValue(elm);
	}
	
	private void updateVariable(Operation op, Kind kind, int ordinal, SubmodelElementValue value) {
		OperationVariable opv = getVariable(op, kind, ordinal);
		ElementValues.update(opv.getValue(), value);
	}
	
	private List<Integer> parseOrdinalExpr(Operation op, Kind kind, String expr) {
		int varCount = switch ( kind ) {
			case INPUT -> op.getInputVariables().size();
			case OUTPUT -> op.getOutputVariables().size();
			case INOUTPUT -> op.getInoutputVariables().size();
		};
		
		if ( expr.equals("*") ) {
			return FStream.range(0, varCount).toList();
		}
		else {
			return FStream.of(expr.split(","))
							.map(Integer::parseInt)
							.peek(ord -> Preconditions.checkArgument(ord >= 0 && ord < varCount))
							.toList();
		}
	}
}
