package mdt.model.sm.ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

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
	/** Json 직렬화 타입 식별자({@code "mdt:ref:opvar"}). */
	public static final String SERIALIZATION_TYPE = "mdt:ref:opvar";
//	private static final String FIELD_SUBMODEL_REF = "submodelReference";
//	private static final String FIELD_ELEMENT_PATH = "elementPath";
	private static final String FIELD_AAS_OP_VAR = "aasOpVar";
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
		 * 순번(0=INPUT, 1=OUTPUT, 2=INOUTPUT) 또는 {@code "in"}/{@code "out"}/{@code "inout"}(대소문자 무시,
		 * 앞뒤 공백 제거) 표현을 허용하며, {@code "*"}는 종류 미지정을 의미하는 {@code null}로 변환된다.
		 * {@link #toString()}은 이 짧은 이름을 반환한다.
		 *
		 * @param kindStr	변환할 문자열.
		 * @return 대응하는 {@link Kind}. {@code "*"}인 경우 {@code null}.
		 * @throws IllegalArgumentException	인식할 수 없는 문자열이거나 순번이 범위를 벗어난 경우.
		 * @throws NullPointerException	{@code kindStr}이 {@code null}인 경우.
		 */
		public static Kind fromString(String kindStr) {
			try {
				int ordinal = Integer.parseInt(kindStr);
				Preconditions.checkArgument(ordinal >= 0 && ordinal < 3,
								"OperationVariable's ordinal should be between 0, 1, or 2, but %s", kindStr);
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

	private final SubmodelBasedElementReference m_opRef;
	private final Kind m_kind;
	private final int m_ordinal;
	
	private OperationVariableReference(SubmodelBasedElementReference opRef, Kind kind, int ordinal) {
		super(opRef.getSubmodelReference());
		
		Preconditions.checkNotNullArgument(kind, "OperationVariableKind is null");
		Preconditions.checkArgument(ordinal >= 0, "OperationVariable ordinal is negative: %s", ordinal);
		
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
	 * @throws IllegalArgumentException	{@code manager}가 {@code null}인 경우.
	 */
	@Override
	public void activate(MDTInstanceManager manager) {
		// super.activate가 실패하면 m_opRef.activate가 호출되지 않아 m_opRef가 이전 대상으로 활성 상태로 남는다.
		// 그래서 이 참조의 활성 여부는 자신과 m_opRef를 함께 본다(isActivated). 2026-11-01 이전에는 접근 메소드가
		// m_opRef.isActivated()만 확인해, 재활성화가 실패한 뒤에도 이전 Operation에 접근했다.
		super.activate(manager);
		m_opRef.activate(manager);
	}

	/**
	 * 이 참조와 대상 Operation 참조가 모두 활성 상태이면 {@code true}이다.
	 *
	 * @return 활성화 여부.
	 */
	@Override
	public boolean isActivated() {
		return super.isActivated() && m_opRef.isActivated();
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

	/**
	 * 대상 {@link Operation} 요소의 idShort path를 반환한다(Operation 참조에 위임).
	 * <p>
	 * 변수는 idShort path로 직접 가리킬 수 없으므로 변수 자체가 아니라 변수를 포함한 Operation의 경로이다.
	 *
	 * @return Operation 요소의 idShort path.
	 * @throws IllegalStateException	Operation 참조의 경로가 아직 확정되지 않은 경우.
	 */
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
	 * @throws IllegalArgumentException	순번이 해당 종류 변수 목록의 범위를 벗어난 경우.
	 */
	@Override
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(isActivated(), "OperationVariableReference is not activated");

		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: %s", holder);
		Operation op = (Operation)holder;

		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		return opv.getValue();
	}

	/**
	 * 참조가 가리키는 Operation 변수의 값을 주어진 요소로 교체한다.
	 * <p>
	 * 대상 {@link Operation}을 읽어 지정된 종류/순번 변수의 값을 바꾼 뒤 Operation 전체를 다시 기록한다.
	 *
	 * @param newSme	새 변수 값.
	 * @throws IOException 읽기·쓰기 과정에서 예외가 발생한 경우.
	 * @throws IllegalStateException 참조가 활성화되지 않았거나 대상이 {@link Operation}이 아닌 경우.
	 * @throws IllegalArgumentException	순번이 해당 종류 변수 목록의 범위를 벗어난 경우.
	 */
	@Override
	public void write(SubmodelElement newSme) throws IOException {
		Preconditions.checkState(isActivated(), "OperationVariableReference is not activated");
		
		SubmodelElement holder = m_opRef.read();
		Preconditions.checkState(holder instanceof Operation, "target reference is not Operation: %s", holder);
		Operation op = (Operation)holder;

		OperationVariable opv = getVariable(op, m_kind, m_ordinal);
		opv.setValue(newSme);
		m_opRef.write(op);
	}

	@Override
	public ElementValue readValue() throws IOException {
		SubmodelElement sme = read();
		return ElementValues.getValue(sme);
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		SubmodelElement sme = read();
		ElementValues.update(sme, smev);
		write(sme);
	}

	@Override
	public void updateFromValueJson(String valueJsonString) throws IOException {
		ElementValue proto = readValue();
		ElementValue newVal = ElementValues.parseValueJsonString(valueJsonString, proto);
		updateValue(newVal);
	}

	/**
	 * 지원하지 않는다.
	 * <p>
	 * 첨부파일은 idShort path로 대상 요소를 지정하는데, Operation 변수는 idShort path로 가리킬 수 없다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void readAttachment(OutputStream out) throws IOException {
		throw new UnsupportedOperationException("readAttachment is not supported for OperationVariableReference");
	}

	/**
	 * 지원하지 않는다.
	 * <p>
	 * 첨부파일은 idShort path로 대상 요소를 지정하는데, Operation 변수는 idShort path로 가리킬 수 없다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void updateAttachment(FileValue file, InputStream content) throws IOException {
		throw new UnsupportedOperationException("updateAttachment is not supported for OperationVariableReference");
	}

	/**
	 * 지원하지 않는다.
	 * <p>
	 * 첨부파일은 idShort path로 대상 요소를 지정하는데, Operation 변수는 idShort path로 가리킬 수 없다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void removeAttachment() throws IOException {
		throw new UnsupportedOperationException("removeAttachment is not supported for OperationVariableReference");
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
		if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}

		OperationVariableReference other = (OperationVariableReference) obj;
		return Objects.equals(m_opRef, other.m_opRef)
				&& Objects.equals(m_kind, other.m_kind)
				&& m_ordinal == other.m_ordinal;
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_opRef, m_kind, m_ordinal);
	}
	
	/**
	 * 대상 {@link Operation} 요소에 대한 참조와 변수 종류, 순번으로
	 * {@link OperationVariableReference}를 생성한다.
	 *
	 * @param opRef		대상 Operation 요소를 가리키는 참조.
	 * @param kind		변수의 종류(입력/출력/입출력).
	 * @param ordinal	같은 종류 변수 목록 안에서의 순번(0부터 시작).
	 * @return 생성된 {@link OperationVariableReference} 객체.
	 * @throws IllegalArgumentException	{@code opRef}나 {@code kind}가 {@code null}이거나 {@code ordinal}이 음수인 경우.
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
		gen.writeObjectField(FIELD_AAS_OP_VAR, m_opRef);
		gen.writeStringField(FIELD_KIND, m_kind.toString());
		gen.writeNumberField(FIELD_ORDINAL, m_ordinal);
	}

	/**
	 * Json 객체로부터 {@link OperationVariableReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code aasOpVar}({@code @type} 포함 중첩 참조), {@code kind}, {@code ordinal}
	 * 				필드를 담은 Json 노드.
	 * @return 복원된 {@link OperationVariableReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우. 필드가 없거나 타입이 맞지 않거나({@code ordinal}은
	 * 						JSON 정수만), {@code aasOpVar}가 {@link SubmodelBasedElementReference}가 아니면
	 * 						{@link JacksonDeserializationException}.
	 * @throws IllegalArgumentException	{@code kind} 값을 인식할 수 없거나 {@code "*"}이거나, {@code ordinal}이
	 * 									음수인 경우.
	 */
	public static OperationVariableReference deserializeFields(JsonNode jnode) throws IOException {
		JsonNode baseOpVar = JacksonUtils.getField(jnode, FIELD_AAS_OP_VAR);
		ElementReference ref = ElementReferences.parseJsonNode(baseOpVar);
		if ( !(ref instanceof SubmodelBasedElementReference opRef) ) {
			throw new JacksonDeserializationException(String.format("invalid field '%s': submodel-based reference expected, but %s",
																	FIELD_AAS_OP_VAR, ref.getClass().getSimpleName()));
		}
		Kind kind = Kind.fromString(JacksonUtils.getTextField(jnode, FIELD_KIND));
		// ordinal은 JSON 정수로 직렬화된다. 이전에는 문자열("1")도 받았다.
		int ordinal = JacksonUtils.getIntField(jnode, FIELD_ORDINAL);

		return newInstance(opRef, kind, ordinal);
	}
	
	private static OperationVariable getVariable(Operation op, Kind kind, int ordinal) {
		List<OperationVariable> opVarList = switch ( kind ) {
			case INPUT -> op.getInputVariables();
			case OUTPUT -> op.getOutputVariables();
			case INOUTPUT -> op.getInoutputVariables();
		};
		Preconditions.checkArgument(ordinal >= 0 && ordinal < opVarList.size(),
									"OperationVariable's ordinal is out of range: %s (size: %s)", ordinal, opVarList.size());
		return opVarList.get(ordinal);
	}
	
	/**
	 * 상위 클래스가 활성화 시점에 기록할 idShort path를 반환한다. 항상 {@code "Operation"}이다.
	 * <p>
	 * 이 값은 상위 클래스의 활성화 여부 판단에만 쓰인다. 실제 접근 경로는 {@link #getIdShortPathString()}이
	 * 반환하는 Operation 참조의 경로이며, 읽기·쓰기도 모두 Operation 참조를 통해 수행된다.
	 *
	 * @return 고정 문자열 {@code "Operation"}.
	 */
	@Override
	protected String buildIdShortPath() {
		return "Operation";
	}
	
	/**
	 * 원형으로 변수의 현재 값({@link #read()})을 적재한다.
	 *
	 * @return 변수 값에 해당하는 {@link SubmodelElement}.
	 * @throws UncheckedIOException	읽기 과정에서 예외가 발생한 경우.
	 */
	@Override
	protected SubmodelElement loadPrototype() {
		try {
			return read();
		}
		catch ( IOException e ) {
			throw new UncheckedIOException("failed to read OperationVariableReference: " + this, e);
		}
	}
}
