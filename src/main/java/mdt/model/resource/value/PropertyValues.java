package mdt.model.resource.value;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import lombok.experimental.UtilityClass;
import mdt.model.DataType;
import mdt.model.DataTypes;
import mdt.model.DataTypes.BooleanType;
import mdt.model.DataTypes.DateTimeType;
import mdt.model.DataTypes.DateType;
import mdt.model.DataTypes.DoubleType;
import mdt.model.DataTypes.DurationType;
import mdt.model.DataTypes.FloatType;
import mdt.model.DataTypes.IntType;
import mdt.model.DataTypes.LongType;
import mdt.model.DataTypes.ShortType;
import mdt.model.DataTypes.StringType;
import utils.DataUtils;
import utils.func.FOption;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class PropertyValues {
	@SuppressWarnings("unused")
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	
	public static PropertyValue<?> fromDataType(DataType<?> dtype, Object initValue) {
		if ( dtype instanceof StringType ) {
			return new StringValue(FOption.map(initValue, v -> "" + v));
		}
		else if ( dtype instanceof IntType ) {
			return new IntegerValue(DataUtils.asInt(initValue));
		}
		else if ( dtype instanceof DurationType ) {
			if ( initValue instanceof String ) {
				return new DurationValue(Duration.parse((String)initValue));
			}
			else {
				return new DurationValue((Duration)initValue);
			}
		}
		else if ( dtype instanceof BooleanType ) {
			return new BooleanValue(DataUtils.asBoolean(initValue));
		}
		else if ( dtype instanceof FloatType ) {
			return new FloatValue(DataUtils.asFloat(initValue));
		}
		else if ( dtype instanceof DoubleType ) {
			return new DoubleValue(DataUtils.asDouble(initValue));
		}
		else if ( dtype instanceof DateTimeType ) {
			return new DateTimeValue(DataUtils.asInstant(initValue));
		}
		else if ( dtype instanceof DateType ) {
			return new DateValue((Date)initValue);
		}
		else if ( dtype instanceof LongType ) {
			return new LongValue(DataUtils.asLong(initValue));
		}
		else if ( dtype instanceof ShortType ) {
			return new ShortValue(DataUtils.asShort(initValue));
		}
		else {
			throw new IllegalArgumentException("Unexpected PropertyValue type: " + dtype);
		}
	}
	
	public static PropertyValue<?> fromValue(Object value) {
		if ( value instanceof String str ) {
			return new StringValue(str);
		}
		else if ( value instanceof Integer iv ) {
			return new IntegerValue(iv);
		}
		else if ( value instanceof Duration duv ) {
			return new DurationValue(duv);
		}
		else if ( value instanceof Boolean bv ) {
			return new BooleanValue(bv);
		}
		else if ( value instanceof Float fv ) {
			return new FloatValue(fv);
		}
		else if ( value instanceof Double dv ) {
			return new DoubleValue(dv);
		}
		else if ( value instanceof Instant tv ) {
			return new DateTimeValue(tv);
		}
		else if ( value instanceof Date dtv ) {
			return new DateValue(dtv);
		}
		else if ( value instanceof Long lv ) {
			return new LongValue(lv);
		}
		else if ( value instanceof Short lv ) {
			return new ShortValue(lv);
		}
		else if ( value instanceof BigDecimal bd ) {
			return new DecimalValue(bd);
		}
		else {
			throw new IllegalArgumentException("Unexpected PropertyValue: " + value);
		}
	}
	
	public static class StringValue extends PropertyValue<String> {
		public StringValue(String value) {
			super(DataTypes.STRING, value);
		}
	}
	public static class IntegerValue extends PropertyValue<Integer> {
		public IntegerValue(int value) {
			super(DataTypes.INT, value);
		}
	}
	public static class FloatValue extends PropertyValue<Float> {
		public FloatValue(float value) {
			super(DataTypes.FLOAT, value);
		}
	}
	public static class DoubleValue extends PropertyValue<Double> {
		public DoubleValue(double value) {
			super(DataTypes.DOUBLE, value);
		}
	}
	public static class DateTimeValue extends PropertyValue<Instant> {
		public DateTimeValue(Instant value) {
			super(DataTypes.DATE_TIME, value);
		}
	}
	public static class DateValue extends PropertyValue<Date> {
		public DateValue(Date value) {
			super(DataTypes.DATE, value);
		}
	}
	public static class DurationValue extends PropertyValue<Duration> {
		public DurationValue(Duration value) {
			super(DataTypes.DURATION, value);
		}
	}
	public static class BooleanValue extends PropertyValue<Boolean> {
		public BooleanValue(boolean value) {
			super(DataTypes.BOOLEAN, value);
		}
	}
	public static class LongValue extends PropertyValue<Long> {
		public LongValue(long value) {
			super(DataTypes.LONG, value);
		}
	}
	public static class ShortValue extends PropertyValue<Short> {
		public ShortValue(short value) {
			super(DataTypes.SHORT, value);
		}
	}
	public static class DecimalValue extends PropertyValue<BigDecimal> {
		public DecimalValue(BigDecimal value) {
			super(DataTypes.DECIMAL, value);
		}
	}
}
