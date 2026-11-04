package mdt.model.sm.ref;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.json.JacksonUtils;

import mdt.model.expr.MDTExpressionParser;
import mdt.model.ModelValidationException;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;


/**
 * MDT 인스턴스의 개별 파라미터(또는 그 하위 경로)를 가리키는 {@link ElementReference} 구현체이다.
 * <p>
 * 대상 인스턴스의 {@code Data} Submodel 안 파라미터 목록(에셋 종류에 따라
 * {@code DataInfo.Equipment.EquipmentParameterValues} 또는
 * {@code DataInfo.Operation.OperationParameterValues})에서, 파라미터 식별자(이름 또는 인덱스)와
 * 선택적 하위 경로로 지정된 {@code ParameterValue}를 참조한다.
 * <p>
 * 파라미터 명세({@code parameterExpr})는 다음 형태를 가질 수 있다.
 * <ul>
 *   <li>인덱스 번호(예: {@code "0"})</li>
 *   <li>파라미터 식별자(예: {@code "Temperature"})</li>
 *   <li>식별자 + 하위 경로(예: {@code "Temperature.unit"}, {@code "Samples[0]"})</li>
 * </ul>
 * 하위 경로는 {@code .}과 {@code [} 중 먼저 나오는 곳부터이며, 파라미터의 {@code ParameterValue} 요소
 * 아래 경로로 붙는다(예: {@code "Samples[0].value"} → {@code ...[i].ParameterValue[0].value}).
 * 대상 Submodel은 항상 인스턴스의 idShort {@code "Data"} Submodel이다.
 * 값을 읽거나 쓰기 전에 {@link #activate(MDTInstanceManager)}로 활성화해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReference extends SubmodelBasedElementReference {
	/**
	 * Json 직렬화 타입 식별자({@code "mdt:ref:param"}). {@link MDTParameterCollectionReference}와 공유한다.
	 */
	public static final String SERIALIZATION_TYPE = "mdt:ref:param";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	static final String FIELD_PARAMETER_EXPR = "parameterExpr";
	
	private final String m_parameterExpr;
	
	private MDTParameterReference(String instanceId, String parameterExpr) {
		super(DefaultSubmodelReference.ofIdShort(instanceId, "Data"));
		Preconditions.checkNotNullArgument(parameterExpr, "parameterExpr is null");
		
		m_parameterExpr = parameterExpr;
	}
	
	/**
	 * 파라미터 명세 문자열을 반환한다.
	 *
	 * @return 파라미터 명세(인덱스, 식별자 또는 식별자+하위 경로).
	 */
	public String getParameterExpr() {
		return m_parameterExpr;
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
		gen.writeStringField(FIELD_INSTANCE_ID, getInstanceId());
		gen.writeStringField(FIELD_PARAMETER_EXPR, m_parameterExpr);
	}

	/**
	 * Json 객체로부터 {@link MDTParameterReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code instanceId}, {@code parameterExpr} 필드를 담은 Json 노드.
	 * @return 복원된 {@link MDTParameterReference} 객체.
	 * @throws IOException	필수 필드가 없거나 Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static MDTParameterReference deserializeFields(JsonNode jnode) throws IOException {
		String instanceId = JacksonUtils.getTextField(jnode, FIELD_INSTANCE_ID);
		String parameterExpr = JacksonUtils.getTextField(jnode, FIELD_PARAMETER_EXPR);

		return new MDTParameterReference(instanceId, parameterExpr);
	}

	@Override
	public String toStringExpr() {
		// 파라미터 번호(정수)는 그대로, 파라미터 id와 하위 경로는 따옴표가 필요하면 감싼다.
		String paramExpr = m_parameterExpr.matches("\\d+")
							? m_parameterExpr : MDTExpressionParser.toIdShortPathExpr(m_parameterExpr);
		return String.format("param:%s:%s", MDTExpressionParser.toNameExpr(getInstanceId()), paramExpr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}

		MDTParameterReference other = (MDTParameterReference) obj;
		return getInstanceId().equals(other.getInstanceId())
				&& m_parameterExpr.equals(other.m_parameterExpr);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getInstanceId(), m_parameterExpr);
	}
	
	/**
	 * 주어진 인스턴스와 파라미터 명세로 {@link MDTParameterReference}를 생성한다.
	 *
	 * @param instanceId	대상 MDT 인스턴스의 식별자.
	 * @param parameterExpr	파라미터 명세(인덱스, 식별자 또는 식별자+하위 경로).
	 * @return 생성된 {@link MDTParameterReference} 객체.
	 * @throws IllegalArgumentException	인자가 {@code null}이거나 {@code instanceId}가 빈 문자열인 경우.
	 */
	public static MDTParameterReference newInstance(String instanceId, String parameterExpr) {
		return new MDTParameterReference(instanceId, parameterExpr);
	}
	
	/**
	 * 참조 대상 파라미터의 idShort path를 생성한다.
	 * <p>
	 * 참조가 활성화되는 시점에 호출되며, 인스턴스의 에셋 종류에 따라 파라미터 목록의 경로 접두어
	 * ({@code DataInfo.Equipment.EquipmentParameterValues} 또는
	 * {@code DataInfo.Operation.OperationParameterValues})를 결정한 뒤, {@code parameterExpr}을
	 * 해석하여 대상 {@code ParameterValue}의 경로를 만든다. {@code parameterExpr}이 숫자이면 인덱스로,
	 * 그렇지 않으면 파라미터 식별자(+선택적 하위 경로)로 간주한다.
	 *
	 * @return 대상 파라미터의 idShort path.
	 * @throws ModelValidationException	인스턴스의 에셋 종류가 설정되어 있지 않은 경우.
	 * @throws IllegalArgumentException	지원하지 않는 에셋 종류인 경우.
	 * @throws mdt.model.ResourceNotFoundException	식별자에 해당하는 파라미터가 없는 경우.
	 */
	@Override
	protected String buildIdShortPath() {
		MDTInstance instance = getInstance();
		
		MDTAssetType assetType = instance.getAssetType();
		if ( assetType == null ) {
			throw new ModelValidationException("AssetType is empty");
		}
	
		String assetTypeName = switch ( assetType ) {
			case Machine -> "Equipment";
            case Process -> "Operation";
            default -> throw new IllegalArgumentException("MDTParameter is not supported for assetType: " + assetType);
		};
		String paramCollPathPrefix = String.format("DataInfo.%s.%sParameterValues",
													assetTypeName, assetTypeName);

		String paramId = null;
		String subPath = "";
		int paramIdx;
		try {
			// 일단 parameter-id가 숫자인 것으로 가정하고 파싱을 실시하여
			// parameter의 idShortPath를 생성하고, 숫자가 아니어서 예외가 발생한 경우에는
			// 일반적인 id 기반의 idShortPath를 생성한다.
			paramIdx = Integer.parseInt(m_parameterExpr);
		}
		catch ( NumberFormatException expected ) {
			// parameter expr이 단일 parameter의 이름으로 구성되지 않고,
			// path로 구성될 수도 있기 때문에 parameter expr에 '.' 또는 '['가 포함되는지를 확인한다.
			// 만일 path인 경우에는 가장 첫번째 path segment를 'ParameterValue'로 치환시켜서
			// parameter의 idShortPath를 구성한다.
			// '.'과 '[' 중 먼저 나오는 곳에서 자른다(idShort에는 둘 다 쓸 수 없다).
			int idx = firstSeparator(m_parameterExpr);
			if ( idx >= 0 ) {
				paramId = m_parameterExpr.substring(0, idx);
				subPath = m_parameterExpr.substring(idx);
			}
			else {
				paramId = m_parameterExpr;
			}
			
			// Parameter id가 숫자가 아닌 경우에는 parameter 식별자로 간주하고
			// 해당 식별자의 Parameter를 찾는다.
			ParameterCollection paramColl = instance.getParameterCollection();
			paramIdx = paramColl.getParameterIndex(paramId);
		}
		
		return String.format("%s[%d].ParameterValue%s", paramCollPathPrefix, paramIdx, subPath);
	}

	// parameter expr에서 첫 path 구분자('.' 또는 '[')의 위치. 없으면 -1.
	private static int firstSeparator(String expr) {
		int dot = expr.indexOf('.');
		int bracket = expr.indexOf('[');
		if ( dot < 0 ) {
			return bracket;
		}
		return (bracket < 0) ? dot : Math.min(dot, bracket);
	}
}
