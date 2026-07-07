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
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class OperationVariableValue extends AbstractElementValue implements ElementValue {
    private final List<ElementValue> m_inputValues;
    private final List<ElementValue> m_outputValues;
    private final List<ElementValue> m_inoutputValues;

    public OperationVariableValue(List<ElementValue> inValues, List<ElementValue> outValues,
    								List<ElementValue> inoutValues) {
		Preconditions.checkNotNullArgument(inValues, "inValues is null");
		Preconditions.checkNotNullArgument(outValues, "outValues is null");
		Preconditions.checkNotNullArgument(inoutValues, "inoutValues is null");

    	m_inputValues = inValues;
    	m_outputValues = outValues;
    	m_inoutputValues = inoutValues;
    }

	public List<ElementValue> getInputValues() {
		return m_inputValues;
	}

	public List<ElementValue> getOutputValues() {
		return m_outputValues;
	}

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
	 * @param op	갱신할 Operation.
	 * @throws IllegalArgumentException	변수 개수가 일치하지 않는 경우.
	 */
	public void update(Operation op) {
		Preconditions.checkNotNullArgument(op, "Operation is null");

		updateVariables(op.getInputVariables(), m_inputValues, "input");
		updateVariables(op.getOutputVariables(), m_outputValues, "output");
		updateVariables(op.getInoutputVariables(), m_inoutputValues, "inoutput");
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

	public static final String SERIALIZATION_TYPE = "mdt:value:opvars";
	private static final String FIELD_INPUT_VARIABLES = "inputs";
	private static final String FIELD_OUTPUT_VARIABLES = "outputs";
	private static final String FIELD_INOUTPUT_VARIABLES = "inoutputs";

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

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

	private static void updateVariables(List<OperationVariable> vars, List<ElementValue> values, String kind) {
		Preconditions.checkArgument(vars.size() == values.size(),
									"Operation %s variable count mismatch: variables.size=%s, values.size=%s",
									kind, vars.size(), values.size());
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
