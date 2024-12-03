package mdt.model.sm;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;

import utils.Utilities;
import utils.func.FOption;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.aas.DefaultSubmodelReference;
import mdt.model.MDTModelSerDe;
import mdt.model.service.SimulationSubmodelService;
import mdt.model.sm.simulation.SimulationInfo;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.value.SubmodelElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementReferences {
	@SuppressWarnings("unchecked")
	public static <T extends SubmodelElement> T cast(SubmodelElementReference ref, Class<T> type) throws IOException {
		SubmodelElement sme = ref.read();
		if ( type.isInstance(sme) ) {
			return (T)sme;
		}
		else {
			String msg = String.format("Target SubmodelElement is not a %s: %s", type.getSimpleName(), sme);
			throw new IllegalArgumentException(msg);
		}
	}
	
	public static SubmodelElementReference parseString(String expr) throws IOException {
		if ( expr.equalsIgnoreCase(SubmodelElementReferenceType.STDOUT.name().toLowerCase()) ) {
			return new StdoutSMEReference();
		}
		
		Tuple<String,String> parts = Utilities.split(expr, ':', Tuple.of("default", expr));	
		SubmodelElementReferenceType refType = SubmodelElementReferenceType.fromName(parts._1);
		String refStrExpr = parts._2;
		return switch ( refType ) {
			case DEFAULT -> DefaultSubmodelElementReference.parseString(refStrExpr);
			case PARAMETER -> MDTParameterReference.parseString(refStrExpr);
			case FILE -> FileSMEReference.parseString(refStrExpr);
			case OPERATION_VARIABLE -> OperationVariableReference.parseString(refStrExpr);
			case ARGUMENT -> OperationArgumentReference.parseString(refStrExpr);
			case IN_MEMORY -> InMemorySMEReference.parseString(refStrExpr);
			case LITERAL -> LiteralSMEReference.parseString(refStrExpr);
			default -> throw new IllegalArgumentException("Invalid SubmodelElementReference expression: " + expr);
		};
	}
	
	public static String toExternalString(SubmodelElementValue smev) throws IOException {
		return ( smev instanceof PropertyValue propv )
				? FOption.getOrElse(propv.getValue(), "")
				: MDTModelSerDe.toJsonString(smev);
	}
	
	public static List<DefaultSubmodelElementReference> loadSimulationInputReferences(DefaultSubmodelReference smRef) {
		SimulationSubmodelService svc = new SimulationSubmodelService(smRef.get());
				
		SimulationInfo simInfo = svc.getSimulation().getSimulationInfo();
		return FStream.from(simInfo.getInputs())
						.zipWithIndex()
						.map(idxed -> {
							String path = String.format("SimulationInfo.Inputs[%d].InputValue", idxed.index());
							return DefaultSubmodelElementReference.newInstance(smRef, path);
						})
						.toList();
	}
	
	public static List<DefaultSubmodelElementReference> loadSimulationOutputReferences(DefaultSubmodelReference smRef) {
		SimulationSubmodelService svc = new SimulationSubmodelService(smRef.get());
				
		SimulationInfo simInfo = svc.getSimulation().getSimulationInfo();
		return FStream.from(simInfo.getInputs())
						.zipWithIndex()
						.map(idxed -> {
							String path = String.format("SimulationInfo.Outputs[%d].InputValue", idxed.index());
							return DefaultSubmodelElementReference.newInstance(smRef, path);
						})
						.toList();
	}

	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<SubmodelElementReference> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public SubmodelElementReference deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode node = parser.getCodec().readTree(parser);
			if ( node instanceof TextNode ) {
				return SubmodelElementReferences.parseString(node.asText());
			}
			
			Preconditions.checkState(node instanceof ObjectNode);
			ObjectNode root = (ObjectNode)node;
			
			String refType = FOption.mapOrElse(node.get("referenceType"), JsonNode::asText,
												SubmodelElementReferenceType.DEFAULT.name());
			
			SubmodelElementReferenceType type = SubmodelElementReferenceType.fromName(refType);
			return switch ( type ) {
				case DEFAULT -> DefaultSubmodelElementReference.parseJson(root);
				case PARAMETER -> MDTParameterReference.parseJson(root);
				case ARGUMENT -> OperationArgumentReference.parseJson(root);
				case FILE -> FileSMEReference.parseJson(root);
				case LITERAL -> LiteralSMEReference.parseJson(root);
				case STDOUT -> StdoutSMEReference.parseJson(root);
				default -> throw new IllegalArgumentException("Unknown PortType: " + type);
			};
		}
	}

	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<SubmodelElementReference> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<SubmodelElementReference> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(SubmodelElementReference ref, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			ref.serialize(gen);
		}
	}
}
