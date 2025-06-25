package mdt.model.sm.value;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.func.FOption;

import mdt.aas.DataType;
import mdt.aas.DataTypes;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class PropertyValue<T> extends AbstractElementValue implements DataElementValue, Supplier<T> {
	protected final T m_value;
	
	abstract public DataType<T> getDataType();
	
	protected PropertyValue(T value) {
		m_value = value;
	}

	@Override
	public T get() {
		return m_value;
	}

	@Override
	public String toValueString() {
		return getDataType().toValueString(m_value);
	}
	
	static PropertyValue<?> parseValueJsonNode(Property prop, JsonNode vnode) {
		switch ( prop.getValueType() ) {
			case STRING:
				return StringPropertyValue.deserializeValue(vnode);
			case INT:
				return IntegerPropertyValue.deserializeValue(vnode);
			case FLOAT:
				return FloatPropertyValue.deserializeValue(vnode);
			case DOUBLE:
				return DoublePropertyValue.deserializeValue(vnode);
			case BOOLEAN:
				return BooleanPropertyValue.deserializeValue(vnode);
			case DATE_TIME:
				return DateTimePropertyValue.deserializeValue(vnode);
			case DURATION:
				return DurationPropertyValue.deserializeValue(vnode);
			default:
				throw new IllegalArgumentException("unknown data type: " + prop.getValueType());
		}
	}

	@Override
	protected Object toValueJsonObject() {
		return getDataType().toJsonObject(m_value);
	}

	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.getJsonMapper().writeValueAsString(this);
	}

	@Override
	public void serializeValue(JsonGenerator gen) throws IOException {
		gen.writeObject(toValueJsonObject());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_value, getDataType());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		PropertyValue<?> other = (PropertyValue<?>) obj;
		return Objects.equals(m_value, other.m_value)
				&& Objects.equals(getDataType(), other.getDataType());
	}
	
	@Override
	public String toString() {
		return toValueString();
	}
	
	public static PropertyValue<?> from(@NonNull Property prop) {
		String value = prop.getValue();
		switch ( prop.getValueType() ) {
			case STRING:
				return STRING(value);
			case INT:
				return INTEGER(DataTypes.INT.parseValueString(value));
			case FLOAT:
				return FLOAT(DataTypes.FLOAT.parseValueString(value));
			case DOUBLE:
				return DOUBLE(DataTypes.DOUBLE.parseValueString(value));
			case BOOLEAN:
				return BOOLEAN(DataTypes.BOOLEAN.parseValueString(value));
			case DATE_TIME:
				return DATE_TIME(DataTypes.DATE_TIME.parseValueString(value));
			case DURATION:
				return DURATION(DataTypes.DURATION.parseValueString(value));
			default:
				throw new IllegalArgumentException("unknown data type: " + prop.getValueType());
		}
	}
	
	public static StringPropertyValue STRING(@Nullable String value) {
		return new StringPropertyValue(value);
	}
	public static class StringPropertyValue extends PropertyValue<String> {
		public static final String SERIALIZATION_TYPE = "mdt:value:string";
		
		public StringPropertyValue(String value) {
			super(value);
		}

		@Override
		public DataType<String> getDataType() {
			return DataTypes.STRING;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static StringPropertyValue deserializeValue(JsonNode vnode) {
			return new StringPropertyValue(DataTypes.STRING.fromJsonNode(vnode));
		}
	}

	public static IntegerPropertyValue INTEGER(Integer value) {
		return new IntegerPropertyValue(value);
	}
	public static class IntegerPropertyValue extends PropertyValue<Integer> {
		public static final String SERIALIZATION_TYPE = "mdt:value:integer";
		
		public IntegerPropertyValue(Integer value) {
			super(value);
		}

		@Override
		public DataType<Integer> getDataType() {
			return DataTypes.INT;
		}

		@Override
		public String toValueJsonString() {
			return FOption.map(m_value, v -> Integer.toString(v));
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static IntegerPropertyValue deserializeValue(JsonNode vnode) {
			return new IntegerPropertyValue(DataTypes.INT.fromJsonNode(vnode));
		}
	}

	public static FloatPropertyValue FLOAT(Float value) {
		return new FloatPropertyValue(value);
	}
	public static class FloatPropertyValue extends PropertyValue<Float> {
		public static final String SERIALIZATION_TYPE = "mdt:value:float";
		
		public FloatPropertyValue(Float value) {
			super(value);
		}

		@Override
		public DataType<Float> getDataType() {
			return DataTypes.FLOAT;
		}

		@Override
		public String toValueJsonString() {
			return FOption.map(m_value, v -> Float.toString(v));
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static FloatPropertyValue deserializeValue(JsonNode vnode) {
			return new FloatPropertyValue(DataTypes.FLOAT.fromJsonNode(vnode));
		}
	}

	public static DoublePropertyValue DOUBLE(Double value) {
		return new DoublePropertyValue(value);
	}
	public static class DoublePropertyValue extends PropertyValue<Double> {
		public static final String SERIALIZATION_TYPE = "mdt:value:double";
		
		public DoublePropertyValue(Double value) {
			super(value);
		}

		@Override
		public DataType<Double> getDataType() {
			return DataTypes.DOUBLE;
		}

		@Override
		public String toValueJsonString() {
			return FOption.map(m_value, v -> Double.toString(v));
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static DoublePropertyValue deserializeValue(JsonNode vnode) {
			return new DoublePropertyValue(DataTypes.DOUBLE.fromJsonNode(vnode));
		}
	}

	public static BooleanPropertyValue BOOLEAN(Boolean value) {
		return new BooleanPropertyValue(value);
	}
	public static class BooleanPropertyValue extends PropertyValue<Boolean> {
		public static final String SERIALIZATION_TYPE = "mdt:value:boolean";
		
		public BooleanPropertyValue(Boolean value) {
			super(value);
		}

		@Override
		public DataType<Boolean> getDataType() {
			return DataTypes.BOOLEAN;
		}

		@Override
		public String toValueJsonString() {
			return FOption.map(m_value, v -> Boolean.toString(v));
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static BooleanPropertyValue deserializeValue(JsonNode vnode) {
			return new BooleanPropertyValue(DataTypes.BOOLEAN.fromJsonNode(vnode));
		}
	}

	public static DateTimePropertyValue DATE_TIME(Instant value) {
		return new DateTimePropertyValue(value);
	}
	public static class DateTimePropertyValue extends PropertyValue<Instant> {
		public static final String SERIALIZATION_TYPE = "mdt:value:dateTime";
		
		public DateTimePropertyValue(Instant value) {
			super(value);
		}

		@Override
		public DataType<Instant> getDataType() {
			return DataTypes.DATE_TIME;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static DateTimePropertyValue deserializeValue(JsonNode vnode) {
			return new DateTimePropertyValue(DataTypes.DATE_TIME.fromJsonNode(vnode));
		}
	}

	public static DurationPropertyValue DURATION(Duration value) {
		return new DurationPropertyValue(value);
	}
	public static class DurationPropertyValue extends PropertyValue<Duration> {
		public static final String SERIALIZATION_TYPE = "mdt:value:duration";
		
		public DurationPropertyValue(Duration value) {
			super(value);
		}

		@Override
		public DataType<Duration> getDataType() {
			return DataTypes.DURATION;
		}

		@Override
		public String getSerializationType() {
			return SERIALIZATION_TYPE;
		}
		
		public static DurationPropertyValue deserializeValue(JsonNode vnode) {
			return new DurationPropertyValue(DataTypes.DURATION.fromJsonNode(vnode));
		}
	}
	
	private String quote(String value) {
		if ( value != null ) {
			return "\"" + value + "\"";
		}
		else {
			return "null";
		}
	}
}
