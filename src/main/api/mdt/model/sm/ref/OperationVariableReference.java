package mdt.model.sm.ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;

import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;


/**
 * {@link Operation}의 특정 입력/출력/입출력 변수(operation variable)를 가리키는
 * {@link ElementReference} 구현체이다.
 * <p>
 * 대상 {@link Operation}에 대한 참조와 변수의 종류({@link Kind}), 그리고 같은 종류 변수 목록
 * 안에서의 순번(ordinal)으로 대상 변수의 값을 지정한다. 값을 읽거나 쓰면 해당 Operation을 읽어
 * 지정된 변수의 값을 꺼내거나 갱신한 뒤 다시 기록한다.
 * <p>
 * Json 직렬화 시 {@link #SERIALIZATION_TYPE}({@code "mdt:ref:opvar"}) 타입으로 식별된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReference extends SubmodelBasedElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:opvar";
//	private static final String FIELD_SUBMODEL_REF = "submodelReference";
//	private static final String FIELD_ELEMENT_PATH = "elementPath";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_ORDINAL = "ordinal";

	/**
	 * Operation 변수의 종류를 나타내는 열거형이다.
	 */
	public enum Kind {
		/** 입력 변수. */
		INPUT,
		/** 출력 변수. */
		OUTPUT,
		/** 입출력 변수. */
		INOUTPUT;

		/**
		 * 문자열을 {@link Kind}로 변환한다.
		 * <p>
		 * 순번(숫자) 또는 {@code "in"}/{@code "out"}/{@code "inout"} 표현을 허용하며,
		 * {@code "*"}는 종류 미지정을 의미하는 {@code null}로 변환된다.
		 *
		 * @param kindStr	변환할 문자열.
		 * @return 대응하는 {@link Kind}. {@code "*"}인 경우 {@code null}.
		 * @throws IllegalArgumentException	인식할 수 없는 문자열인 경우.
		 */
		public static Kind fromString(String kindStr) {
			try {
				int ordinal = Integer.parseInt(kindStr);
				Preconditions.checkArgument(ordinal >= 0 && ordinal < 3,
											"OperationVariable's ordinal should be between 0, 1, or 2, but {}", kindStr);
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
		
		@Override
		public String toString() {
			return switch ( this ) {
				case INPUT -> "in";
				case OUTPUT -> "out";
				case INOUTPUT -> "inout";
			};
		}
	};

	private final MDTElementReference m_opRef;
	private final Kind m_kind;
	private final int m_ordinal;
	
	private OperationVariableReference(SubmodelBasedElementReference opRef, Kind kind, int ordinal) {
		super(opRef.getSubmodelReference());
		
		Preconditions.checkNotNullArgument(kind, "OperationVariableKind is null");
		Preconditions.checkArgument(ordinal >= 0, "OperationVariable ordinal is negative: %d", ordinal);
		
		m_opRef = opRef;
		m_kind = kind;
		m_ordinal = ordinal;
	}

	/**
	 * 이 참조를 활성화한다.
	 * <p>
	 * 상위 클래스의 활성화에 더해, 대상 {@link Operation}을 가리키는 내부 참조도 함께 활성화한다.
	 *
	 * @param manager	활성화에 사용할 {@link MDTInstanceManager}.
	 */
	@Override
	public void activate(MDTInstanceManager manager) {
		super.activate(manager);
		m_opRef.activate(manager);
	}
	
	/**
	 * 이 변수가 속한 {@link Operation}에 대한 참조를 반환한다.
	 *
	 * @return Operation을 가리키는 {@link MDTElementReference}.
	 */
	public MDTElementReference getOperationReference() {
		return m_opRef;
	}

	/**
	 * 변수의 종류(입력/출력/입출력)를 반환한다.
	 *
	 * @return 변수의 종류를 나타내는 {@link Kind}.
	 */
	public Kind getVariableKind() {
		return m_kind;
	}

	/**
	 * 같은 종류 변수 목록 안에서의 변수 순번(ordinal)을 반환한다.
	 *
	 * @return 변수 순번(0부터 시작).
	 */
	public int getVariableOrdinal() {
		return m_ordinal;
	}

	@Override
	public String getIdShortPathString() {
		return m_opRef.getIdShortPathString();
	}

	/**
	 * 참조가 가리키는 Operation 변수의 값을 읽어 반환한다.
	 * <p>
	 * 대상 {@link Operation}을 읽어 지정된 종류/순번의 변수 값을 반환한다.
	 *
	 * @return 변수 값에 해당하는 {@link SubmodelElement}.
	 * @throws IOException 읽기 과정에서 예외가 발생한 경우.
	 * @throws IllegalStateException 참조가 활성화되지 않았거나 대상이 {@link Operation}이 아닌 경우.
	 */
	@Override
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(m_opRef.isActivated(), "OperationVariableReference is not activated");

		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;

		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		return opv.getValue();
	}

	@Override
	public void write(SubmodelElement newSme) throws IOException {
		Preconditions.checkState(m_opRef.isActivated(), "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;

		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		opv.setValue(newSme);
		m_opRef.write(op);
	}

	@Override
	public void readAttachment(OutputStream out) throws IOException {
		Preconditions.checkState(m_opRef.isActivated(), "OperationVariableReference is not activated");
		
		m_opRef.readAttachment(out);
	}

	@Override
	public void updateAttachment(FileValue file, InputStream content) throws IOException {
		Preconditions.checkState(m_opRef.isActivated(), "OperationVariableReference is not activated");
		
		m_opRef.updateAttachment(file, content);
	}

	@Override
	public void removeAttachment() throws IOException {
		Preconditions.checkState(m_opRef.isActivated(), "OperationVariableReference is not activated");
		
		m_opRef.removeAttachment();
	}

	@Override
	public void updateValue(ElementValue value) throws ResourceNotFoundException, IOException {
		Preconditions.checkNotNullArgument(value, "value is null");
		Preconditions.checkState(m_opRef.isActivated(), "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: {}", holder);
		Operation op = (Operation)holder;
		
		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		SubmodelElement opValue = opv.getValue();
		ElementValues.update(opValue, value);
		m_opRef.write(op);
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		ElementValue proto = readValue();
		ElementValue newVal = ElementValues.parseValueJsonString(valueJsonString, proto);
		updateValue(newVal);
	}

	@Override
	public String toStringExpr() {
		return String.format("opvar:%s:%s:%d", m_opRef.toStringExpr(), m_kind, m_ordinal);
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
		return getSubmodelReference().equals(other.getSubmodelReference())
				&& m_kind.equals(other.m_kind)
				&& m_ordinal == other.m_ordinal;
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_opRef, m_kind, m_ordinal);
	}
	
	/**
	 * Operation을 포함한 Submodel 참조와 변수 종류, 순번으로 {@link OperationVariableReference}를 생성한다.
	 *
	 * @param submodelRef	대상 Operation({@code "Operation"} idShort)을 포함한 Submodel에 대한 참조.
	 * @param kind			변수의 종류(입력/출력/입출력).
	 * @param ordinal		같은 종류 변수 목록 안에서의 순번(0부터 시작).
	 * @return 생성된 {@link OperationVariableReference} 객체.
	 */
	public static OperationVariableReference newInstance(SubmodelBasedElementReference opRef, Kind kind, int ordinal) {
		Preconditions.checkNotNullArgument(opRef, "operation reference is null");
		return new OperationVariableReference(opRef, kind, ordinal);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	/**
	 * 이 참조의 필드들을 주어진 {@link JsonGenerator}로 직렬화한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Serializer}가 {@link ElementReference} 객체를
	 * 직렬화하는 과정에서 호출된다.
	 *
	 * @param gen	직렬화에 사용할 {@link JsonGenerator}.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		m_opRef.serializeFields(gen);
		gen.writeStringField(FIELD_KIND, m_kind.toString());
		gen.writeNumberField(FIELD_ORDINAL, m_ordinal);
	}

	/**
	 * Json 객체로부터 {@link OperationVariableReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code operationReference}, {@code kind}, {@code ordinal} 필드를 담은 Json 노드.
	 * @return 복원된 {@link OperationVariableReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static OperationVariableReference deserializeFields(JsonNode jnode) throws IOException {
		DefaultElementReference opRef = DefaultElementReference.deserializeFields(jnode);
		String kindStr = checkJsonField(jnode, FIELD_KIND).asText();
		Kind kind = Kind.fromString(kindStr);

		try {
			String ordinalStr = checkJsonField(jnode, FIELD_ORDINAL).asText();
			int ordinal = Integer.parseInt(ordinalStr);

			return newInstance(opRef, kind, ordinal);
		}
		catch ( NumberFormatException e ) {
			throw new IOException("Invalid ordinal value for OperationVariableReference: " + e.getMessage(), e);
		}
	}
	
	private static OperationVariable getVariable(Operation op, Kind kind, int ordinal) {
		List<OperationVariable> opVarList = switch ( kind ) {
			case INPUT -> op.getInputVariables();
			case OUTPUT -> op.getOutputVariables();
			case INOUTPUT -> op.getInoutputVariables();
		};
		Preconditions.checkArgument(ordinal >= 0 && ordinal < opVarList.size(), 
									"OperationVariable's ordinal is out of range: %d (size: %d)", ordinal, opVarList.size());
		return opVarList.get(ordinal);
	}
	
	/**
	 * 대상 {@link Operation}의 idShort path를 반환한다.
	 * <p>
	 * Operation은 Submodel 내 {@code "Operation"} idShort에 위치하는 것으로 고정되어 있으므로 항상
	 * {@code "Operation"}을 반환한다.
	 *
	 * @return Operation의 idShort path({@code "Operation"}).
	 */
	@Override
	protected String buildIdShortPath() {
		return "Operation";
	}
}
