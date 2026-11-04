package mdt.aas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;


/**
 * {@link DataTypes}의 값 문자열 변환을 검사한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DataTypesTest {
	private static final List<DataType<?>> ALL_TYPES = List.of(
			DataTypes.STRING, DataTypes.BOOLEAN, DataTypes.SHORT, DataTypes.INT, DataTypes.LONG,
			DataTypes.FLOAT, DataTypes.DOUBLE, DataTypes.DATE_TIME, DataTypes.DATE, DataTypes.TIME,
			DataTypes.DURATION, DataTypes.DECIMAL);

	@Test
	public void toValueStringOfNullIsNull() {
		// DataType.toValueString()의 계약: null이면 null을 반환한다.
		for ( DataType<?> type: ALL_TYPES ) {
			assertNull(type.toValueString(null), "type=" + type.getId());
		}
	}

	@Test
	public void toValueStringOfNonNullIsUnchanged() {
		assertEquals("true", DataTypes.BOOLEAN.toValueString(true));
		assertEquals("0", DataTypes.INT.toValueString(0));
		assertEquals("7", DataTypes.SHORT.toValueString((short)7));
		assertEquals("12", DataTypes.INT.toValueString(12));
		assertEquals("12", DataTypes.INT.toValueString("12"));
		assertEquals("3000000000", DataTypes.LONG.toValueString(3_000_000_000L));
		assertEquals("1.5", DataTypes.FLOAT.toValueString(1.5f));
		assertEquals("0.0", DataTypes.DOUBLE.toValueString(0.0));
		assertEquals("2.5", DataTypes.DOUBLE.toValueString(2.5));
		assertEquals("2026-10-28", DataTypes.DATE.toValueString(java.sql.Date.valueOf("2026-10-28")));
		assertEquals("PT1M", DataTypes.DURATION.toValueString(Duration.ofMinutes(1)));
		Instant ts = Instant.parse("2026-10-28T01:02:03Z");
		assertEquals(ts, DataTypes.DATE_TIME.parseValueString(DataTypes.DATE_TIME.toValueString(ts)));
	}

	@Test
	public void timeUsesXsdTimeFormat() {
		DataType<LocalTime> time = DataTypes.TIME;

		// 24시간제 HH:mm:ss 형식이며, 오후 시각이 오전으로 바뀌지 않는다.
		assertEquals("13:05:09", time.toValueString(LocalTime.of(13, 5, 9)));
		assertEquals("00:00:00", time.toValueString(LocalTime.MIDNIGHT));
		assertEquals("13:05:09.25", time.toValueString(LocalTime.of(13, 5, 9, 250_000_000)));

		assertEquals(LocalTime.of(13, 5, 9), time.parseValueString("13:05:09"));
		assertEquals(LocalTime.of(13, 5, 9, 250_000_000), time.parseValueString("13:05:09.25"));
		assertNull(time.parseValueString(""));
		assertThrows(IllegalArgumentException.class, () -> time.parseValueString("13-05-09"));
		assertThrows(IllegalArgumentException.class, () -> time.parseValueString("13:05:09Z"));

		LocalTime t = LocalTime.of(23, 59, 1);
		assertEquals(t, time.parseValueString(time.toValueString(t)));
	}

	@Test
	public void timeConvertsJdbcAndOtherObjects() {
		DataType<LocalTime> time = DataTypes.TIME;
		LocalTime t = LocalTime.of(17, 30, 0);

		assertEquals(java.sql.Time.valueOf(t), time.toJdbcObject(t));
		assertEquals(t, time.fromJdbcObject(java.sql.Time.valueOf(t)));
		assertEquals(t, time.fromJdbcObject(t));
		assertEquals(t, time.fromJdbcObject("17:30:00"));
		assertNull(time.fromJdbcObject(null));
		assertNull(time.toJdbcObject(null));

		assertEquals("17:30:00", time.toValueString(java.sql.Time.valueOf(t)));
		assertEquals("17:30:00", time.toValueString("17:30"));
		assertThrows(IllegalArgumentException.class, () -> time.toValueString(1730));
	}

	@Test
	public void durationJdbcValuesAreSeconds() {
		// 정수·실수 모두 초 단위로 읽고, 쓸 때도 초 단위 실수로 쓴다.
		assertEquals(Duration.ofSeconds(90), DataTypes.DURATION.fromJdbcObject(90L));
		assertEquals(Duration.ofSeconds(90), DataTypes.DURATION.fromJdbcObject(90));
		assertEquals(Duration.ofMillis(1500), DataTypes.DURATION.fromJdbcObject(1.5));
		assertEquals(Duration.ofMillis(1500), DataTypes.DURATION.fromJdbcObject(1.5f));
		assertEquals(1.5, DataTypes.DURATION.toJdbcObject(Duration.ofMillis(1500)));
		assertEquals(Duration.ofMillis(1500),
					DataTypes.DURATION.fromJdbcObject(DataTypes.DURATION.toJdbcObject(Duration.ofMillis(1500))));
		assertNull(DataTypes.DURATION.toJdbcObject(null));
	}

	@Test
	public void numericTypesAcceptJdbcNumbers() {
		// PostgreSQL NUMERIC 컬럼이나 avg() 결과는 BigDecimal로 온다.
		java.math.BigDecimal avg = new java.math.BigDecimal("12.75");
		assertEquals(12.75, DataTypes.DOUBLE.fromJdbcObject(avg));
		assertEquals(12.75f, DataTypes.FLOAT.fromJdbcObject(avg));
		// 정수 타입은 소수 부분이 있으면 오류로 처리한다.
		assertThrows(IllegalArgumentException.class, () -> DataTypes.INT.fromJdbcObject(avg));
		assertEquals(12, DataTypes.INT.fromJdbcObject(new java.math.BigDecimal("12.00")));
		assertEquals(false, DataTypes.BOOLEAN.fromJdbcObject("0"));
		assertEquals(12L, DataTypes.LONG.fromJdbcObject(java.math.BigInteger.valueOf(12)));
		assertEquals((short)7, DataTypes.SHORT.fromJdbcObject((byte)7));
		assertEquals("12.75", DataTypes.DOUBLE.toValueString(avg));
		assertEquals("12", DataTypes.INT.toValueString((short)12));
	}

	@Test
	public void dateTypesHandleJdbcNullAndObjects() {
		assertNull(DataTypes.DATE_TIME.fromJdbcObject(null));
		assertNull(DataTypes.DATE.fromJdbcObject(null));

		Instant ts = Instant.parse("2026-10-28T01:02:03Z");
		assertEquals(ts, DataTypes.DATE_TIME.fromJdbcObject(java.sql.Timestamp.from(ts)));

		String day = "2026-10-28";
		for ( Object in: List.of(java.sql.Date.valueOf(day), java.time.LocalDate.parse(day),
								java.sql.Timestamp.valueOf(day + " 13:00:00"), day) ) {
			assertEquals(day, DataTypes.DATE.toValueString(DataTypes.DATE.fromJdbcObject(in)), "input=" + in);
		}
		// LocalDate를 직접 값 문자열로 바꿔도 날짜가 바뀌지 않는다.
		assertEquals(day, DataTypes.DATE.toValueString(java.time.LocalDate.parse(day)));
	}

	@Test
	public void dateTimeAcceptsOffsetAndZonedValues() {
		Instant ts = Instant.parse("2026-10-28T01:02:03Z");
		String expected = DataTypes.DATE_TIME.toValueString(ts);

		assertEquals(expected, DataTypes.DATE_TIME.toValueString(ts.atOffset(java.time.ZoneOffset.ofHours(9))));
		assertEquals(expected, DataTypes.DATE_TIME.toValueString(ts.atZone(java.time.ZoneOffset.UTC)));
		assertEquals(expected, DataTypes.DATE_TIME.toValueString(java.sql.Timestamp.from(ts)));
		assertEquals(ts, DataTypes.DATE_TIME.fromJdbcObject(ts.atOffset(java.time.ZoneOffset.UTC)));
	}

	// --- fromJsonNode: 값을 조용히 바꾸지 않는다 ---

	private static com.fasterxml.jackson.databind.JsonNode json(String text) throws java.io.IOException {
		return mdt.model.MDTModelSerDe.getJsonMapper().readTree(text);
	}

	private static void assertRejected(DataType<?> type, String jsonText) {
		utils.json.JacksonDeserializationException e = assertThrows(utils.json.JacksonDeserializationException.class,
														() -> type.fromJsonNode(json(jsonText)), type.getId() + " " + jsonText);
		org.junit.jupiter.api.Assertions.assertTrue(e.getMessage().startsWith("invalid " + type.getId() + " value: "),
													e.getMessage());
	}

	@Test
	public void fromJsonNode_integralTypes() throws java.io.IOException {
		assertEquals(42, DataTypes.INT.fromJsonNode(json("42")));
		assertEquals(42, DataTypes.INT.fromJsonNode(json("\"42\"")));		// 숫자 문자열은 받는다
		assertEquals(3, DataTypes.INT.fromJsonNode(json("3.0")));			// 손실 없는 실수는 받는다
		assertNull(DataTypes.INT.fromJsonNode(json("null")));
		// 이전에는 "abc"→0, 3.9→3, 3000000000→-1294967296으로 오류 없이 바꿨다.
		for ( String bad: new String[] { "\"abc\"", "3.9", "\"3.9\"", "3000000000", "true", "{}", "[]" } ) {
			assertRejected(DataTypes.INT, bad);
		}
		assertEquals(3000000000L, DataTypes.LONG.fromJsonNode(json("3000000000")));
		assertRejected(DataTypes.LONG, "1e30");
		assertRejected(DataTypes.LONG, "\"x\"");
		assertEquals((short)7, DataTypes.SHORT.fromJsonNode(json("7")));
		assertRejected(DataTypes.SHORT, "40000");
		assertRejected(DataTypes.SHORT, "1.5");
		assertEquals("invalid xs:int value: int expected, but non-numeric string",
					assertThrows(utils.json.JacksonDeserializationException.class,
								() -> DataTypes.INT.fromJsonNode(json("\"abc\""))).getMessage());
	}

	@Test
	public void fromJsonNode_realTypes() throws java.io.IOException {
		assertEquals(1.5, DataTypes.DOUBLE.fromJsonNode(json("1.5")));
		assertEquals(2.0, DataTypes.DOUBLE.fromJsonNode(json("\"2\"")));
		assertEquals(1.5f, DataTypes.FLOAT.fromJsonNode(json("1.5")));
		for ( String bad: new String[] { "\"abc\"", "\"NaN\"", "\"Infinity\"", "1e400", "true" } ) {
			assertRejected(DataTypes.DOUBLE, bad);
		}
		assertRejected(DataTypes.FLOAT, "1e300");			// float 범위 초과
		assertEquals(new java.math.BigDecimal("0.1"), DataTypes.DECIMAL.fromJsonNode(json("\"0.1\"")));
		// 고정밀 값은 문자열로 넣으면 그대로 보존된다. JSON 숫자로 넣으면 공유 매퍼가 readTree 단계에서
		// double로 읽어 정밀도를 잃는다(매퍼 설정의 한계, fromJsonNode와 무관).
		assertEquals(new java.math.BigDecimal("12345678901234567890.5"),
					DataTypes.DECIMAL.fromJsonNode(json("\"12345678901234567890.5\"")));
		assertRejected(DataTypes.DECIMAL, "\"abc\"");
	}

	@Test
	public void fromJsonNode_boolean() throws java.io.IOException {
		assertEquals(true, DataTypes.BOOLEAN.fromJsonNode(json("true")));
		assertEquals(false, DataTypes.BOOLEAN.fromJsonNode(json("\"FALSE\"")));
		assertEquals(true, DataTypes.BOOLEAN.fromJsonNode(json("\"1\"")));
		// 이전에는 "yes"를 false로 읽었다.
		for ( String bad: new String[] { "\"yes\"", "1", "{}" } ) {
			assertRejected(DataTypes.BOOLEAN, bad);
		}
		// 값 문자열 해석도 같은 규칙이다.
		assertEquals(false, DataTypes.BOOLEAN.parseValueString("0"));
		assertNull(DataTypes.BOOLEAN.parseValueString(""));
		assertThrows(IllegalArgumentException.class, () -> DataTypes.BOOLEAN.parseValueString("yes"));
	}

	@Test
	public void fromJsonNode_stringAndTemporal() throws java.io.IOException {
		// 문자열은 스칼라를 텍스트로, 객체·배열은 JSON 텍스트로 받는다(RPC 출력 경로 호환).
		assertEquals("1", DataTypes.STRING.fromJsonNode(json("1")));
		assertEquals("{\"a\":1}", DataTypes.STRING.fromJsonNode(json("{\"a\":1}")));
		assertEquals(Duration.ofSeconds(5), DataTypes.DURATION.fromJsonNode(json("\"PT5S\"")));
		assertRejected(DataTypes.DURATION, "5");
		assertRejected(DataTypes.DATE_TIME, "\"not-a-date\"");
		assertRejected(DataTypes.TIME, "{}");
	}

	/**
	 * {@link DataTypes#TYPES}와 {@link DataTypes#NAME_TO_TYPES}는 수정할 수 없다(mdt-client API 검토 D7, 2026-11-03).
	 * 이전에는 {@code TYPES}가 {@code Arrays.asList}라서 {@code set()}이 되었고 내부 배열까지 바뀌었다.
	 */
	@Test
	public void typesAndNameMapAreImmutable() {
		assertThrows(UnsupportedOperationException.class, () -> DataTypes.TYPES.set(0, DataTypes.INT));
		assertThrows(UnsupportedOperationException.class, () -> DataTypes.NAME_TO_TYPES.put("X", DataTypes.INT));
		assertEquals(DataTypes.STRING, DataTypes.TYPES.get(0));
	}

	/**
	 * {@code xs:date}는 {@link java.time.LocalDate}로 다룬다(mdt-client API 검토 I7).
	 */
	@Test
	public void dateIsLocalDate() {
		java.time.LocalDate day = java.time.LocalDate.of(2026, 10, 28);
		assertEquals(java.time.LocalDate.class, DataTypes.DATE.getJavaClass());
		assertEquals(day, DataTypes.DATE.parseValueString("2026-10-28"));
		assertEquals(day, DataTypes.DATE.parseValueString("2026-10-28Z"));
		assertEquals(day, DataTypes.DATE.parseValueString("2026-10-28+09:00"));
		assertNull(DataTypes.DATE.parseValueString(""));
		assertThrows(IllegalArgumentException.class, () -> DataTypes.DATE.parseValueString("2026-10-28xyz"));
		assertThrows(IllegalArgumentException.class, () -> DataTypes.DATE.parseValueString("2026/10/28"));
		assertEquals(java.sql.Date.valueOf(day), DataTypes.DATE.toJdbcObject(day));

		// JDBC 드라이버·utils.jdbc.SQLDataTypes.DATE의 날짜 클래스로도 찾는다(mdt-instance JDBC 컬럼 매핑).
		assertEquals(DataTypes.DATE, DataTypes.fromJavaClass(java.time.LocalDate.class));
		assertEquals(DataTypes.DATE, DataTypes.fromJavaClass(java.sql.Date.class));
		assertEquals(DataTypes.DATE, DataTypes.fromJavaClass(java.util.Date.class));
	}
}
