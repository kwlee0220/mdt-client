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
import java.util.regex.Pattern;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import utils.DataUtils;
import utils.Split;

import mdt.model.ModelGenerationException;


/**
 * AAS({@code xs:*}) 데이터 타입 레지스트리이자 정적 팩토리 모음이다.
 * <p>
 * AAS 모델에서 사용하는 XSD 데이터 타입({@code xs:string}, {@code xs:int}, {@code xs:dateTime} 등)을
 * 표현하는 {@link DataType} 싱글턴들을 상수 필드로 제공하고, 다양한 키(AAS4J {@link DataTypeDefXsd},
 * 타입 이름, Java 클래스, Java 객체)로부터 해당 {@link DataType}을 조회하는 정적 메소드를 제공한다.
 * <p>
 * 각 {@link DataType}은 값 문자열({@code xs:*} 표기) · JDBC 객체 · Json 노드 사이의 상호 변환을 담당한다.
 * 이 클래스는 인스턴스화할 수 없는 유틸리티 클래스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DataTypes {
	private DataTypes() {
		throw new AssertionError("Should not be called: class=" + DataTypes.class.getName());
	}

	/** {@code xs:string} 타입 싱글턴. */
	public static final StringType STRING = new StringType();
	/** {@code xs:boolean} 타입 싱글턴. */
	public static final BooleanType BOOLEAN = new BooleanType();
	/** {@code xs:short} 타입 싱글턴. */
	public static final ShortType SHORT = new ShortType();
	/** {@code xs:int} 타입 싱글턴. */
	public static final IntType INT = new IntType();
	/** {@code xs:long} 타입 싱글턴. */
	public static final LongType LONG = new LongType();
	/** {@code xs:float} 타입 싱글턴. */
	public static final FloatType FLOAT = new FloatType();
	/** {@code xs:double} 타입 싱글턴. */
	public static final DoubleType DOUBLE = new DoubleType();
	/** {@code xs:dateTime} 타입 싱글턴. */
	public static final DateTimeType DATE_TIME = new DateTimeType();
	/** {@code xs:date} 타입 싱글턴. */
	public static final DateType DATE = new DateType();
	/** {@code xs:time} 타입 싱글턴. */
	public static final TimeType TIME = new TimeType();
	/** {@code xs:duration} 타입 싱글턴. */
	public static final DurationType DURATION = new DurationType();
	/** {@code xs:decimal} 타입 싱글턴. */
	public static final DecimalType DECIMAL = new DecimalType();

	/**
	 * AAS4J의 {@link DataTypeDefXsd}에 대응하는 {@link DataType}을 반환한다.
	 *
	 * @param type	AAS4J XSD 데이터 타입.
	 * @return 대응하는 {@link DataType}. 등록되지 않은 타입이면 {@code null}.
	 */
	public static DataType<?> fromAas4jDatatype(DataTypeDefXsd type) {
		return _XSD_TO_TYPES.get(type);
	}

	/**
	 * 타입 이름으로부터 {@link DataType}을 반환한다.
	 * <p>
	 * 이름은 대소문자를 구분하지 않으며, {@code DataTypeDefXsd}의 이름(예: {@code "INT"})과
	 * {@code xs:} 식별자(예: {@code "xs:int"}) 모두를 키로 사용할 수 있다.
	 *
	 * @param name	데이터 타입 이름.
	 * @return 대응하는 {@link DataType}.
	 * @throws ModelGenerationException	이름에 해당하는 타입이 없는 경우.
	 */
	public static DataType<?> fromDataTypeName(String name) {
		DataType<?> dtype = _NAME_TO_TYPES.get(name.toUpperCase());
		if ( dtype == null ) {
			throw new ModelGenerationException("unknown DataType name=" + name);
		}
		else {
			return dtype;
		}
	}

	/**
	 * Java 클래스에 대응하는 {@link DataType}을 반환한다.
	 *
	 * @param cls	Java 클래스(예: {@code Integer.class}).
	 * @return 대응하는 {@link DataType}. 등록되지 않은 클래스이면 {@code null}.
	 */
	public static DataType<?> fromJavaClass(Class<?> cls) {
		return _CLSSS_TO_TYPES.get(cls);
	}

	/**
	 * Java 객체의 런타임 타입으로부터 대응하는 {@link DataType}을 추론하여 반환한다.
	 * <p>
	 * 주로 JDBC 조회 결과 객체의 데이터 타입을 판별하는 데 사용한다.
	 *
	 * @param jdbcObj	데이터 타입을 판별할 객체.
	 * @return 대응하는 {@link DataType}. {@code jdbcObj}가 {@code null}이면 {@code null}.
	 * @throws IllegalArgumentException	지원하지 않는 타입의 객체인 경우.
	 */
	public static DataType<?> fromJavaObject(Object jdbcObj) {
		return switch (jdbcObj) {
			case null -> null;
			case String s -> STRING;
			case Integer i -> INT;
			case Float f -> FLOAT;
			case Double d -> DOUBLE;
			case Timestamp t -> DATE_TIME;
			case Instant i -> DATE_TIME;
			case java.sql.Date d -> DATE;
			case java.sql.Time t -> TIME;
			case Duration d -> DURATION;
			case BigDecimal b -> DECIMAL;
			case Boolean b -> BOOLEAN;
			case Long l -> LONG;
			case Short s -> SHORT;
			default -> throw new IllegalArgumentException("Unsupported JDBC object: " + jdbcObj);
		};
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
	/** 지원하는 모든 {@link DataType}의 목록(불변). */
	public static final List<DataType<?>> TYPES = Arrays.asList(_TYPES);
	/** 타입 이름/{@code xs:} 식별자 → {@link DataType} 매핑(불변). */
	public static final Map<String,DataType<?>> NAME_TO_TYPES = Collections.unmodifiableMap(_NAME_TO_TYPES);
	
	/** {@code xs:boolean} ↔ {@link Boolean} 데이터 타입. */
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

	/** {@code xs:dateTime} ↔ {@link Instant} 데이터 타입. 타임존이 없는 문자열은 시스템 기본 타임존으로 해석한다. */
	public static class DateTimeType extends AbstractDataType<Instant> implements DataType<Instant> {
		private static final Pattern TZ_PATTERN = Pattern.compile(".*([Zz]|[+-]\\d{2}:\\d{2})(\\[.+])?$");

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
				Split split = Split.splitLast(ldtStr, ":");
				String tail = split.tail().get();
				
				int end = Math.min(tail.indexOf('.') + 4, tail.length());
				String formatted = tail.substring(0, end);
				return String.format("%s:%s", split.head(), formatted);
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
			
		    if (TZ_PATTERN.matcher(str).matches()) {
		        return ZonedDateTime.parse(str).toInstant();
		    }
		    else {
		    	LocalDateTime ldt = LocalDateTime.parse(str);
				return ldt.toInstant(ZoneOffset.systemDefault().getRules().getOffset(ldt));
		    }
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

	/** {@code xs:duration} ↔ {@link Duration} 데이터 타입. JDBC에는 밀리초 단위 {@code long}으로 저장한다. */
	public static class DurationType extends AbstractDataType<Duration> implements DataType<Duration> {
		private DurationType() {
			super("xs:duration", DataTypeDefXsd.DURATION, Duration.class);
		}
	
		/**
		 * {@link Duration} 값을 ISO-8601 표기 문자열로 변환한다.
		 *
		 * @param value	변환할 {@link Duration}. {@code null}이면 {@code null}을 반환한다.
		 * @return ISO-8601 표기 문자열(예: {@code "PT1H30M"}) 또는 {@code null}.
		 */
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

	/** {@code xs:date} ↔ {@link Date} 데이터 타입. 값 문자열은 {@code "yyyy-MM-dd"} 형식이다. */
	public static class DateType extends AbstractDataType<Date> implements DataType<Date> {
		// SimpleDateFormat은 thread-safe하지 않으므로 스레드별 인스턴스를 사용한다.
		private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER
									= ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

		private DateType() {
			super("xs:date", DataTypeDefXsd.DATE, Date.class);
		}

		@Override
		public String toValueString(Object obj) {
			Date value = DataUtils.asDate(obj);
			return (value != null) ? DATE_FORMATTER.get().format(value) : null;
		}

		@Override
		public Date parseValueString(String str) {
			try {
				return (str != null) ? DATE_FORMATTER.get().parse(str) : null;
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

	/** {@code xs:float} ↔ {@link Float} 데이터 타입. */
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
			return switch (jdbcObj) {
				case null -> null;
				case Double d -> d.floatValue();
				case Float f -> f;
				default -> throw new IllegalArgumentException("Invalid JDBC object type: " + jdbcObj.getClass());
			};
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

	/** {@code xs:int} ↔ {@link Integer} 데이터 타입. */
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

	/** {@code xs:long} ↔ {@link Long} 데이터 타입. */
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

	/** {@code xs:short} ↔ {@link Short} 데이터 타입. */
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

	/** {@code xs:double} ↔ {@link Double} 데이터 타입. */
	public static class DoubleType extends AbstractDataType<Double> implements DataType<Double> {
		private DoubleType() {
			super("xs:double", DataTypeDefXsd.DOUBLE, Double.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			Double value = DataUtils.asDouble(obj);
			return (value != null) ? ""+value : null;
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

	/** {@code xs:string} ↔ {@link String} 데이터 타입. */
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

	/** {@code xs:time} ↔ {@link LocalTime} 데이터 타입. */
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

	/** {@code xs:decimal} ↔ {@link BigDecimal} 데이터 타입. 정밀도 보존을 위해 {@link BigDecimal}로 직접 처리한다. */
	public static class DecimalType extends AbstractDataType<BigDecimal> implements DataType<BigDecimal> {
		private DecimalType() {
			super("xs:decimal", DataTypeDefXsd.DECIMAL, BigDecimal.class);
		}
	
		@Override
		public String toValueString(Object obj) {
			if ( obj == null ) {
				return null;
			}

			BigDecimal value = (obj instanceof BigDecimal bd) ? bd : BigDecimalParser.parse(obj.toString());
			return value.toPlainString();
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
