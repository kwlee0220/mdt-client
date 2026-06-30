package mdt.model.sm.ref.timeseries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.timeseries.TimeSeriesRange.Anchor;

/**
 * {@link ReadLastRecordsReference}의 단위 테스트.
 * <p>
 * {@code equals}는 submodel 참조만 비교하고 range/columns는 비교하지 않으므로,
 * range/columns의 보존은 {@code toStringExpr()}(이 둘을 인코딩) 문자열로 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ReadLastRecordsReferenceTest {
	private final ObjectMapper m_mapper = new ObjectMapper();

	private static ReadLastRecordsReference.Builder builder() {
		return ReadLastRecordsReference.builder()
										.submodelReference(DefaultSubmodelReference.ofIdShort("inst", "Data"));
	}

	// --- 생성/접근자 ---

	@Test
	public void builder_setsSubmodel() {
		ReadLastRecordsReference ref = builder().build();
		assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"), ref.getSubmodelReference());
	}

	@Test
	public void getSerializationType_isTimeseriesType() {
		assertEquals("mdt:ref:timeseries", builder().build().getSerializationType());
		assertEquals("mdt:ref:timeseries", ReadLastRecordsReference.SERIALIZATION_TYPE);
	}

	// --- toStringExpr (range / projection 형태) ---

	@Test
	public void toStringExpr_plain() {
		assertEquals("timeseries:inst:Data", builder().build().toStringExpr());
	}

	@Test
	public void toStringExpr_lastCount() {
		assertEquals("timeseries:inst:Data#last=7", builder().last(7).build().toStringExpr());
	}

	@Test
	public void last_bareNumberIsCount_suffixIsDuration() throws IOException {
		// last=N (접미사 없음) → 마지막 N개 레코드(Count): JSON 'last' 필드는 숫자
		ReadLastRecordsReference count = (ReadLastRecordsReference)
				ElementReferences.parseExpr("timeseries:inst:Data#last=10");
		assertEquals("timeseries:inst:Data#last=10", count.toStringExpr());
		assertEquals(10, m_mapper.readTree(count.toJsonString()).path("last").asInt());

		// last=10s (접미사) → 마지막 10초(Trailing, latest): JSON 'last' 필드는 "<기간>@<anchor>" 문자열
		// ReadLastRecordsReference.toStringExpr()은 anchor를 항상 명시하므로 '@latest'가 보존된다.
		ReadLastRecordsReference dur = (ReadLastRecordsReference)
				ElementReferences.parseExpr("timeseries:inst:Data#last=10s");
		assertEquals("timeseries:inst:Data#last=PT10S@latest", dur.toStringExpr());
		assertEquals("PT10S@latest", m_mapper.readTree(dur.toJsonString()).path("last").asText());
	}

	@Test
	public void toStringExpr_lastLatest() {
		// last(Duration, LATEST) → anchor를 항상 명시하므로 '@latest'가 표기된다.
		assertEquals("timeseries:inst:Data#last=PT1H@latest",
					builder().last(Duration.ofHours(1), Anchor.LATEST).build().toStringExpr());
	}

	@Test
	public void toStringExpr_nowAnchor() {
		// last(Duration, NOW) → '@now' 표기
		assertEquals("timeseries:inst:Data#last=PT1H@now",
					builder().last(Duration.ofHours(1), Anchor.NOW).build().toStringExpr());
	}

	@Test
	public void toStringExpr_absoluteRange() {
		TimeSeriesRange r = TimeSeriesRange.between(Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-02-01T00:00:00Z"));
		assertEquals("timeseries:inst:Data#2024-01-01T00:00:00Z~2024-02-01T00:00:00Z",
					builder().range(r).build().toStringExpr());
	}

	@Test
	public void toStringExpr_columns_excludesImplicitTime() {
		assertEquals("timeseries:inst:Data|current,power",
					builder().columns(List.of("current", "power")).build().toStringExpr());
	}

	@Test
	public void toStringExpr_rangeAndColumns() {
		assertEquals("timeseries:inst:Data#last=7|current,power",
					builder().last(7).columns(List.of("current", "power")).build().toStringExpr());
	}

	// --- 표현식 round-trip (parseExpr ↔ toStringExpr) ---

	@Test
	public void exprRoundTrip_allForms() {
		assertExprRoundTrips("timeseries:inst:Data");
		assertExprRoundTrips("timeseries:inst:Data#last=7");				// 마지막 N개(count)
		assertExprRoundTrips("timeseries:inst:Data#last=PT1H@latest");
		assertExprRoundTrips("timeseries:inst:Data#last=PT2H30M@now");
		assertExprRoundTrips("timeseries:inst:Data#2024-01-01T00:00:00Z~2024-02-01T00:00:00Z");
		assertExprRoundTrips("timeseries:inst:Data|current,power");
		assertExprRoundTrips("timeseries:inst:Data#last=7|current,power");
		assertExprRoundTrips("timeseries:inst:Data#last=5|current");
		// 개방형 절대 범위
		assertExprRoundTrips("timeseries:inst:Data#2024-01-01T09:00:00Z~");
		assertExprRoundTrips("timeseries:inst:Data#~2024-02-01T00:00:00Z");
	}

	@Test
	public void parseExpr_returnsReadLastRecordsReference() {
		ElementReference ref = ElementReferences.parseExpr("timeseries:inst:Data#last=7|current,power");
		assertTrue(ref instanceof ReadLastRecordsReference);
		assertEquals(DefaultSubmodelReference.ofIdShort("inst", "Data"),
					((ReadLastRecordsReference)ref).getSubmodelReference());
	}

	// --- JSON 직렬화/역직렬화 round-trip ---

	@Test
	public void jsonRoundTrip_allForms() throws IOException {
		assertJsonRoundTrips(builder().build());
		assertJsonRoundTrips(builder().last(7).build());
		assertJsonRoundTrips(builder().last(Duration.ofHours(1), Anchor.LATEST).build());
		// NOW anchor도 'duration' 필드로 직렬화되어 round-trip 보존된다.
		assertJsonRoundTrips(builder().last(Duration.ofMinutes(30), Anchor.NOW).build());
		assertJsonRoundTrips(builder().range(TimeSeriesRange.between(Instant.parse("2024-01-01T00:00:00Z"),
															Instant.parse("2024-02-01T00:00:00Z"))).build());
		assertJsonRoundTrips(builder().columns(List.of("current", "power")).build());
		assertJsonRoundTrips(builder().last(7).columns(List.of("current", "power")).build());
	}

	@Test
	public void serialize_columnsExcludeImplicitTime() throws IOException {
		ReadLastRecordsReference ref = builder().columns(List.of("current", "power")).build();
		JsonNode node = m_mapper.readTree(ref.toJsonString());

		JsonNode cols = node.get("columns");
		Assertions.assertNotNull(cols);
		assertEquals(2, cols.size());
		assertEquals("current", cols.get(0).asText());
		assertEquals("power", cols.get(1).asText());
	}

	@Test
	public void serialize_rangeFieldsByVariant() throws IOException {
		// Count와 Trailing은 모두 'last' 필드를 사용하되, 숫자(Count) / 문자열(Trailing)로 구분된다.
		JsonNode countNode = m_mapper.readTree(builder().last(7).build().toJsonString());
		assertTrue(countNode.path("last").isInt());
		assertEquals(7, countNode.path("last").asInt());

		// Trailing은 'last' 필드 하나에 "<기간>@<anchor>" 문자열로 직렬화된다(anchor 보존).
		JsonNode latestNode = m_mapper.readTree(builder().last(Duration.ofHours(1), Anchor.LATEST)
															.build().toJsonString());
		assertTrue(latestNode.path("last").isTextual());
		assertEquals("PT1H@latest", latestNode.path("last").asText());

		JsonNode nowNode = m_mapper.readTree(builder().last(Duration.ofHours(1), Anchor.NOW)
															.build().toJsonString());
		assertTrue(nowNode.path("last").isTextual());
		assertEquals("PT1H@now", nowNode.path("last").asText());

		// Absolute는 'timeSpan' 객체 아래 'from'/'to' 필드를 사용한다(한쪽 개방 가능).
		assertHasTimeSpanField(builder().range(TimeSeriesRange.between(Instant.parse("2024-01-01T00:00:00Z"), null)).build(), "from");
		assertHasTimeSpanField(builder().range(TimeSeriesRange.between(null, Instant.parse("2024-02-01T00:00:00Z"))).build(), "to");
	}

	// --- equals 동작 명세(range/columns는 비교에 포함되지 않음) ---

	@Test
	public void equals_ignoresRangeAndColumns() {
		ReadLastRecordsReference plain = builder().build();
		ReadLastRecordsReference withRange = builder().last(7).columns(List.of("current")).build();
		// submodel 참조가 같으면 range/columns가 달라도 동치로 취급된다.
		assertEquals(plain, withRange);
	}

	@Test
	public void equals_differentSubmodel_areNotEqual() {
		ReadLastRecordsReference r1 = builder().build();
		ReadLastRecordsReference r2 = ReadLastRecordsReference.builder()
										.submodelReference(DefaultSubmodelReference.ofIdShort("inst", "Other"))
										.build();
		Assertions.assertNotEquals(r1, r2);
	}

	// --- 헬퍼 ---

	private static void assertExprRoundTrips(String expr) {
		ElementReference ref = ElementReferences.parseExpr(expr);
		assertTrue(ref instanceof ReadLastRecordsReference, "not a ReadLastRecordsReference: " + expr);
		assertEquals(expr, ref.toStringExpr(), "toStringExpr mismatch");
		// 재파싱 결과도 동일해야 한다(멱등).
		assertEquals(expr, ElementReferences.parseExpr(ref.toStringExpr()).toStringExpr(), "re-parse mismatch");
	}

	private void assertJsonRoundTrips(ReadLastRecordsReference ref) throws IOException {
		String json = ref.toJsonString();
		ElementReference restored = ElementReferences.parseJsonString(json);
		assertTrue(restored instanceof ReadLastRecordsReference, "restored type mismatch");
		// equals는 range/columns를 무시하므로 toStringExpr로 전체 보존을 검증한다.
		assertEquals(ref.toStringExpr(), restored.toStringExpr(), "JSON round-trip mismatch");
	}

	private void assertHasTimeSpanField(ReadLastRecordsReference ref, String field) throws IOException {
		JsonNode node = m_mapper.readTree(ref.toJsonString());
		Assertions.assertFalse(node.path("timeSpan").path(field).isMissingNode(),
								"missing JSON field: timeSpan." + field);
	}
}
