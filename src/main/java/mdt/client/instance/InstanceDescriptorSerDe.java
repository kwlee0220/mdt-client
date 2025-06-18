package mdt.client.instance;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.InternalException;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.instance.DefaultInstanceDescriptor;
import mdt.model.instance.DefaultInstanceSubmodelDescriptor;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InstanceDescriptorSerDe {
	public static final JsonMapper MAPPER = new JsonMapper();
	static {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(InstanceDescriptor.class, new InstanceDescriptorDeserializer());
		module.addSerializer(InstanceDescriptor.class, new InstanceDescriptorSerializer());
		MAPPER.registerModule(module);
	}
	
	public InstanceDescriptor readInstanceDescriptor(String json) {
		if ( json == null ) {
			return null;
		}
		
		try {
			return MAPPER.readValue(json, InstanceDescriptor.class);
		}
		catch ( Exception e ) {
			throw new InternalException("" + e);
		}
	}
	public List<InstanceDescriptor> readInstanceDescriptorList(String json) {
		if ( json == null ) {
			return null;
		}
		
		try {
			return MAPPER.readValue(json, new TypeReference<List<InstanceDescriptor>>() {});
		}
		catch ( Exception e ) {
			throw new InternalException("" + e);
		}
	}
	
	public String toJson(InstanceDescriptor desc) {
		if ( desc == null ) {
			return null;
		}
		
		try {
			return MAPPER.writeValueAsString(desc);
		}
		catch ( JsonProcessingException e ) {
			throw new InternalException("" + e);
		}
	}
	public String toJson(List<? extends InstanceDescriptor> descList) {
		if ( descList == null ) {
			return null;
		}
		
		try {
			return MAPPER.writeValueAsString(descList);
		}
		catch ( JsonProcessingException e ) {
			throw new InternalException("" + e);
		}
	}

	private static class InstanceDescriptorDeserializer extends StdDeserializer<InstanceDescriptor> {
		private static final long serialVersionUID = 1L;

		private InstanceDescriptorDeserializer() {
			this(null);
		}
		private InstanceDescriptorDeserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public InstanceDescriptor deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			ObjectCodec codec = parser.getCodec();
			JsonNode node = codec.readTree(parser);
			
			DefaultInstanceDescriptor desc = new DefaultInstanceDescriptor();
			desc.setId(getStringField(node, "id"));
			desc.setAasId(getStringField(node, "aasId"));
			desc.setAasIdShort(getStringField(node, "aasIdShort"));
			desc.setGlobalAssetId(getStringField(node, "globalAssetId"));
			desc.setAssetType(MDTAssetType.valueOf(getStringField(node, "assetType")));
			desc.setAssetKind(FOption.map(getStringField(node, "assetKind"), AssetKind::valueOf));
			
			ArrayNode submodelNodes = (ArrayNode)node.get("submodels");
			desc.setSubmodels(FStream.from(submodelNodes.elements())
										.map(n -> {
											String id = getStringField(n, "id");
											String idShort = getStringField(n, "idShort");
											String semanticId = getStringField(n, "semanticId");
											
											return new DefaultInstanceSubmodelDescriptor(id, idShort, semanticId);
										})
										.toList());
			return desc;
		}
		
		private String getStringField(JsonNode node, String fieldName) {
			JsonNode field = node.get(fieldName);
			return (!field.isNull()) ? field.asText() : null;
		}
	}
	
	private static class InstanceDescriptorSerializer extends StdSerializer<InstanceDescriptor> {
		private static final long serialVersionUID = 1L;

		private InstanceDescriptorSerializer() {
			this(null);
		}
		private InstanceDescriptorSerializer(Class<InstanceDescriptor> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(InstanceDescriptor desc, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField("id", desc.getId());
			gen.writeStringField("aasId", desc.getAasId());
			gen.writeStringField("aasIdShort", desc.getAasIdShort());
			gen.writeStringField("globalAssetId", desc.getGlobalAssetId());
			gen.writeStringField("assetType", desc.getAssetType().name());
			gen.writeStringField("assetKind", desc.getAssetKind().name());
			
			gen.writeArrayFieldStart("submodels");
			for ( InstanceSubmodelDescriptor smDesc: desc.getInstanceSubmodelDescriptorAll() ) {
				serialize(smDesc, gen);
			}
			gen.writeEndArray();
			
			gen.writeEndObject();
		}
		
		private void serialize(InstanceSubmodelDescriptor desc, JsonGenerator gen) throws IOException {
			gen.writeStartObject();
			gen.writeStringField("id", desc.getId());
			gen.writeStringField("idShort", desc.getIdShort());
			gen.writeStringField("semanticId", desc.getSemanticId());
			gen.writeEndObject();
		}
	}
}
