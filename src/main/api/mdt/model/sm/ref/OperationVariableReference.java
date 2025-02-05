package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReference extends AbstractElementReference
											implements MDTElementReference {
	private static final String FIELD_OPERATION = "operationReference";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_ORDINAL = "ordinal";
	
	private final MDTElementReference m_opRef;
	@Nullable private final Kind m_kind;
	@Nullable private final int m_ordinal;
	
	public enum Kind { INPUT, OUTPUT, INOUTPUT };
	
	private OperationVariableReference(MDTElementReference opRef, Kind kind, int ordinal) {
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
	
	public MDTElementReference getOperationReference() {
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
	public String getElementPath() {
		return m_opRef.getElementPath();
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
	public SubmodelElement update(SubmodelElementValue value) throws ResourceNotFoundException, IOException {
		Preconditions.checkArgument(value != null);
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;
		
		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		SubmodelElement opValue = opv.getValue();
		ElementValues.update(opValue, value);
		m_opRef.write(op);
		return opValue;
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
								m_opRef.getElementPath(), m_kind.name().toLowerCase(), m_ordinal);
	}
	
	public static OperationVariableReference newInstance(String instanceId, String submodelIdShort,
															String opIdShortPath, Kind kind, int ordinal) {
		DefaultElementReference smeRef
							= DefaultElementReference.newInstance(instanceId, submodelIdShort, opIdShortPath);
		return new OperationVariableReference(smeRef, kind, ordinal);
	}
	
	public static OperationVariableReference newInstance(MDTElementReference smeRef,
															Kind kind, int ordinal) {
		return new OperationVariableReference(smeRef, kind, ordinal);
	}
	
	public static OperationVariableReference parseString(String refExpr) {
		// refExpr: "<instanceId>/<submodelIdShort>/<opIdShortPath>/<kind>/<ordinal>"
		String[] parts = refExpr.split("/");
		if ( parts.length != 5 ) {
			throw new IllegalArgumentException("invalid OperationVariableReference: " + refExpr);
		}

		Kind kind = parseKind(parts[3]);
		int ordinal = Integer.parseInt(parts[4]);
		return newInstance(parts[0], parts[1], parts[2], kind, ordinal);
	}
	
	public static OperationVariableReference parseJson(ObjectNode topNode) throws IOException {
		MDTElementReference opRef = MDTModelSerDe.readValue(topNode.get(FIELD_OPERATION), MDTElementReference.class);
		String kindStr = topNode.get(FIELD_KIND).asText().toUpperCase();
		Kind kind = parseKind(kindStr);
		int ordinal = Integer.parseInt(topNode.get(FIELD_ORDINAL).asText());

		return newInstance(opRef, kind, ordinal);
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_REFERENCE_TYPE, ElementReferenceType.OPERATION_VARIABLE.getCode());
		gen.writeObjectField(FIELD_OPERATION, m_opRef);
		gen.writeStringField(FIELD_KIND, m_kind.name().toLowerCase());
		gen.writeNumberField(FIELD_ORDINAL, m_ordinal);
		gen.writeEndObject();
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
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		OperationVariableReference ref = OperationVariableReference.newInstance("test", "Simulation",
																				"Operation", Kind.INPUT, 0);
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsInt());
		
		String json = ref.toJsonString();
		System.out.println(json);
		
		MDTElementReference ref2 = (MDTElementReference)ElementReferenceUtils.parseJsonString(json);
		ref2.activate(manager);
		System.out.println(ref2.readValue());
	}
}
