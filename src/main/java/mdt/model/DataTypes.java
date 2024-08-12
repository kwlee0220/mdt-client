package mdt.model;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.google.common.collect.Maps;

import lombok.experimental.UtilityClass;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class DataTypes {
	public static StringType STRING = new StringType();
	public static BooleanType BOOLEAN = new BooleanType();
	public static ShortType SHORT = new ShortType();
	public static IntegerType INTEGER = new IntegerType();
	public static LongType LONG = new LongType();
	public static FloatType FLOAT = new FloatType();
	public static DoubleType DOUBLE = new DoubleType();
	public static DateTimeType DATE_TIME = new DateTimeType();
	public static DateType DATE = new DateType();
	public static TimeType TIME = new TimeType();
	public static DurationType DURATION = new DurationType();
	
	public static DataType<?> fromAas4jDatatype(DataTypeDefXsd type) {
		return _XSD_TO_TYPES.get(type);
	}
	
	public static DataType<?> fromDataTypeName(String name) {
		return _NAME_TO_TYPES.get(name.toUpperCase());
	}
	
	private static final DataType<?>[] _TYPES = {
		STRING, BOOLEAN, SHORT, INTEGER, LONG, FLOAT, DOUBLE,
		DATE_TIME, DATE, TIME, DURATION,
	};
	private static final Map<String,DataType<?>> _NAME_TO_TYPES = Maps.newHashMap();
	private static final Map<DataTypeDefXsd,DataType<?>> _XSD_TO_TYPES = Maps.newHashMap();
	static {
		for ( DataType<?> dtype: _TYPES ) {
			_XSD_TO_TYPES.put(dtype.getTypeDefXsd(), dtype);
			_NAME_TO_TYPES.put(dtype.getName(), dtype);
			_NAME_TO_TYPES.put(dtype.getId(), dtype);
		}
	}
	public static final List<DataType<?>> TYPES = Arrays.asList(_TYPES);
	public static final Map<String,DataType<?>> NAME_TO_TYPES = Collections.unmodifiableMap(_NAME_TO_TYPES);
	
	public static class BooleanType extends AbstractDataType<Boolean> implements DataType<Boolean> {
		private BooleanType() {
			super("xs:boolean", DataTypeDefXsd.BOOLEAN, Boolean.class);
		}
	
		@Override
		public String toValueString(Boolean value) {
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Boolean parseValueString(String str) {
			return (str != null) ? Boolean.parseBoolean(str) : null;
		}
	}

	public static class DateTimeType extends AbstractDataType<Instant> implements DataType<Instant> {
		private DateTimeType() {
			super("xs:dateTime", DataTypeDefXsd.DATE_TIME, Instant.class);
		}
	
		@Override
		public String toValueString(Instant value) {
			return (value != null) ? value.toString() : null;
		}
	
		@Override
		public Instant parseValueString(String str) {
			return (str != null) ? Instant.parse(str) : null;
		}
	}

	public static class DateType extends AbstractDataType<Date> implements DataType<Date> {
		private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
		
		private DateType() {
			super("xs:date", DataTypeDefXsd.DATE, Date.class);
		}
	
		@Override
		public String toValueString(Date value) {
			return (value != null) ? DATE_FORMATTER.format(value) : null;
		}
	
		@Override
		public Date parseValueString(String str) {
			try {
				return (str != null) ? DATE_FORMATTER.parse(str) : null;
			}
			catch ( ParseException e ) {
				throw new IllegalArgumentException("Invalid string (not xs:date string): " + str);
			}
		}
	}

	public static class DoubleType extends AbstractDataType<Double> implements DataType<Double> {
		private DoubleType() {
			super("xs:double", DataTypeDefXsd.DOUBLE, Double.class);
		}
	
		@Override
		public String toValueString(Double value) {
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Double parseValueString(String str) {
			return (str != null) ? Double.parseDouble(str) : null;
		}
	}

	public static class DurationType extends AbstractDataType<Duration> implements DataType<Duration> {
		private DurationType() {
			super("xs:duration", DataTypeDefXsd.DURATION, Duration.class);
		}
	
		public String toString(Duration value) {
			return (value != null) ? value.toString() : null;
		}
	
		@Override
		public String toValueString(Duration value) {
			return (value != null) ? value.toString() : "";
		}
	
		@Override
		public Duration parseValueString(String str) {
			return (str != null && str.trim().length() > 0) ? Duration.parse(str) : null;
		}
	}

	public static class FloatType extends AbstractDataType<Float> implements DataType<Float> {
		private FloatType() {
			super("xs:float", DataTypeDefXsd.FLOAT, Float.class);
		}
	
		@Override
		public String toValueString(Float value) {
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Float parseValueString(String str) {
			return (str != null) ? Float.parseFloat(str) : null;
		}
	}

	public static class IntegerType extends AbstractDataType<Integer> implements DataType<Integer> {
		private IntegerType() {
			super("xs:integer", DataTypeDefXsd.INTEGER, Integer.class);
		}
	
		@Override
		public String toValueString(Integer value) {
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Integer parseValueString(String str) {
			return (str != null) ? Integer.parseInt(str) : null;
		}
	}

	public static class LongType extends AbstractDataType<Long> implements DataType<Long> {
		private LongType() {
			super("xs:long", DataTypeDefXsd.LONG, Long.class);
		}
	
		@Override
		public String toValueString(Long value) {
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Long parseValueString(String str) {
			return (str != null) ? Long.parseLong(str) : null;
		}
	}

	public static class ShortType extends AbstractDataType<Short> implements DataType<Short> {
		private ShortType() {
			super("xs:short", DataTypeDefXsd.SHORT, Short.class);
		}
	
		@Override
		public String toValueString(Short value) {
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Short parseValueString(String str) {
			return (str != null) ? Short.parseShort(str) : null;
		}
	}

	public static class StringType extends AbstractDataType<String> implements DataType<String> {
		private StringType() {
			super("xs:string", DataTypeDefXsd.STRING, String.class);
		}
	
		@Override
		public String toValueString(String value) {
			return value;
		}
	
		@Override
		public String parseValueString(String str) {
			return str;
		}
	}

	public static class TimeType extends AbstractDataType<Time> implements DataType<Time> {
		private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("hh-mm-ss");
		
		private TimeType() {
			super("xs:duration", DataTypeDefXsd.DURATION, Time.class);
		}
	
		@Override
		public String toValueString(Time value) {
			return (value != null) ? TIME_FORMATTER.format(value) : null;
		}
	
		@Override
		public Time parseValueString(String str) {
			try {
				return (str != null) ? new Time(TIME_FORMATTER.parse(str).getTime()) : null;
			}
			catch ( ParseException e ) {
				throw new IllegalArgumentException("Invalid string (not xs:time string): " + str);
			}
		}
	}
}
