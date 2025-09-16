package mdt.model.instance;

import java.io.IOException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.func.FOption;

import mdt.model.AASUtils;
import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using=InstanceDescriptor.Serializer.class)
@JsonDeserialize(using=InstanceDescriptor.Deserializer.class)
@Getter @Setter
@Accessors(prefix="m_")
@NoArgsConstructor
public class InstanceDescriptor {
	private String m_id;
	private MDTInstanceStatus m_status;
	private @Nullable String m_baseEndpoint;
	
	private String m_aasId;
	private @Nullable String m_aasIdShort;
	private @Nullable String m_globalAssetId;
	private @Nullable MDTAssetType m_assetType;
	private @Nullable AssetKind m_assetKind;
	
	/**
	 * 대상 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return	식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * 대상 MDTInstance의 상태를 반환한다.
	 * 
	 * @return	상태 정보
	 */
	public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	/**
	 * 대상 MDTInstance에 부여된 기반 endpoint를 반환한다.
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
	 */
	@Nullable public String getBaseEndpoint() {
		return m_baseEndpoint;
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
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 * 
	 * @return	idShort.
	 */
	@Nullable
	public String getAasIdShort() {
		return m_aasIdShort;
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
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * 
	 * @return	자산 타입.
	 */
	@Nullable
	public MDTAssetType getAssetType() {
		return m_assetType;
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 종류를 반환한다.
	 * 
	 * @return	자산 종류.
	 */
	@Nullable
	public AssetKind getAssetKind() {
		return m_assetKind;
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
		
		return String.format("%s/submodel-elements/DataInfo.%s.%sParameterValues[%d]",
							smEndpoint, assetTypeName, assetTypeName, paramIdx);
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
			gen.writeStringField("assetType", FOption.map(desc.getAssetType(), MDTAssetType::name));
			gen.writeStringField("assetKind", FOption.map(desc.getAssetKind(), AssetKind::name));
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
			desc.setAssetKind(AssetKind.valueOf(node.get("assetKind").asText().toUpperCase()));
			desc.setStatus(MDTInstanceStatus.valueOf(node.get("status").asText()));
			
			JsonNode be = node.get("baseEndpoint");
			desc.setBaseEndpoint(be == null || be.isNull() ? null : be.asText());
			
			return desc;
		}
	}
}