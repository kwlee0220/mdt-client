package mdt.task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.json.JacksonDeserializationException;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTParserException;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.FileStoreReference;
import mdt.model.sm.value.MultiLanguagePropertyValue;

/**
 * {@link TaskArgument}의 {@code @type} 기반 Json 직렬화/역직렬화에 대한 단위 테스트.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TaskArgumentTest {
	private static JsonNode toJsonNode(TaskArgument arg) {
		return MDTModelSerDe.getJsonMapper().valueToTree(arg);
	}

	@Test
	public void literal_roundTrip() throws IOException {
		TaskArgument arg = TaskArgument.literal(17);
		JsonNode jnode = toJsonNode(arg);
		Assertions.assertEquals(TaskArgument.LiteralArgument.SERIALIZATION_TYPE, jnode.get("@type").asText());

		TaskArgument restored = TaskArgument.parseJsonNode(jnode);
		Assertions.assertEquals(arg, restored);
	}

	@Test
	public void reference_roundTripThroughJackson() throws IOException {
		TaskArgument arg = TaskArgument.reference(DefaultElementReference.newInstance("inst", "Data", "DataInfo.x"));
		String json = MDTModelSerDe.getJsonMapper().writeValueAsString(arg);

		// @JsonDeserialize(using = TaskArgument.Deserializer)를 거쳐 등록부로 복원된다.
		TaskArgument restored = MDTModelSerDe.getJsonMapper().readValue(json, TaskArgument.class);
		Assertions.assertEquals(arg, restored);
	}

	@Test
	public void missingType_throws() {
		ObjectNode jnode = MDTModelSerDe.getJsonMapper().createObjectNode();
		jnode.put("value", 1);
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> TaskArgument.parseJsonNode(jnode));
		Assertions.assertEquals("'@type' field is missing: base=TaskArgument, available fields=[value]",
								e.getMessage());
	}

	@Test
	public void unregisteredType_throws() {
		ObjectNode jnode = MDTModelSerDe.getJsonMapper().createObjectNode();
		jnode.put("@type", "mdt:argument:bogus");
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> TaskArgument.parseJsonNode(jnode));
		// 등록 타입 목록의 순서는 고정되어 있지 않으므로 앞부분만 비교한다.
		Assertions.assertTrue(e.getMessage().startsWith("Unregistered TaskArgument type: mdt:argument:bogus"),
								e.getMessage());
	}

	// --- 필드 검증 ---

	@Test
	public void reference_missingReferenceField_throws() {
		ObjectNode jnode = MDTModelSerDe.getJsonMapper().createObjectNode();
		jnode.put("@type", TaskArgument.ReferenceArgument.SERIALIZATION_TYPE);
		// 이전에는 생성자의 IllegalArgumentException("ref must not be null")이었다.
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> TaskArgument.parseJsonNode(jnode));
		Assertions.assertEquals("Field not found: field=reference, available fields=[@type]", e.getMessage());
	}

	@Test
	public void reference_nonMDTReference_throws() throws IOException {
		// FileStoreReference는 ElementReference이지만 MDTElementReference가 아니다.
		// 이전에는 강제 형변환에서 ClassCastException이 났다.
		ObjectNode jnode = MDTModelSerDe.getJsonMapper().createObjectNode();
		jnode.put("@type", TaskArgument.ReferenceArgument.SERIALIZATION_TYPE);
		jnode.set("reference", new FileStoreReference(new File("/tmp/x.bin")).toJsonNode());
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> TaskArgument.parseJsonNode(jnode));
		Assertions.assertEquals("invalid field 'reference': MDTElementReference expected, but FileStoreReference",
								e.getMessage());
	}

	@Test
	public void literal_missingValueField_throws() {
		ObjectNode jnode = MDTModelSerDe.getJsonMapper().createObjectNode();
		jnode.put("@type", TaskArgument.LiteralArgument.SERIALIZATION_TYPE);
		// 이전에는 IllegalArgumentException("JsonNode is null")이었다.
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> TaskArgument.parseJsonNode(jnode));
		Assertions.assertEquals("Field not found: field=value, available fields=[@type]", e.getMessage());
	}

	// --- parseJsonNode ---

	@Test
	public void parseJsonNode_rejectsNull() {
		// 이전에는 MDTModelSerDe를 거쳐 null·JSON null에 대해 null을 돌려줬다.
		Assertions.assertThrows(IllegalArgumentException.class, () -> TaskArgument.parseJsonNode(null));
		Assertions.assertThrows(JacksonDeserializationException.class,
								() -> TaskArgument.parseJsonNode(NullNode.getInstance()));
	}

	@Test
	public void jacksonPath_reportsMissingFieldAsIOException() {
		// Jackson 경유(Deserializer)에서도 필드 누락이 IOException 계열로 전달된다.
		String json = "{\"@type\":\"" + TaskArgument.LiteralArgument.SERIALIZATION_TYPE + "\"}";
		Assertions.assertThrows(IOException.class,
								() -> MDTModelSerDe.getJsonMapper().readValue(json, TaskArgument.class));
	}

	// --- parseExpr (이전 이름 parseTaskArgument) ---

	@Test
	public void parseExpr_acceptsReferencesAndLiterals() {
		Assertions.assertInstanceOf(TaskArgument.ReferenceArgument.class,
									TaskArgument.parseExpr("param:inst:Speed"));
		Assertions.assertInstanceOf(TaskArgument.ReferenceArgument.class,
									TaskArgument.parseExpr("inst:Data:Info.x"));
		// 따옴표 없는 단어·숫자·불리언도 값 리터럴이다.
		Assertions.assertEquals("literal(\"hello\")", TaskArgument.parseExpr("hello").toString());
		Assertions.assertEquals("literal(42)", TaskArgument.parseExpr("42").toString());
		Assertions.assertEquals("literal(true)", TaskArgument.parseExpr("true").toString());
		// 문법에 맞지 않는 값은 따옴표로 감싸 문자열로 넘긴다.
		Assertions.assertEquals("literal(\"PT5S\")", TaskArgument.parseExpr("'PT5S'").toString());
	}

	@Test
	public void parseExpr_rejectsOtherExpressions() {
		// 이전에는 아래 입력을 모두 표현식 문자열 자체를 값으로 갖는 문자열 리터럴로 바꿨다
		// (후행 입력 오류인 'a.b'만 IllegalArgumentException으로 나갔다).
		for ( String expr: new String[] { "", "PT5S", "2024-01-01T00:00:00Z", "inst:Data",
											"param:inst:Speed=3", "null", "a.b", "hello world" } ) {
			Assertions.assertThrows(MDTParserException.class, () -> TaskArgument.parseExpr(expr), expr);
		}
		MDTParserException e = Assertions.assertThrows(MDTParserException.class,
															() -> TaskArgument.parseExpr("inst:Data"));
		Assertions.assertTrue(e.getMessage().startsWith(
				"TaskArgument expression must be an element reference or a value literal: 'inst:Data'"), e.getMessage());
		Assertions.assertThrows(IllegalArgumentException.class, () -> TaskArgument.parseExpr(null));
	}

	// --- equals / activate ---

	@Test
	public void equalsComparesWrappedReferenceOrValue() {
		Assertions.assertEquals(TaskArgument.literal(17), TaskArgument.literal(17));
		Assertions.assertEquals(TaskArgument.literal(17).hashCode(), TaskArgument.literal(17).hashCode());
		Assertions.assertNotEquals(TaskArgument.literal(17), TaskArgument.literal(18));
		Assertions.assertNotEquals(TaskArgument.literal("17"), TaskArgument.literal(17));

		Assertions.assertEquals(TaskArgument.reference("param:inst:Speed"), TaskArgument.reference("param:inst:Speed"));
		Assertions.assertEquals(TaskArgument.reference("param:inst:Speed").hashCode(),
								TaskArgument.reference("param:inst:Speed").hashCode());
		Assertions.assertNotEquals(TaskArgument.reference("param:inst:Speed"), TaskArgument.reference("param:inst:Load"));
		Assertions.assertNotEquals(TaskArgument.reference("param:inst:Speed"), TaskArgument.literal("param:inst:Speed"));
	}

	@Test
	public void literalActivate_isNoOp() {
		// 종류와 상관없이 activate를 부를 수 있다. 리터럴은 관리자를 쓰지 않는다.
		TaskArgument arg = TaskArgument.literal(1);
		arg.activate(null);
		Assertions.assertEquals(TaskArgument.literal(1), arg);
	}

	// --- parseJsonString ---

	@Test
	public void parseJsonString_followsParseJsonNode() throws IOException {
		TaskArgument arg = TaskArgument.literal(17);
		Assertions.assertEquals(arg, TaskArgument.parseJsonString(arg.toJsonString()));
		// 이전에는 MDTModelSerDe를 거쳐 "null"이면 null을 돌려줬다. 이제 parseJsonNode와 같이 거부한다.
		Assertions.assertThrows(JacksonDeserializationException.class, () -> TaskArgument.parseJsonString("null"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> TaskArgument.parseJsonString(null));
		// 필드 누락도 parseJsonNode와 같은 예외·메시지다(이전에는 JSON 전체를 담은 IOException).
		String json = "{\"@type\":\"" + TaskArgument.LiteralArgument.SERIALIZATION_TYPE + "\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> TaskArgument.parseJsonString(json));
		Assertions.assertEquals("Field not found: field=value, available fields=[@type]", e.getMessage());
		// JSON 문법 오류는 Jackson의 IOException이다.
		Assertions.assertThrows(IOException.class, () -> TaskArgument.parseJsonString("{"));
	}

	@Test
	public void literalEquality_followsMutableValue() {
		// 감싼 값이 가변이면 인자의 equals·hashCode도 따라 바뀐다(문서화한 계약).
		MultiLanguagePropertyValue value = new MultiLanguagePropertyValue(
				new java.util.ArrayList<>(List.of(MultiLanguagePropertyValue.buildLangStringTextType("en", "before"))));
		TaskArgument arg = TaskArgument.literal(value);
		int before = arg.hashCode();
		value.getLangTextAll().get(0).setText("after");
		Assertions.assertNotEquals(before, arg.hashCode());
	}

	@Test
	public void parseJsonString_rejectsTrailingInput() throws IOException {
		// 이전에는 첫 JSON 값 뒤의 입력을 무시했다(argument-spec-review.md AS-03과 같은 문제).
		String json = TaskArgument.literal(1).toJsonString();
		Assertions.assertThrows(IOException.class, () -> TaskArgument.parseJsonString(json + " xyz"));
		Assertions.assertThrows(IOException.class, () -> TaskArgument.parseJsonString(json + " {}"));
		Assertions.assertEquals(TaskArgument.literal(1), TaskArgument.parseJsonString(json + "\n"));
	}
}
