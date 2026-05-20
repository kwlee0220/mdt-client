package mdt.model.instance;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.NoArgsConstructor;

import utils.Preconditions;

import mdt.model.AASUtils;
import mdt.model.sm.info.MDTAssetType;

/**
 * {@link InstanceDescriptor}는 MDTInstance의 메타데이터를 담는 디스크립터 객체이다.
 * <p>
 * 본 클래스는 MDTInstance를 식별하고 그 상태 및 포함된 AssetAdministrationShell의
 * 기본 정보를 표현하기 위해 사용되며, JSON으로 직렬화/역직렬화되어 클라이언트와 서버 간에
 * 교환된다.
 * <p>
 * 필드는 다음 두 그룹으로 구분된다.
 * <ul>
 *   <li><b>필수(required)</b>: {@code id}, {@code aasId}, {@code status}, {@code assetType}.
 *       해당 필드들의 setter는 {@code null} 값을 거부하며, JSON 역직렬화 시에도 누락되면
 *       예외가 발생한다.</li>
 *   <li><b>선택(optional)</b>: {@code baseEndpoint}, {@code aasIdShort}, {@code globalAssetId}.
 *       {@code null}이 허용되며 {@link Nullable}로 표기된다. 특히 {@code baseEndpoint}는
 *       MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우 {@code null}이다.</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using=InstanceDescriptor.Serializer.class)
@JsonDeserialize(using=InstanceDescriptor.Deserializer.class)
@NoArgsConstructor
public class InstanceDescriptor {
	private String m_id;
	private MDTInstanceStatus m_status;
	private @Nullable String m_baseEndpoint;
	
	private String m_aasId;
	private @Nullable String m_aasIdShort;
	private @Nullable String m_globalAssetId;
	private MDTAssetType m_assetType;
	
	/**
	 * 대상 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return	식별자.
	 */
	@NotNull public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		Preconditions.checkNotNullArgument(id, "id should not be null in InstanceDescriptor");
		
		m_id = id;
	}
	
	/**
	 * 대상 MDTInstance의 상태를 반환한다.
	 * 
	 * @return	상태 정보
	 */
	@NotNull public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	public void setStatus(MDTInstanceStatus status) {
		Preconditions.checkNotNullArgument(status, "status should not be null in InstanceDescriptor");
		
		m_status = status;
	}
	
	/**
	 * 대상 MDTInstance에 부여된 기반 endpoint를 반환한다.
	 * <p>
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
	 *
	 * @return	기반 endpoint URL. MDTInstance가 RUNNING 상태가 아닌 경우 {@code null}.
	 */
	@Nullable public String getBaseEndpoint() {
		return m_baseEndpoint;
	}
	
	public void setBaseEndpoint(String endpoint) {
		m_baseEndpoint = endpoint;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 식별자를 반환한다.
	 * 
	 * @return	AAS 식별자.
	 */
	@NotNull
	public String getAasId() {
		return m_aasId;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 식별자를 Base64 URL-safe 형식으로
	 * 인코딩한 값을 반환한다.
	 * <p>
	 * 본 값은 {@link #getAasId()}로부터 매번 파생 계산되는 read-only 속성이며,
	 * JSON 직렬화 결과에만 포함된다. 역직렬화 시에는 무시되며, 별도의 setter도 제공되지 않는다.
	 *
	 * @return	Base64 URL-safe 인코딩된 AAS 식별자.
	 */
	@NotNull
	public String getAasIdEncoded() {
		return AASUtils.encodeBase64UrlSafe(m_aasId);
	}
	
	public void setAasId(String aasId) {
		Preconditions.checkNotNullArgument(aasId, "aasId should not be null in InstanceDescriptor");
		
		m_aasId = aasId;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 * 
	 * @return	idShort.
	 */
	@Nullable
	public String getAasIdShort() {
		return m_aasIdShort;
	}
	
	public void setAasIdShort(String aasIdShort) {
		m_aasIdShort = aasIdShort;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId 를 반환한다.
	 * 
	 * @return	자산 식별자.
	 */
	@Nullable
	public String getGlobalAssetId() {
		return m_globalAssetId;
	}
	
	public void setGlobalAssetId(String globalAssetId) {
		m_globalAssetId = globalAssetId;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * 
	 * @return	자산 타입.
	 */
	@NotNull
	public MDTAssetType getAssetType() {
		return m_assetType;
	}
	
	public void setAssetType(MDTAssetType assetType) {
		Preconditions.checkNotNullArgument(assetType, "assetType should not be null in InstanceDescriptor");
		
		m_assetType = assetType;
	}

	/**
	 * 주어진 인덱스에 해당하는 MDTParameter의 endpoint URL을 구성하여 반환한다.
	 * <p>
	 * 자산 타입({@link #getAssetType()})이 {@link MDTAssetType#Machine}인 경우 {@code Equipment},
	 * {@link MDTAssetType#Process}인 경우 {@code Operation}을 기준으로 idShortPath를 구성하며,
	 * 그 외의 자산 타입은 지원되지 않는다.
	 *
	 * @param paramIdx		MDTParameter의 인덱스(0-based).
	 * @param smEndpoint	대상 Submodel의 endpoint URL.
	 * @return				MDTParameter 값에 접근할 수 있는 endpoint URL.
	 * @throws IllegalStateException
	 * 						{@code smEndpoint} 또는 {@code assetType}이 설정되지 않았거나,
	 * 						{@code assetType}이 {@code Machine}/{@code Process}가 아닌 경우.
	 */
	public String getParameterEndpoint(int paramIdx, String smEndpoint) {
		Preconditions.checkNotNullArgument(smEndpoint, "smEndpoint should not be null in InstanceDescriptor");
		Preconditions.checkArgument(paramIdx >= 0, "paramIdx should be greater than or equal to 0 in InstanceDescriptor");
		Preconditions.checkState(m_assetType != null, "assetType should not be null in InstanceDescriptor");

		String assetTypeName = switch ( m_assetType ) {
			case Machine -> "Equipment";
            case Process -> "Operation";
            default -> throw new IllegalStateException("MDTParameter is not supported for assetType: " + m_assetType);
		};

		String idShortPath = String.format("DataInfo.%s.%sParameterValues[%d].ParameterValue",
											assetTypeName, assetTypeName, paramIdx);
		String encodedIdShortPath = URLEncoder.encode(idShortPath, StandardCharsets.UTF_8);
		return String.format("%s/submodel-elements/%s", smEndpoint, encodedIdShortPath);
	}

	/**
	 * {@link InstanceDescriptor}를 JSON 객체로 직렬화하는 Jackson 시리얼라이저.
	 * <p>
	 * 모든 필드가 평면(flat) JSON 객체로 출력되며, 직렬화 결과에는 추가로
	 * {@code aasIdEncoded}(AAS 식별자의 Base64 URL-safe 인코딩) 필드가 포함된다.
	 * 필수 필드인 {@code id}/{@code aasId}/{@code assetType}/{@code status}가 {@code null}이면 직렬화 시점에
	 * {@link IllegalStateException}이 발생한다.
	 */
	public static class Serializer extends StdSerializer<InstanceDescriptor> {
		private static final long serialVersionUID = 1L;

		public Serializer() {
			this(null);
		}
		public Serializer(Class<InstanceDescriptor> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(InstanceDescriptor desc, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
			// 사전에 validation 수행
			Preconditions.checkState(desc.getId() != null, "id should not be null in InstanceDescriptor");
			Preconditions.checkState(desc.getAasId() != null, "aasId should not be null in InstanceDescriptor");
			MDTAssetType at = desc.getAssetType();
			Preconditions.checkState(at != null, "assetType should not be null in InstanceDescriptor");
			MDTInstanceStatus st = desc.getStatus();
			Preconditions.checkState(st != null, "status should not be null in InstanceDescriptor");

			gen.writeStartObject();
			gen.writeStringField("id", desc.getId());
			gen.writeStringField("aasId", desc.getAasId());
			gen.writeStringField("aasIdEncoded", desc.getAasIdEncoded());
			gen.writeStringField("aasIdShort", desc.getAasIdShort());
			gen.writeStringField("globalAssetId", desc.getGlobalAssetId());
			gen.writeStringField("assetType", at.name());
			gen.writeStringField("status", st.name());
			gen.writeStringField("baseEndpoint", desc.getBaseEndpoint());
			
			gen.writeEndObject();
		}
	}
	
	/**
	 * JSON 객체로부터 {@link InstanceDescriptor}를 역직렬화하는 Jackson 디시리얼라이저.
	 * <p>
	 * 필수 필드({@code id}, {@code aasId}, {@code assetType}, {@code status})가 누락되거나
	 * {@code null}인 경우 {@link DeserializationContext#reportInputMismatch} 를 통해
	 * {@code MismatchedInputException}을 발생시킨다.
	 * 직렬화 시 포함되는 {@code aasIdEncoded}는 파생 값이므로 역직렬화 시 무시된다.
	 */
	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<InstanceDescriptor> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public InstanceDescriptor deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException {
			JsonNode node = parser.getCodec().readTree(parser);

			InstanceDescriptor desc = new InstanceDescriptor();

			desc.setId(requireTextField(node, "id", ctxt));
			desc.setAasId(requireTextField(node, "aasId", ctxt));
			desc.setAasIdShort(getText(node.get("aasIdShort"), "aasIdShort", ctxt));
			desc.setGlobalAssetId(getText(node.get("globalAssetId"), "globalAssetId", ctxt));
			desc.setAssetType(parseAssetType(requireTextField(node, "assetType", ctxt), ctxt));
			desc.setStatus(parseStatus(requireTextField(node, "status", ctxt), ctxt));
			desc.setBaseEndpoint(getText(node.get("baseEndpoint"), "baseEndpoint", ctxt));

			return desc;
		}

		private MDTAssetType parseAssetType(String value, DeserializationContext ctxt)
			throws IOException {
			try {
				return MDTAssetType.valueOf(value);
			}
			catch ( IllegalArgumentException e ) {
				return ctxt.reportInputMismatch(InstanceDescriptor.class,
												"invalid assetType in InstanceDescriptor: %s", value);
			}
		}

		private MDTInstanceStatus parseStatus(String value, DeserializationContext ctxt)
			throws IOException {
			try {
				return MDTInstanceStatus.valueOf(value);
			}
			catch ( IllegalArgumentException e ) {
				return ctxt.reportInputMismatch(InstanceDescriptor.class,
												"invalid status in InstanceDescriptor: %s", value);
			}
		}

		private String requireTextField(JsonNode node, String fieldName, DeserializationContext ctxt)
			throws IOException {
			JsonNode v = requireField(node, fieldName, ctxt);
			if ( !v.isTextual() ) {
				return ctxt.reportInputMismatch(InstanceDescriptor.class,
												"%s should be a string in InstanceDescriptor", fieldName);
			}
			return v.asText();
		}
		
		private String getText(JsonNode node, String fieldName, DeserializationContext ctxt)
			throws IOException {
			if ( node == null || node.isNull() ) {
				return null;
			}
			if ( !node.isTextual() ) {
				return ctxt.reportInputMismatch(InstanceDescriptor.class,
												"%s should be a string in InstanceDescriptor", fieldName);
			}
			return node.asText();
		}

		private JsonNode requireField(JsonNode node, String fieldName, DeserializationContext ctxt)
			throws IOException {
			JsonNode v = node.get(fieldName);
			if ( v == null || v.isNull() ) {
				return (JsonNode)ctxt.reportInputMismatch(InstanceDescriptor.class,
														"missing %s in InstanceDescriptor", fieldName);
			}
			return v;
		}
	}
}