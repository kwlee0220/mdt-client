package mdt.model.instance;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.NoArgsConstructor;

import mdt.model.AASUtils;
import mdt.model.sm.info.MDTAssetType;

/**
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
	private @Nullable MDTAssetType m_assetType;
	
	/**
	 * 대상 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return	식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		m_id = id;
	}
	
	/**
	 * 대상 MDTInstance의 상태를 반환한다.
	 * 
	 * @return	상태 정보
	 */
	public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	public void setStatus(MDTInstanceStatus status) {
		m_status = status;
	}
	
	/**
	 * 대상 MDTInstance에 부여된 기반 endpoint를 반환한다.
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
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
	public String getAasId() {
		return m_aasId;
	}
	
	public String getAasIdEncoded() {
		return AASUtils.encodeBase64UrlSafe(m_aasId);
	}
	
	public void setAasId(String aasId) {
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
	
	public void setGlobalAssetId(String assetId) {
		m_globalAssetId = assetId;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * 
	 * @return	자산 타입.
	 */
	@Nullable
	public MDTAssetType getAssetType() {
		return m_assetType;
	}
	
	public void setAssetType(MDTAssetType assetType) {
		m_assetType = assetType;
	}
	
	public String getParameterEndpoint(int paramIdx, String smEndpoint) {
		if ( m_baseEndpoint == null ) {
			throw new IllegalStateException("no base endpoint");
		}
		
		String assetTypeName = switch ( m_assetType ) {
			case Machine -> "Equipment";
            case Process -> "Operation";
            default -> throw new IllegalArgumentException("MDTParameter is not supported for assetType: " + m_assetType);
		};
		
		String idShortPath = String.format("DataInfo.%s.%sParameterValues[%d].ParameterValue",
											assetTypeName, assetTypeName, paramIdx);
		String encodedIdShortPath = URLEncoder.encode(idShortPath, StandardCharsets.UTF_8);
		return String.format("%s/submodel-elements/%s", smEndpoint, encodedIdShortPath);
	}

	public static class Serializer extends StdSerializer<InstanceDescriptor> {
		private static final long serialVersionUID = 1L;

		private Serializer() {
			this(null);
		}
		private Serializer(Class<InstanceDescriptor> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(InstanceDescriptor desc, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField("id", desc.getId());
			gen.writeStringField("aasId", desc.getAasId());
			gen.writeStringField("aasIdEncoded", desc.getAasIdEncoded());
			gen.writeStringField("aasIdShort", desc.getAasIdShort());
			gen.writeStringField("globalAssetId", desc.getGlobalAssetId());
			gen.writeStringField("assetType", (desc.getAssetType() != null) ? desc.getAssetType().name() : null);
			gen.writeStringField("status", desc.getStatus().name());
			gen.writeStringField("baseEndpoint", desc.getBaseEndpoint());
			
			gen.writeEndObject();
		}
	}
	
	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<InstanceDescriptor> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
		
		private String getText(JsonNode node) {
			return (node == null || node.isNull()) ? null : node.asText();
		}
	
		@Override
		public InstanceDescriptor deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode node = parser.getCodec().readTree(parser);
			
			InstanceDescriptor desc = new InstanceDescriptor();
			desc.setId(node.get("id").asText());
			desc.setAasId(node.get("aasId").asText());
			desc.setAasIdShort(getText(node.get("aasIdShort")));
			desc.setGlobalAssetId(getText(node.get("globalAssetId")));
			desc.setAssetType(MDTAssetType.valueOf(node.get("assetType").asText()));
			desc.setStatus(MDTInstanceStatus.valueOf(node.get("status").asText()));
			
			JsonNode be = node.get("baseEndpoint");
			desc.setBaseEndpoint(be == null || be.isNull() ? null : be.asText());
			
			return desc;
		}
	}
}