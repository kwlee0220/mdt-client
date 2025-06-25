package mdt.model.sm.value;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.Assert;
import org.junit.Test;

import mdt.aas.DataTypes;
import mdt.model.sm.value.PropertyValue.BooleanPropertyValue;
import mdt.model.sm.value.PropertyValue.DateTimePropertyValue;
import mdt.model.sm.value.PropertyValue.DoublePropertyValue;
import mdt.model.sm.value.PropertyValue.DurationPropertyValue;
import mdt.model.sm.value.PropertyValue.FloatPropertyValue;
import mdt.model.sm.value.PropertyValue.IntegerPropertyValue;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class PropertyValueTest {
	private static final String STRING_JSON = "{\"@type\":\"mdt:value:string\",\"value\":\"abc\"}";
	private static final String STRING_VALUE_JSON = "\"abc\"";
	private static final String STRING_NULL_JSON = "{\"@type\":\"mdt:value:string\",\"value\":null}";
	private static final String STRING_NULL_VALUE_JSON = "null";
	@Test
	public void testStringPropertyValue() throws IOException {
		StringPropertyValue value = PropertyValue.STRING("abc");
		Assert.assertEquals(STRING_JSON, value.toJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(STRING_JSON);
		Assert.assertTrue(ev instanceof StringPropertyValue);
		StringPropertyValue parsedValue = (StringPropertyValue)ev;
		Assert.assertEquals("abc", parsedValue.get());
		
		StringPropertyValue nullValue = PropertyValue.STRING(null);
		Assert.assertEquals(STRING_NULL_JSON, nullValue.toJsonString());
		Assert.assertEquals(STRING_NULL_VALUE_JSON, nullValue.toValueJsonString());
		
		ev = ElementValues.parseJsonString(STRING_NULL_JSON);
		Assert.assertTrue(ev instanceof StringPropertyValue);
		parsedValue = (StringPropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		Assert.assertEquals(STRING_VALUE_JSON, value.toValueJsonString());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.STRING);
		ElementValues.updateWithRawValueString(prop, STRING_VALUE_JSON);
		Assert.assertEquals("abc", prop.getValue());
	}
	
	private static final String INTEGER_VALUE_JSON = "{\"@type\":\"mdt:value:integer\",\"value\":11}";
	private static final String INTEGER_NULL_VALUE_JSON = "{\"@type\":\"mdt:value:integer\",\"value\":null}";
	private static final String INTEGER_VALUE_SERIALIZATION = "11";
	@Test
	public void testIntegerPropertyValue() throws IOException {
		IntegerPropertyValue value = PropertyValue.INTEGER(11);
		
		Assert.assertEquals(INTEGER_VALUE_JSON, value.toJsonString());
		Assert.assertEquals(INTEGER_VALUE_SERIALIZATION, value.toValueJsonString());
		
		IntegerPropertyValue nullValue = PropertyValue.INTEGER(null);
		Assert.assertEquals(INTEGER_NULL_VALUE_JSON, nullValue.toJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(INTEGER_VALUE_JSON);
		Assert.assertTrue(ev instanceof IntegerPropertyValue);
		IntegerPropertyValue parsedValue = (IntegerPropertyValue)ev;
		Assert.assertEquals(Integer.valueOf(11), parsedValue.get());
		
		ev = ElementValues.parseJsonString(INTEGER_NULL_VALUE_JSON);
		Assert.assertTrue(ev instanceof IntegerPropertyValue);
		parsedValue = (IntegerPropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.INT);
		ElementValues.updateWithRawValueString(prop, INTEGER_VALUE_SERIALIZATION);
		Assert.assertEquals("11", prop.getValue());
	}
	
	private static final String DOUBLE_VALUE_JSON = "{\"@type\":\"mdt:value:double\",\"value\":0.234}";
	private static final String DOUBLE_NULL_VALUE_JSON = "{\"@type\":\"mdt:value:double\",\"value\":null}";
	private static final String DOUBLE_VALUE_SERIALIZATION = "0.234";
	@Test
	public void testDoublePropertyValue() throws IOException {
		DoublePropertyValue value = PropertyValue.DOUBLE(0.234);
		
		Assert.assertEquals(DOUBLE_VALUE_JSON, value.toJsonString());
		Assert.assertEquals(DOUBLE_VALUE_SERIALIZATION, value.toValueJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(DOUBLE_VALUE_JSON);
		Assert.assertTrue(ev instanceof DoublePropertyValue);
		DoublePropertyValue parsedValue = (DoublePropertyValue)ev;
		Assert.assertEquals(Double.valueOf(0.234), parsedValue.get());
		
		ev = ElementValues.parseJsonString(DOUBLE_NULL_VALUE_JSON);
		Assert.assertTrue(ev instanceof DoublePropertyValue);
		parsedValue = (DoublePropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		DoublePropertyValue nullValue = PropertyValue.DOUBLE(null);
		Assert.assertEquals(DOUBLE_NULL_VALUE_JSON, nullValue.toJsonString());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.DOUBLE);
		ElementValues.updateWithRawValueString(prop, DOUBLE_VALUE_SERIALIZATION);
		Assert.assertEquals("0.234", prop.getValue());
	}
	
	private static final String FLOAT_VALUE_JSON = "{\"@type\":\"mdt:value:float\",\"value\":12.5}";
	private static final String FLOAT_NULL_VALUE_JSON = "{\"@type\":\"mdt:value:float\",\"value\":null}";
	private static final String FLOAT_VALUE_SERIALIZATION = "12.5";
	@Test
	public void testFloatPropertyValue() throws IOException {
		FloatPropertyValue value = PropertyValue.FLOAT(12.5f);
		
		Assert.assertEquals(FLOAT_VALUE_JSON, value.toJsonString());
		Assert.assertEquals(FLOAT_VALUE_SERIALIZATION, value.toValueJsonString());
		
		FloatPropertyValue nullValue = PropertyValue.FLOAT(null);
		Assert.assertEquals(FLOAT_NULL_VALUE_JSON, nullValue.toJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(FLOAT_VALUE_JSON);
		Assert.assertTrue(ev instanceof FloatPropertyValue);
		FloatPropertyValue parsedValue = (FloatPropertyValue)ev;
		Assert.assertEquals(Float.valueOf(12.5f), parsedValue.get());
		
		ev = ElementValues.parseJsonString(FLOAT_NULL_VALUE_JSON);
		Assert.assertTrue(ev instanceof FloatPropertyValue);
		parsedValue = (FloatPropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.FLOAT);
		ElementValues.updateWithRawValueString(prop, FLOAT_VALUE_SERIALIZATION);
		Assert.assertEquals("12.5", prop.getValue());
	}
	
	private static final String BOOLEAN_VALUE_JSON = "{\"@type\":\"mdt:value:boolean\",\"value\":true}";
	private static final String BOOLEAN_NULL_VALUE_JSON = "{\"@type\":\"mdt:value:boolean\",\"value\":null}";
	private static final String BOOLEAN_VALUE_SERIALIZATION = "true";
	@Test
	public void testBooleanPropertyValue() throws IOException {
		BooleanPropertyValue value = PropertyValue.BOOLEAN(true);
		
		Assert.assertEquals(BOOLEAN_VALUE_JSON, value.toJsonString());
		Assert.assertEquals(BOOLEAN_VALUE_SERIALIZATION, value.toValueJsonString());
		
		BooleanPropertyValue nullValue = PropertyValue.BOOLEAN(null);
		Assert.assertEquals(BOOLEAN_NULL_VALUE_JSON, nullValue.toJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(BOOLEAN_VALUE_JSON);
		Assert.assertTrue(ev instanceof BooleanPropertyValue);
		BooleanPropertyValue parsedValue = (BooleanPropertyValue)ev;
		Assert.assertEquals(Boolean.valueOf(true), parsedValue.get());
		
		ev = ElementValues.parseJsonString(BOOLEAN_NULL_VALUE_JSON);
		Assert.assertTrue(ev instanceof BooleanPropertyValue);
		parsedValue = (BooleanPropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.BOOLEAN);
		ElementValues.updateWithRawValueString(prop, BOOLEAN_VALUE_SERIALIZATION);
		Assert.assertEquals("true", prop.getValue());
	}
	
	private static final String DATE_TIME_JSON = "{\"@type\":\"mdt:value:dateTime\",\"value\":\"2023-10-01T12:00:00\"}";
	private static final String DATE_TIME_NULL_JSON = "{\"@type\":\"mdt:value:dateTime\",\"value\":null}";
	private static final String DATE_TIME_VALUE_JSON = "\"2023-10-01T12:00:00\"";
	private static final String DATE_TIME_NULL_VALUE_JSON = "null";
	@Test
	public void testDateTimePropertyValue() throws IOException {
		Instant dt = DataTypes.DATE_TIME.parseValueString("2023-10-01T12:00:00");
		DateTimePropertyValue value = PropertyValue.DATE_TIME(dt);
		
		Assert.assertEquals(DATE_TIME_JSON, value.toJsonString());
		Assert.assertEquals(DATE_TIME_VALUE_JSON, value.toValueJsonString());
		
		value = PropertyValue.DATE_TIME(null);
		Assert.assertEquals(DATE_TIME_NULL_JSON, value.toJsonString());
		Assert.assertEquals(DATE_TIME_NULL_VALUE_JSON, value.toValueJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(DATE_TIME_JSON);
		Assert.assertTrue(ev instanceof DateTimePropertyValue);
		DateTimePropertyValue parsedValue = (DateTimePropertyValue)ev;
		Assert.assertEquals(dt, parsedValue.get());
		
		ev = ElementValues.parseJsonString(DATE_TIME_NULL_JSON);
		Assert.assertTrue(ev instanceof DateTimePropertyValue);
		parsedValue = (DateTimePropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.DATE_TIME);
		ElementValues.updateWithRawValueString(prop, DATE_TIME_VALUE_JSON);
		Assert.assertEquals("2023-10-01T12:00:00", prop.getValue());
	}
	
	private static final String DURATION_VALUE_JSON = "{\"@type\":\"mdt:value:duration\",\"value\":\"PT11S\"}";
	private static final String DURATION_NULL_VALUE_JSON = "{\"@type\":\"mdt:value:duration\",\"value\":null}";
	private static final String DURATION_VALUE_SERIALIZATION = "\"PT11S\"";
	private static final String DURATION_NULL_VALUE_SERIALIZATION = "null";
	@Test
	public void testDurationPropertyValue() throws IOException {
		DurationPropertyValue value = PropertyValue.DURATION(Duration.ofSeconds(11));
		
		Assert.assertEquals(DURATION_VALUE_JSON, value.toJsonString());
		Assert.assertEquals(DURATION_VALUE_SERIALIZATION, value.toValueJsonString());
		
		value = PropertyValue.DURATION(null);
		Assert.assertEquals(DURATION_NULL_VALUE_JSON, value.toJsonString());
		Assert.assertEquals(DURATION_NULL_VALUE_SERIALIZATION, value.toValueJsonString());
		
		ElementValue ev = ElementValues.parseJsonString(DURATION_VALUE_JSON);
		Assert.assertTrue(ev instanceof DurationPropertyValue);
		DurationPropertyValue parsedValue = (DurationPropertyValue)ev;
		Assert.assertEquals(Duration.ofSeconds(11), parsedValue.get());
		
		ev = ElementValues.parseJsonString(DURATION_NULL_VALUE_JSON);
		Assert.assertTrue(ev instanceof DurationPropertyValue);
		parsedValue = (DurationPropertyValue)ev;
		Assert.assertEquals(null, parsedValue.get());
		
		DefaultProperty prop = new DefaultProperty();
		prop.setValueType(DataTypeDefXsd.DURATION);
		ElementValues.updateWithRawValueString(prop, DURATION_VALUE_SERIALIZATION);
		Assert.assertEquals("PT11S", prop.getValue());
	}
}
