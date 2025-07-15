package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReference extends SubmodelBasedElementReference implements MDTElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:opvar";
	private static final String FIELD_OPERATION = "operationReference";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_ORDINAL = "ordinal";
	
	public enum Kind {
		INPUT, OUTPUT, INOUTPUT;
		
		public static Kind fromString(String kindStr) {
			try {
				int ordinal = Integer.parseInt(kindStr);
				Preconditions.checkArgument(ordinal >= 0 && ordinal < 2,
											"OperationVariable's ordinal should be between 0 and 1, but {}", kindStr);
				return Kind.values()[ordinal];
			}
			catch ( NumberFormatException expected ) {
				kindStr = kindStr.trim().toLowerCase();
				return switch ( kindStr ) {
					case "in" -> INPUT;
					case "out" -> OUTPUT;
					case "inout" -> INOUTPUT;
					case "*" -> null;
					default -> throw new IllegalArgumentException("Invalid OperationVariable's kind: " + kindStr);
				};
			}
		}
	};

	private final MDTElementReference m_opRef;
	private final Kind m_kind;
	private final int m_ordinal;
	
	private OperationVariableReference(MDTElementReference opRef, Kind kind, int ordinal) {
		Preconditions.checkArgument(opRef != null, "OperationReference is null");
		Preconditions.checkArgument(kind != null, "OperationVariableKind is null");
		Preconditions.checkArgument(ordinal >= 0, "OperationVariable ordinal is negative: %d", ordinal);
		
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
	
	public int getVariableOrdinal() {
		return m_ordinal;
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
	public SubmodelService getSubmodelService() {
		return m_opRef.getSubmodelService();
	}

	@Override
	public MDTSubmodelReference getSubmodelReference() {
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		if ( m_opRef instanceof SubmodelBasedElementReference subRef ) {
			return subRef.getSubmodelReference();
		}
		else {
			throw new IllegalStateException(
					"OperationVariableReference's operation reference is not SubmodelBasedElementReference: "
							+ m_opRef);
		}
	}

	@Override
	public String getIdShortPathString() {
		return m_opRef.getIdShortPathString();
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
	public void updateValue(ElementValue value) throws ResourceNotFoundException, IOException {
		Preconditions.checkArgument(value != null);
		Preconditions.checkState(m_opRef != null, "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;
		
		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		SubmodelElement opValue = opv.getValue();
		ElementValues.update(opValue, value);
		m_opRef.write(op);
	}

	@Override
	public void updateWithValueJsonString(String valueJsonString) throws IOException {
		SubmodelElement proto = read();
		ElementValue newVal = ElementValues.parseValueJsonString(proto, valueJsonString);
		updateValue(newVal);
	}

	@Override
	public String toStringExpr() {
		return String.format("opvar:%s:%s:%d", m_opRef.toStringExpr(), toKindString(m_kind), m_ordinal);
	}
	
	@Override
	public String toString() {
		return String.format("opvar:%s:%s", m_opRef.toStringExpr(), toKindString(m_kind), m_ordinal);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || !(obj instanceof OperationVariableReference) ) {
			return false;
		}

		OperationVariableReference other = (OperationVariableReference) obj;
		return m_opRef.equals(other.m_opRef)
				&& m_kind.equals(other.m_kind)
				&& m_ordinal == other.m_ordinal;
	}
	
	public static OperationVariableReference newInstance(MDTElementReference opRef, Kind kind, int ordinal) {
		return new OperationVariableReference(opRef, kind, ordinal);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeObjectField(FIELD_OPERATION, m_opRef);
		gen.writeStringField(FIELD_KIND, m_kind.name().toLowerCase());
		gen.writeNumberField(FIELD_ORDINAL, m_ordinal);
	}
	
	public static OperationVariableReference deserializeFields(JsonNode jnode) throws IOException {
		MDTElementReference opRef = MDTModelSerDe.readValue(jnode.get(FIELD_OPERATION), MDTElementReference.class);
		String kindStr = jnode.get(FIELD_KIND).asText().toUpperCase();
		Kind kind = parseKind(kindStr);
		int ordinal = Integer.parseInt(jnode.get(FIELD_ORDINAL).asText());

		return newInstance(opRef, kind, ordinal);
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
	
	private static String toKindString(Kind kind) {
		return switch (kind) {
			case INPUT -> "in";
			case OUTPUT -> "out";
			case INOUTPUT -> "inout";
		};
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
