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
}
