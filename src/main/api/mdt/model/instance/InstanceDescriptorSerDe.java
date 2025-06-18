package mdt.model.instance;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import utils.stream.FStream;

import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InstanceDescriptorSerDe {
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
			gen.writeStringField("status", desc.getStatus().name());
			gen.writeStringField("baseEndpoint", desc.getBaseEndpoint());
			gen.writeStringField("aasId", desc.getAasId());
			gen.writeStringField("aasIdShort", desc.getAasIdShort());
			gen.writeStringField("globalAssetId", desc.getGlobalAssetId());
			if ( desc.getAssetType() != null ) {
				gen.writeStringField("assetType", desc.getAssetType().name());
			}
			if ( desc.getAssetKind() != null ) {
				gen.writeStringField("assetKind", desc.getAssetKind().name());
			}
			
			gen.writeArrayFieldStart("submodels");
			for ( InstanceSubmodelDescriptor smDesc : desc.getInstanceSubmodelDescriptorAll() ) {
				gen.writeObject(smDesc);
			}
			gen.writeEndArray();
			
			gen.writeArrayFieldStart("parameters");
			for ( MDTParameterDescriptor param : desc.getMDTParameterDescriptorAll() ) {
				gen.writeObject(param);
			}
			gen.writeEndArray();
			
			gen.writeArrayFieldStart("operations");
			for ( MDTOperationDescriptor op : desc.getMDTOperationDescriptorAll() ) {
				gen.writeObject(op);
			}
			gen.writeEndArray();
			
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
			
			DefaultInstanceDescriptor desc = new DefaultInstanceDescriptor();
			desc.setId(node.get("id").asText());
			desc.setAasId(node.get("aasId").asText());
			desc.setAasIdShort(getText(node.get("aasIdShort")));
			desc.setGlobalAssetId(getText(node.get("globalAssetId")));
			desc.setAssetType(MDTAssetType.valueOf(getText(node.get("assetType"))));
			desc.setAssetKind(AssetKind.valueOf(node.get("assetKind").asText()));
			desc.setStatus(MDTInstanceStatus.valueOf(node.get("status").asText()));
			
			JsonNode be = node.get("baseEndpoint");
			desc.setBaseEndpoint(be.isNull() ? null : be.asText());
			
			List<DefaultInstanceSubmodelDescriptor> smDescList
						= FStream.from(node.get("submodels").elements())
								.mapOrThrow(smNode -> smNode.traverse(parser.getCodec())
															.readValueAs(DefaultInstanceSubmodelDescriptor.class))
								.toList();
			desc.setSubmodels(smDescList);
			
			List<DefaultAssetParameterDescriptor> paramDescList
					= FStream.from(node.get("parameters").elements())
							.mapOrThrow(paramNode -> paramNode.traverse(parser.getCodec())
																.readValueAs(DefaultAssetParameterDescriptor.class))
							.toList();
			desc.setAssetParameters(paramDescList);
			
			List<DefaultMDTOperationDescriptor> opDescList
					= FStream.from(node.get("operations").elements())
							.mapOrThrow(opNode -> opNode.traverse(parser.getCodec())
																.readValueAs(DefaultMDTOperationDescriptor.class))
							.toList();
			desc.setAssetOperations(opDescList);
			
			return desc;
		}
	}
}
