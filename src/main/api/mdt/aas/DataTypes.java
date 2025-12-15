package mdt.aas;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import lombok.experimental.UtilityClass;

import utils.DataUtils;
import utils.Tuple;
import utils.Utilities;
import utils.func.Try;

import mdt.model.ModelGenerationException;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class DataTypes {
	public static StringType STRING = new StringType();
	public static BooleanType BOOLEAN = new BooleanType();
	public static ShortType SHORT = new ShortType();
	public static IntType INT = new IntType();
	public static LongType LONG = new LongType();
	public static FloatType FLOAT = new FloatType();
	public static DoubleType DOUBLE = new DoubleType();
	public static DateTimeType DATE_TIME = new DateTimeType();
	public static DateType DATE = new DateType();
	public static TimeType TIME = new TimeType();
	public static DurationType DURATION = new DurationType();
	public static DecimalType DECIMAL = new DecimalType();
	
	public static DataType<?> fromAas4jDatatype(DataTypeDefXsd type) {
		return _XSD_TO_TYPES.get(type);
	}
	
	public static DataType<?> fromDataTypeName(String name) {
		DataType<?> dtype = _NAME_TO_TYPES.get(name.toUpperCase());
		if ( dtype == null ) {
			throw new ModelGenerationException("unknown DataType name=" + name);
		}
		else {
			return dtype;
		}
	}
	
	public static DataType<?> fromJavaClass(Class<?> cls) {
		return _CLSSS_TO_TYPES.get(cls);
	}
	
	private static final DataType<?>[] _TYPES = {
		STRING, BOOLEAN, SHORT, INT, LONG, FLOAT, DOUBLE,
		DATE_TIME, DATE, TIME, DURATION, DECIMAL,
	};
	private static final Map<String,DataType<?>> _NAME_TO_TYPES = Maps.newHashMap();
	private static final Map<DataTypeDefXsd,DataType<?>> _XSD_TO_TYPES = Maps.newHashMap();
	private static final Map<Class<?>,DataType<?>> _CLSSS_TO_TYPES = Maps.newHashMap();
	static {
		for ( DataType<?> dtype: _TYPES ) {
			_XSD_TO_TYPES.put(dtype.getTypeDefXsd(), dtype);
			_NAME_TO_TYPES.put(dtype.getName(), dtype);
			_NAME_TO_TYPES.put(dtype.getId(), dtype);
			
			_CLSSS_TO_TYPES.put(dtype.getJavaClass(), dtype);
		}
	}
	public static final List<DataType<?>> TYPES = Arrays.asList(_TYPES);
	public static final Map<String,DataType<?>> NAME_TO_TYPES = Collections.unmodifiableMap(_NAME_TO_TYPES);
	
	public static class BooleanType extends AbstractDataType<Boolean> implements DataType<Boolean> {
		private BooleanType() {
			super("xs:boolean", DataTypeDefXsd.BOOLEAN, Boolean.class);
		}
	
		@Override
		public String toValueString(Object value) {
			Boolean flag = DataUtils.asBoolean(value);
			return (flag != null) ? ""+flag : null;
		}
	
		@Override
		public Boolean parseValueString(String str) {
			return (str != null) ? Boolean.parseBoolean(str) : null;
		}

		@Override
		public Boolean toJdbcObject(Boolean value) {
			return value;
		}

		@Override
		public Boolean fromJdbcObject(Object jdbcObj) {
			return (Boolean)jdbcObj;
		}

		@Override
		public Object toJsonObject(Boolean value) {
			return value;
		}

		@Override
		public Boolean fromJsonNode(JsonNode jnode) {
			return (jnode == null || jnode.isNull()) ? null : jnode.asBoolean();
		}
	}

	public static class DateTimeType extends AbstractDataType<Instant> implements DataType<Instant> {
		private static final DateTimeFormatter DT_FORMATTER1 = DateTimeFormatter.ISO_LOCAL_DATE_TIME
																			.withZone(ZoneOffset.systemDefault());
		
		private DateTimeType() {
			super("xs:dateTime", DataTypeDefXsd.DATE_TIME, Instant.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			if ( obj == null ) {
				return null;
			}
			
			LocalDateTime ldt = DataUtils.asDatetime(obj);
			String ldtStr = ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			if ( ldtStr.indexOf('.') >= 0 ) {
				Tuple<String,String> tup = Utilities.splitLast(ldtStr, ':');
				
				int end = Math.min(tup._2.indexOf('.') + 4, tup._2.length());
				String formatted = tup._2.substring(0, end);
				return String.format("%s:%s", tup._1, formatted);
			}
			else {
				return ldtStr;
			}
		}
	
		@Override
		public Instant parseValueString(String str) {
			if ( str == null || str.trim().length() == 0 ) {
				return null;
			}
			return Try.get(() -> Instant.parse(str))
						.recover(() -> ZonedDateTime.parse(str, DT_FORMATTER1).toInstant())
						.get();
		}

		@Override
		public Timestamp toJdbcObject(Instant value) {
			return (value != null) ? Timestamp.from(value) : null;
		}

		@Override
		public Instant fromJdbcObject(Object jdbcObj) {
			return DataUtils.asInstant(jdbcObj);
		}
	}

	public static class DateType extends AbstractDataType<Date> implements DataType<Date> {
		private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
		
		private DateType() {
			super("xs:date", DataTypeDefXsd.DATE, Date.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Date value = DataUtils.asDate(obj);
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

		@Override
		public java.sql.Date toJdbcObject(Date value) {
			return (value != null) ? new java.sql.Date(value.getTime()) : null;
		}

		@Override
		public Date fromJdbcObject(Object jdbcObj) {
			return new Date(((java.sql.Date)jdbcObj).getTime());
		}
	}

	public static class DoubleType extends AbstractDataType<Double> implements DataType<Double> {
		private DoubleType() {
			super("xs:double", DataTypeDefXsd.DOUBLE, Double.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Double value = DataUtils.asDouble(obj);
			return (value != null) ? ""+obj : null;
		}
	
		@Override
		public Double parseValueString(String str) {
			return (str != null) ? Double.parseDouble(str) : null;
		}

		@Override
		public Double toJdbcObject(Double value) {
			return value;
		}

		@Override
		public Double fromJdbcObject(Object jdbcObj) {
			return (Double)jdbcObj;
		}

		@Override
		public Object toJsonObject(Double value) {
			return value;
		}

		@Override
		public Double fromJsonNode(JsonNode jnode) {
			return (jnode == null || jnode.isNull()) ? null : jnode.asDouble();
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
		public String toValueString(Object value) {
			return (value != null) ? ((Duration)value).toString() : null;
		}
	
		@Override
		public Duration parseValueString(String str) {
			return (str != null && str.trim().length() > 0) ? Duration.parse(str) : null;
		}

		@Override
		public Long toJdbcObject(Duration value) {
			return (value != null) ? value.toMillis() : null;
		}

		@Override
		public Duration fromJdbcObject(Object jdbcObj) {
			if ( jdbcObj == null ) {
				return null;
			}
			if ( jdbcObj instanceof Long longv ) {
				return Duration.ofMillis(longv);
			}
			else if ( jdbcObj instanceof Double dbv ) {
				long millis = Math.round(dbv.doubleValue() * 1000);
				return Duration.ofMillis(millis);
			}
			else if ( jdbcObj instanceof Float fltv ) {
				long millis = Math.round(fltv.floatValue() * 1000);
				return Duration.ofMillis(millis);
			}
			else {
				throw new IllegalArgumentException("Invalid Duration value: " + jdbcObj);
			}
		}
	}

	public static class FloatType extends AbstractDataType<Float> implements DataType<Float> {
		private FloatType() {
			super("xs:float", DataTypeDefXsd.FLOAT, Float.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Float value = DataUtils.asFloat(obj);
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Float parseValueString(String str) {
			return (str != null) ? Float.parseFloat(str) : null;
		}

		@Override
		public Float toJdbcObject(Float value) {
			return value;
		}

		@Override
		public Float fromJdbcObject(Object jdbcObj) {
			if ( jdbcObj instanceof Double ) {
				return ((Double) jdbcObj).floatValue();
			}
			else if ( jdbcObj instanceof Float ) {
				return (Float) jdbcObj;
			}
			else {
				throw new IllegalArgumentException("Invalid JDBC object type: " + jdbcObj.getClass());
			}
		}

		@Override
		public Object toJsonObject(Float value) {
			return value;
		}

		@Override
		public Float fromJsonNode(JsonNode jnode) {
			return (jnode == null || jnode.isNull()) ? null : (float)jnode.asDouble();
		}
	}

	public static class IntType extends AbstractDataType<Integer> implements DataType<Integer> {
		private IntType() {
			super("xs:int", DataTypeDefXsd.INT, Integer.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Integer value = DataUtils.asInt(obj);
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Integer parseValueString(String str) {
			return (str != null) ? Integer.parseInt(str) : null;
		}

		@Override
		public Integer toJdbcObject(Integer value) {
			return value;
		}

		@Override
		public Integer fromJdbcObject(Object jdbcObj) {
			if ( jdbcObj == null ) {
				return 0;
			}
			if ( jdbcObj instanceof Integer intv ) {
				return intv;
			}
			else if ( jdbcObj instanceof Long longv ) {
				return longv.intValue();
			}
			else {
				return Integer.parseInt(jdbcObj.toString());
			}
		}

		@Override
		public Object toJsonObject(Integer value) {
			return value;
		}

		@Override
		public Integer fromJsonNode(JsonNode jnode) {
			return (jnode == null || jnode.isNull()) ? null : jnode.asInt();
		}
	}

	public static class LongType extends AbstractDataType<Long> implements DataType<Long> {
		private LongType() {
			super("xs:long", DataTypeDefXsd.LONG, Long.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Long value = DataUtils.asLong(obj);
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Long parseValueString(String str) {
			return (str != null) ? Long.parseLong(str) : null;
		}

		@Override
		public Long toJdbcObject(Long value) {
			return value;
		}

		@Override
		public Long fromJdbcObject(Object jdbcObj) {
			return (Long)jdbcObj;
		}

		@Override
		public Object toJsonObject(Long value) {
			return value;
		}

		@Override
		public Long fromJsonNode(JsonNode jnode) {
			return (jnode == null || jnode.isNull()) ? null : jnode.asLong();
		}
	}

	public static class ShortType extends AbstractDataType<Short> implements DataType<Short> {
		private ShortType() {
			super("xs:short", DataTypeDefXsd.SHORT, Short.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Short value = DataUtils.asShort(obj);
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public Short parseValueString(String str) {
			return (str != null) ? Short.parseShort(str) : null;
		}

		@Override
		public Short toJdbcObject(Short value) {
			return value;
		}

		@Override
		public Short fromJdbcObject(Object jdbcObj) {
			return (Short)jdbcObj;
		}

		@Override
		public Object toJsonObject(Short value) {
			return value;
		}

		@Override
		public Short fromJsonNode(JsonNode jnode) {
			return (jnode == null || jnode.isNull()) ? null : (short)jnode.asInt();
		}
	}

	public static class StringType extends AbstractDataType<String> implements DataType<String> {
		private StringType() {
			super("xs:string", DataTypeDefXsd.STRING, String.class);
		}
	
		@Override
		public String toValueString(Object value) {
			return (value != null) ? value.toString() : null;
		}
	
		@Override
		public String parseValueString(String str) {
			return str;
		}

		@Override
		public String toJdbcObject(String value) {
			return value;
		}

		@Override
		public String fromJdbcObject(Object jdbcObj) {
			return (String)jdbcObj;
		}

		@Override
		public Object toJsonObject(String value) {
			return value;
		}

		@Override
		public String fromJsonNode(JsonNode jnode) {
			if ( jnode == null || jnode.isNull() ) {
				return null;
			}
			else {
				return jnode.isTextual() ? jnode.asText() : jnode.toString();
			}
		}
	}

	public static class TimeType extends AbstractDataType<LocalTime> implements DataType<LocalTime> {
		private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh-mm-ss");
		
		private TimeType() {
			super("xs:time", DataTypeDefXsd.TIME, LocalTime.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			LocalTime value = (LocalTime)obj;
			return (value != null) ? TIME_FORMATTER.format(value) : null;
		}
	
		@Override
		public LocalTime parseValueString(String str) {
			try {
				return (str != null) ? LocalTime.parse(str, TIME_FORMATTER) : null;
			}
			catch ( DateTimeParseException e ) {
				throw new IllegalArgumentException("Invalid string (not xs:time string): " + str);
			}
		}

		@Override
		public java.sql.Time toJdbcObject(LocalTime value) {
			return (value != null) ? Time.valueOf(value) : null;
		}

		@Override
		public LocalTime fromJdbcObject(Object jdbcObj) {
			return ((java.sql.Time)jdbcObj).toLocalTime();
		}
	}

	public static class DecimalType extends AbstractDataType<BigDecimal> implements DataType<BigDecimal> {
		private DecimalType() {
			super("xs:decimal", DataTypeDefXsd.DECIMAL, BigDecimal.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Double value = DataUtils.asDouble(obj);
			return (value != null) ? ""+value : null;
		}
	
		@Override
		public BigDecimal parseValueString(String str) {
			return (str != null) ? BigDecimalParser.parse(str) : null;
		}

		@Override
		public BigDecimal toJdbcObject(BigDecimal value) {
			return value;
		}

		@Override
		public BigDecimal fromJdbcObject(Object jdbcObj) {
			return (BigDecimal)jdbcObj;
		}
	}
}
