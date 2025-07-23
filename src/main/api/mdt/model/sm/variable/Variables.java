package mdt.model.sm.variable;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import lombok.experimental.UtilityClass;

import utils.Throwables;
import utils.json.JacksonDeserializationException;
import utils.json.JacksonUtils;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.LiteralExpr;
import mdt.model.expr.MDTElementReferenceExpr;
import mdt.model.expr.MDTExpr;
import mdt.model.expr.MDTExprParser;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.variable.AbstractVariable.ElementVariable;
import mdt.model.sm.variable.AbstractVariable.ReferenceVariable;
import mdt.model.sm.variable.AbstractVariable.ValueVariable;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Variables {
	public static void activate(Variable var, MDTInstanceManager manager) {
		Preconditions.checkArgument(var != null, "Variable is null");
		Preconditions.checkArgument(manager != null, "MDTInstanceManager is null");

		if ( var instanceof ReferenceVariable refVar ) {
			refVar.activate(manager);
		}
//		else {
//			throw new IllegalArgumentException("Variable is not a ReferenceVariable: " + var);
//		}
	}
	
	public static ReferenceVariable newInstance(String name, String description, ElementReference ref) {
		return new ReferenceVariable(name, description, ref);
	}
	
	public static ValueVariable newInstance(String name, String description, ElementValue value) {
		return new ValueVariable(name, description, value);
	}
	
	public static ElementVariable newInstance(String name, String description, SubmodelElement element) {
		return new ElementVariable(name, description, element);
	}
	
	public static Variable newValueVariable(String name, String description, String valueExpr) {
		ElementValue elmValue = MDTExprParser.parseValueLiteral(valueExpr).evaluate();
		return Variables.newInstance(name, description, elmValue);
	}
	
	public static Variable newReferenceVariable(String name, String description, String refExpr) {
		MDTExpr expr = MDTExprParser.parseExpr(refExpr);
		if ( expr instanceof MDTElementReferenceExpr elmRef ) {
			try {
				return Variables.newInstance(name, "", elmRef.evaluate());
			}
			catch ( Exception e ) {
				Throwable cause = Throwables.unwrapThrowable(e);
				String msg = String.format("Failed to parse variable expr: (\"%s\"), ref=%s, cause=%s",
											name, refExpr, cause);
				throw new IllegalArgumentException(msg);
			}
		}
		else if ( expr instanceof LiteralExpr lit ) {
			return Variables.newInstance(name, "", lit.evaluate());
		}
		else {
			throw new IllegalArgumentException("Unexpected variable expression: name="
												+ name + ", expr=" + refExpr);
		}
	}
	
	public static AbstractVariable newInstance(String name, String description, String expr) {
		Object valObj = MDTExprParser.parseExpr(expr).evaluate();
		if ( valObj instanceof ElementReference ref ) {
			return newInstance(name, description, ref);
		}
		else if ( valObj instanceof ElementValue value ) {
			return newInstance(name, description, value);
		}
		else {
			throw new IllegalArgumentException("Invalid expression for TaskVariable's value: expr=" + expr);
		}
	}
	
	public static AbstractVariable parseJsonNode(JsonNode jnode) throws IOException {
		return MDTModelSerDe.readValue(jnode, AbstractVariable.class);
	}
	
	public static AbstractVariable parseJsonString(String jsonString) throws IOException {
		return MDTModelSerDe.readValue(jsonString, AbstractVariable.class);
	}

	private static final String FIELD_TYPE = "@type";
	private static final BiMap<String,Class<? extends Variable>> SERIALIZABLES = HashBiMap.create();
	static {
		SERIALIZABLES.put(ReferenceVariable.SERIALIZATION_TYPE, ReferenceVariable.class);
		SERIALIZABLES.put(ValueVariable.SERIALIZATION_TYPE, ValueVariable.class);
		SERIALIZABLES.put(ElementVariable.SERIALIZATION_TYPE, ElementVariable.class);
	}
	
	@SuppressWarnings("serial")
	public static class Serializer extends StdSerializer<Variable> {
		private Serializer() {
			this(null);
		}
		private Serializer(Class<Variable> cls) {
			super(cls);
		}
		
		@Override
		public void serialize(Variable serde, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField(FIELD_TYPE, serde.getSerializationType());
			serde.serializeFields(gen);
			gen.writeEndObject();
		}
	}

	@SuppressWarnings("serial")
	public static class Deserializer extends StdDeserializer<Variable> {
		public Deserializer() {
			this(null);
		}
		public Deserializer(Class<?> vc) {
			super(vc);
		}
	
		@Override
		public Variable deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
			JsonNode jnode = parser.getCodec().readTree(parser);
			return parseTypedJsonNode(jnode);
		}
	}
	
	public static Variable parseTypedJsonNode(JsonNode jnode) throws IOException {
		String type = JacksonUtils.getStringFieldOrNull(jnode, FIELD_TYPE);
		if ( type == null ) {
			throw new JacksonDeserializationException(String.format("'%s' field is missing: json=%s",
																	FIELD_TYPE, jnode));
		}
		
		switch ( type ) {
			case ReferenceVariable.SERIALIZATION_TYPE:
				return ReferenceVariable.deserializeFields(jnode);
			case ValueVariable.SERIALIZATION_TYPE:
				return ValueVariable.deserializeFields(jnode);
			case ElementVariable.SERIALIZATION_TYPE:
				return ElementVariable.deserializeFields(jnode);
			default:
				throw new JacksonDeserializationException("Unregistered Variable type: " + type);
		}
	}
}
