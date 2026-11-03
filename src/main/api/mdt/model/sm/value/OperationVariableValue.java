package mdt.model.sm.value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.Preconditions;
import utils.json.JacksonUtils;
import utils.stream.FStream;

/**
 * AAS {@link Operation}의 input/output/inoutput 변수 값 목록을 표현하는 클래스.
 * <p>
 * 값 객체({@link #toValueObject()})와 value-only JSON은 {@code inputs}/{@code outputs}/{@code inoutputs}
 * 필드에 각 변수 값 목록을 가진 객체이다. polymorphic JSON의 타입 식별자는 {@value #SERIALIZATION_TYPE}이며,
 * 이 때 각 필드는 {@link ElementListValue}의 polymorphic JSON으로 직렬화된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class OperationVariableValue extends AbstractElementValue {
    private final List<ElementValue> m_inputValues;
    private final List<ElementValue> m_outputValues;
    private final List<ElementValue> m_inoutputValues;

	/**
	 * 주어진 input/output/inoutput 변수 값 목록으로 {@code OperationVariableValue} 객체를 생성한다.
	 * <p>
	 * 목록은 복사되지 않고 그대로 보관된다.
	 *
	 * @param inValues		input 변수 값 목록.
	 * @param outValues		output 변수 값 목록.
	 * @param inoutValues	inoutput 변수 값 목록.
	 * @throws IllegalArgumentException	인자 중 {@code null}이 있는 경우.
	 */
    public OperationVariableValue(List<ElementValue> inValues, List<ElementValue> outValues,
    								List<ElementValue> inoutValues) {
		Preconditions.checkNotNullArgument(inValues, "inValues is null");
		Preconditions.checkNotNullArgument(outValues, "outValues is null");
		Preconditions.checkNotNullArgument(inoutValues, "inoutValues is null");

    	m_inputValues = inValues;
    	m_outputValues = outValues;
    	m_inoutputValues = inoutValues;
    }

	/**
	 * input 변수 값 목록을 반환한다.
	 *
	 * @return	input 변수 값 목록.
	 */
	public List<ElementValue> getInputValues() {
		return m_inputValues;
	}

	/**
	 * output 변수 값 목록을 반환한다.
	 *
	 * @return	output 변수 값 목록.
	 */
	public List<ElementValue> getOutputValues() {
		return m_outputValues;
	}

	/**
	 * inoutput 변수 값 목록을 반환한다.
	 *
	 * @return	inoutput 변수 값 목록.
	 */
	public List<ElementValue> getInoutputValues() {
		return m_inoutputValues;
	}

	@Override
	public Map<String, Object> toValueObject() {
		Map<String, Object> value = Maps.newLinkedHashMap();
		value.put(FIELD_INPUT_VARIABLES, new ElementListValue(m_inputValues).toValueObject());
		value.put(FIELD_OUTPUT_VARIABLES, new ElementListValue(m_outputValues).toValueObject());
		value.put(FIELD_INOUTPUT_VARIABLES, new ElementListValue(m_inoutputValues).toValueObject());
		return value;
	}

	/**
	 * 이 값의 각 변수 값을 주어진 {@link Operation}의 대응 변수(input/output/inoutput)의
	 * value 원소에 순서대로 반영한다.
	 *
	 * <p>
	 * 세 종류 변수의 개수와 {@code null} 값은 반영 전에 모두 확인하므로 이때는 아무 변수도 바뀌지 않는다
	 * (2026-11-01 이전에는 종류별로 확인해, output 개수가 틀려도 input은 이미 바뀌었다).
	 * 그러나 변수 하나의 반영이 실패하면(타입 불일치 등) 앞선 변수들은 이미 바뀐 상태로 남는다(원자적이지 않음).
	 *
	 * @param op	갱신할 Operation.
	 * @throws IllegalArgumentException	변수 개수가 일치하지 않거나, {@code null} 값이 있거나,
	 * 									변수 반영이 실패한 경우.
	 */
	public void update(Operation op) {
		Preconditions.checkNotNullArgument(op, "Operation is null");

		ElementListValue.checkUpdatable(m_inputValues, op.getInputVariables().size(), "input variable");
		ElementListValue.checkUpdatable(m_outputValues, op.getOutputVariables().size(), "output variable");
		ElementListValue.checkUpdatable(m_inoutputValues, op.getInoutputVariables().size(), "inoutput variable");

		updateVariables(op.getInputVariables(), m_inputValues);
		updateVariables(op.getOutputVariables(), m_outputValues);
		updateVariables(op.getInoutputVariables(), m_inoutputValues);
	}

	/**
	 * 주어진 {@link Operation}의 각 변수(input/output/inoutput)의 value 원소로부터
	 * {@code OperationVariableValue}를 생성한다.
	 *
	 * @param op	원본 Operation.
	 * @return 생성된 OperationVariableValue.
	 */
	public static OperationVariableValue from(Operation op) {
		return new OperationVariableValue(getVariableValues(op.getInputVariables()),
											getVariableValues(op.getOutputVariables()),
											getVariableValues(op.getInoutputVariables()));
	}

	/**
	 * 값 객체(변수 종류별 값 리스트의 {@link Map})와 템플릿 {@code OperationVariableValue}로부터
	 * {@code OperationVariableValue}를 생성한다.
	 * <p>
	 * 각 변수 값의 타입은 템플릿의 대응 변수 값으로부터 결정되며, 입력 맵에 없는 변수 종류는
	 * 빈 리스트로 간주된다.
	 *
	 * @param vobj	변수 종류(inputs/outputs/inoutputs)에서 값 리스트로의 Map.
	 * @param proto	각 변수 값의 타입 결정에 사용할 템플릿 OperationVariableValue.
	 * @return 생성된 OperationVariableValue.
	 * @throws IOException	{@code vobj}가 Map이 아니거나 변환이 실패한 경우.
	 */
	public static OperationVariableValue fromValueObject(Object vobj, OperationVariableValue proto)
		throws IOException {
		if ( vobj instanceof Map vmap ) {
			return new OperationVariableValue(
						variablesFromValueObject(vmap.get(FIELD_INPUT_VARIABLES), proto.m_inputValues),
						variablesFromValueObject(vmap.get(FIELD_OUTPUT_VARIABLES), proto.m_outputValues),
						variablesFromValueObject(vmap.get(FIELD_INOUTPUT_VARIABLES), proto.m_inoutputValues));
		}
		else {
			throw new IOException("OperationVariableValue value is not Map: obj=" + vobj);
		}
	}

	/**
	 * JSON 객체 노드와 템플릿 {@code OperationVariableValue}로부터 {@code OperationVariableValue}를
	 * 생성한다.
	 * <p>
	 * 각 변수 값의 타입은 템플릿의 대응 변수 값으로부터 결정되며, 노드에 없는 변수 종류는
	 * 빈 리스트로 간주된다.
	 *
	 * @param vnode	변수 종류(inputs/outputs/inoutputs)별 값 배열을 담은 JSON 객체 노드.
	 * @param proto	각 변수 값의 타입 결정에 사용할 템플릿 OperationVariableValue.
	 * @return 생성된 OperationVariableValue.
	 * @throws IOException	{@code vnode}가 객체가 아니거나 변환이 실패한 경우.
	 */
	public static OperationVariableValue parseValueJsonNode(JsonNode vnode, OperationVariableValue proto)
		throws IOException {
		if ( !vnode.isObject() ) {
			throw new IOException("OperationVariableValue expects an 'Object' node: JsonNode=" + vnode);
		}

		return new OperationVariableValue(
					parseVariablesJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_INPUT_VARIABLES),
											proto.m_inputValues),
					parseVariablesJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_OUTPUT_VARIABLES),
											proto.m_outputValues),
					parseVariablesJsonNode(JacksonUtils.getFieldOrNull(vnode, FIELD_INOUTPUT_VARIABLES),
											proto.m_inoutputValues));
	}

	/** polymorphic JSON 직렬화에 사용되는 타입 식별자. */
	public static final String SERIALIZATION_TYPE = "mdt:value:opvars";
	private static final String FIELD_INPUT_VARIABLES = "inputs";
	private static final String FIELD_OUTPUT_VARIABLES = "outputs";
	private static final String FIELD_INOUTPUT_VARIABLES = "inoutputs";

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	/**
	 * polymorphic JSON의 {@code value} 하위 노드로부터 {@code OperationVariableValue}를 생성한다.
	 * <p>
	 * {@code inputs}/{@code outputs}/{@code inoutputs} 필드는 각각 {@link ElementListValue}의 polymorphic
	 * JSON이어야 하며, 없는 필드는 빈 리스트로 간주된다. {@link ElementValues}의 역직렬화 등록부에서 사용된다.
	 *
	 * @param vnode	JSON 객체 노드.
	 * @return	생성된 {@code OperationVariableValue} 객체.
	 * @throws IOException	변수 목록 필드를 역직렬화할 수 없는 경우.
	 */
	public static OperationVariableValue deserializeValue(JsonNode vnode) throws IOException {
		return new OperationVariableValue(deserializeVariables(vnode, FIELD_INPUT_VARIABLES),
											deserializeVariables(vnode, FIELD_OUTPUT_VARIABLES),
											deserializeVariables(vnode, FIELD_INOUTPUT_VARIABLES));
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeStartObject();

		gen.writeFieldName(FIELD_INPUT_VARIABLES);
		ElementValues.serializeJson(new ElementListValue(m_inputValues), gen);

		gen.writeFieldName(FIELD_OUTPUT_VARIABLES);
		ElementValues.serializeJson(new ElementListValue(m_outputValues), gen);

		gen.writeFieldName(FIELD_INOUTPUT_VARIABLES);
		ElementValues.serializeJson(new ElementListValue(m_inoutputValues), gen);

		gen.writeEndObject();
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_inputValues, m_outputValues, m_inoutputValues);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || OperationVariableValue.class != obj.getClass() ) {
			return false;
		}

		OperationVariableValue other = (OperationVariableValue)obj;
		return Objects.equals(m_inputValues, other.m_inputValues)
				&& Objects.equals(m_outputValues, other.m_outputValues)
				&& Objects.equals(m_inoutputValues, other.m_inoutputValues);
	}

	private static List<ElementValue> getVariableValues(List<OperationVariable> vars) {
		return FStream.from(vars)
						.map(var -> ElementValues.getValue(var.getValue()))
						.toList();
	}

	private static void updateVariables(List<OperationVariable> vars, List<ElementValue> values) {
		FStream.zip(vars, values)
				.forEach(zip -> ElementValues.update(zip._1.getValue(), zip._2));
	}

	private static List<ElementValue> variablesFromValueObject(Object vobj, List<ElementValue> protoValues)
		throws IOException {
		if ( vobj == null ) {
			return List.of();
		}
		return ElementListValue.fromValueObject(vobj, new ElementListValue(protoValues)).getElementValues();
	}

	private static List<ElementValue> parseVariablesJsonNode(JsonNode varsNode, List<ElementValue> protoValues)
		throws IOException {
		if ( varsNode == null ) {
			return List.of();
		}
		return ElementListValue.parseValueJsonNode(varsNode, new ElementListValue(protoValues)).getElementValues();
	}

	private static List<ElementValue> deserializeVariables(JsonNode vnode, String fieldName) throws IOException {
		JsonNode varsNode = JacksonUtils.getFieldOrNull(vnode, fieldName);
		if ( varsNode == null ) {
			return List.of();
		}
		return ((ElementListValue)ElementValues.parseJsonNode(varsNode)).getElementValues();
	}
}
