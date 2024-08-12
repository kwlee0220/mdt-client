package mdt.model.resource.value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import utils.func.FOption;

import lombok.experimental.UtilityClass;
import mdt.model.DataType;
import mdt.model.DataTypes;
import mdt.model.DataTypes.BooleanType;
import mdt.model.DataTypes.DoubleType;
import mdt.model.DataTypes.DurationType;
import mdt.model.DataTypes.FloatType;
import mdt.model.DataTypes.IntegerType;
import mdt.model.DataTypes.StringType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class PropertyValues {
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	
	public static PropertyValue<?> fromDataType(DataType<?> dtype, Object initValue) {
		if ( dtype instanceof StringType ) {
			return new StringValue(FOption.map(initValue, v -> "" + v));
		}
		else if ( dtype instanceof IntegerType ) {
			return new IntegerValue((Integer)initValue);
		}
		else if ( dtype instanceof DurationType ) {
			return new DurationValue((Duration)initValue);
		}
		else if ( dtype instanceof BooleanType ) {
			return new BooleanValue((Boolean)initValue);
		}
		else if ( dtype instanceof FloatType ) {
			return new FloatValue((Float)initValue);
		}
		else if ( dtype instanceof DoubleType ) {
			return new DoubleValue((Double)initValue);
		}
		else if ( dtype instanceof DateTimeValue ) {
			return new DateTimeValue((Instant)initValue);
		}
		else if ( dtype instanceof DateValue ) {
			return new DateValue((Date)initValue);
		}
		else if ( dtype instanceof LongValue ) {
			return new LongValue((Long)initValue);
		}
		else if ( dtype instanceof ShortValue ) {
			return new ShortValue((Short)initValue);
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
		else {
			throw new IllegalArgumentException("Unexpected PropertyValue: " + value);
		}
	}
	
	public static class StringValue extends PropertyValue<String> {
		public StringValue(String value) {
			super(DataTypes.STRING, value);
		}

		@Override
		public void setString(String str) {
			set(str);
		}
	}
	public static class IntegerValue extends PropertyValue<Integer> {
		public IntegerValue(int value) {
			super(DataTypes.INTEGER, value);
		}

		@Override
		public void setString(String str) {
			set(Integer.parseInt(str));
		}
	}
	public static class FloatValue extends PropertyValue<Float> {
		public FloatValue(float value) {
			super(DataTypes.FLOAT, value);
		}

		@Override
		public void setString(String str) {
			set(Float.parseFloat(str));
		}
	}
	public static class DoubleValue extends PropertyValue<Double> {
		public DoubleValue(double value) {
			super(DataTypes.DOUBLE, value);
		}

		@Override
		public void setString(String str) {
			set(Double.parseDouble(str));
		}
	}
	public static class DateTimeValue extends PropertyValue<Instant> {
		public DateTimeValue(Instant value) {
			super(DataTypes.DATE_TIME, value);
		}

		@Override
		public void setString(String str) {
			set(Instant.parse(str));
		}
	}
	public static class DateValue extends PropertyValue<Date> {
		public DateValue(Date value) {
			super(DataTypes.DATE, value);
		}

		@Override
		public void setString(String str) {
			try {
				set(DATE_FORMATTER.parse(str));
			}
			catch ( ParseException e ) {
				throw new IllegalArgumentException("invalid Date string: " + str);
			}
		}
	}
	public static class DurationValue extends PropertyValue<Duration> {
		public DurationValue(Duration value) {
			super(DataTypes.DURATION, value);
		}

		@Override
		public void setString(String str) {
			set(Duration.parse(str));
		}
	}
	public static class BooleanValue extends PropertyValue<Boolean> {
		public BooleanValue(boolean value) {
			super(DataTypes.BOOLEAN, value);
		}

		@Override
		public void setString(String str) {
			set(Boolean.parseBoolean(str));
		}
	}
	public static class LongValue extends PropertyValue<Long> {
		public LongValue(long value) {
			super(DataTypes.LONG, value);
		}

		@Override
		public void setString(String str) {
			set(Long.parseLong(str));
		}
	}
	public static class ShortValue extends PropertyValue<Short> {
		public ShortValue(short value) {
			super(DataTypes.SHORT, value);
		}

		@Override
		public void setString(String str) {
			set(Short.parseShort(str));
		}
	}
}
