package mdt.workflow.model;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonDeserializationException;

import mdt.model.MDTModelSerDe;
import mdt.model.expr.MDTParserException;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.PropertyValue;
import mdt.workflow.model.ArgumentSpec.LiteralArgument;
import mdt.workflow.model.ArgumentSpec.TaskOutputArgument;

/**
 * {@link ArgumentSpec}의 공통 속성(식별자/이름/설명)이 JSON 직렬화를 왕복해도 보존되는지 검증한다.
 */
public class ArgumentSpecRoundTripTest {
	@Test
	public void literal_preservesCommonFields() throws IOException {
		LiteralArgument arg = LiteralArgument.builder()
											.id("sleepTime")
											.name("SleepTime")
											.description("대기 시간(초)")
											.value(5)
											.build();

		ArgumentSpec restored = ArgumentSpec.parseJsonString(arg.toJsonString());

		Assertions.assertEquals("sleepTime", restored.getId());
		Assertions.assertEquals("SleepTime", restored.getName());
		Assertions.assertEquals("대기 시간(초)", restored.getDescription());
	}

	@Test
	public void taskOutput_preservesCommonFields() throws IOException {
		TaskOutputArgument arg = TaskOutputArgument.builder()
													.id("result")
													.name("Result")
													.description("이전 태스크의 결과")
													.taskId("task1")
													.argumentName("out1")
													.build();

		ArgumentSpec restored = ArgumentSpec.parseJsonString(arg.toJsonString());

		Assertions.assertEquals("result", restored.getId());
		Assertions.assertEquals("Result", restored.getName());
		Assertions.assertEquals("이전 태스크의 결과", restored.getDescription());
		Assertions.assertEquals("task1", ((TaskOutputArgument)restored).getTaskId());
	}

	@Test
	public void missingRequiredField_isRejectedByBuild() {
		// 식별자는 필수이다.
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> LiteralArgument.builder().value(5).build());
		// 하위 타입의 필수 필드도 마찬가지이다.
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> LiteralArgument.builder().id("noValue").build());
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> TaskOutputArgument.builder().id("x").taskId("task1").build());
	}

	@Test
	public void idIsAlwaysSerialized_whileNameAndDescriptionAreNot() throws IOException {
		LiteralArgument arg = ArgumentSpec.literal("sleepTime", PropertyValue.INTEGER(5));

		String json = arg.toJsonString();
		Assertions.assertTrue(json.contains("\"id\""));
		Assertions.assertFalse(json.contains("\"name\""));
		Assertions.assertFalse(json.contains("\"description\""));

		ArgumentSpec restored = ArgumentSpec.parseJsonString(json);
		Assertions.assertEquals("sleepTime", restored.getId());
		Assertions.assertNull(restored.getName());
		Assertions.assertNull(restored.getDescription());
	}

	@Test
	public void missingIdInJson_isRejected() {
		String json = "{\"@type\":\"mdt:task_arg:task-output\",\"taskId\":\"t\",\"argumentName\":\"a\"}";
		Assertions.assertThrows(IOException.class, () -> ArgumentSpec.parseJsonString(json));
	}

	private static JsonNode tree(String json) throws IOException {
		return MDTModelSerDe.getJsonMapper().readTree(json);
	}

	@Test
	public void missingType_isRejected() {
		String json = "{\"id\":\"x\",\"value\":1}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonNode(tree(json)));
		Assertions.assertEquals("'@type' field is missing: base=ArgumentSpec, available fields=[id, value]",
								e.getMessage());
	}

	@Test
	public void unregisteredType_isRejected() {
		String json = "{\"@type\":\"mdt:task_arg:bogus\",\"id\":\"x\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonNode(tree(json)));
		// 등록 타입 목록의 순서는 고정되어 있지 않으므로 앞부분만 비교한다.
		Assertions.assertTrue(e.getMessage().startsWith("Unregistered ArgumentSpec type: mdt:task_arg:bogus"),
								e.getMessage());
	}

	@Test
	public void literalWithoutValue_isRejected() {
		// 이전에는 IllegalArgumentException("JsonNode is null")이었다.
		String json = "{\"@type\":\"mdt:task_arg:literal\",\"id\":\"x\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonNode(tree(json)));
		Assertions.assertEquals("Field not found: field=value, available fields=[@type, id]", e.getMessage());
	}

	@Test
	public void parseJsonNodeAndString_rejectNull() {
		// 이전에는 MDTModelSerDe를 거쳐 null·JSON null·"null"에 대해 null을 돌려줬다.
		Assertions.assertThrows(IllegalArgumentException.class, () -> ArgumentSpec.parseJsonNode(null));
		Assertions.assertThrows(JacksonDeserializationException.class,
								() -> ArgumentSpec.parseJsonNode(com.fasterxml.jackson.databind.node.NullNode.getInstance()));
		Assertions.assertThrows(JacksonDeserializationException.class, () -> ArgumentSpec.parseJsonString("null"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ArgumentSpec.parseJsonString(null));
	}

	@Test
	public void parseJsonString_reportsMissingFieldWithoutDumpingJson() {
		// 이전에는 MDTModelSerDe가 JSON 전체를 메시지에 넣은 IOException으로 감쌌다.
		String json = "{\"@type\":\"mdt:task_arg:task-output\",\"id\":\"x\",\"taskId\":\"t\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonString(json));
		Assertions.assertEquals("Field not found: field=argumentName, available fields=[@type, id, taskId]",
								e.getMessage());
	}

	@Test
	public void builderErrorsNameTheBuilderMethod() {
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
															() -> TaskOutputArgument.builder().id("x").taskId("t").build());
		Assertions.assertEquals("argumentName is not set", e.getMessage());
		e = Assertions.assertThrows(IllegalArgumentException.class,
									() -> ArgumentSpec.ReferenceArgument.builder().id("x").build());
		Assertions.assertEquals("reference is not set", e.getMessage());
		e = Assertions.assertThrows(IllegalArgumentException.class, () -> LiteralArgument.builder().value(1).build());
		Assertions.assertEquals("id is not set", e.getMessage());
	}

	// --- 참조 표현식 검증 ---

	@Test
	public void reference_rejectsInvalidExpressionAtConstruction() {
		// 이전에는 생성 시점에 검증하지 않아 태스크를 실행할 때에야 드러났다.
		MDTParserException e = Assertions.assertThrows(MDTParserException.class,
															() -> ArgumentSpec.reference("x", "param:"));
		Assertions.assertTrue(e.getMessage().startsWith("invalid reference expression: 'param:'"), e.getMessage());
		// 후행 입력 오류(파서의 IllegalArgumentException)도 같은 예외로 맞춘다.
		Assertions.assertThrows(MDTParserException.class, () -> ArgumentSpec.reference("x", "inst:Data:a b"));
		// Submodel 참조처럼 요소 참조가 아닌 표현식도 거부한다.
		Assertions.assertThrows(MDTParserException.class, () -> ArgumentSpec.reference("x", "inst:Data"));
		Assertions.assertEquals("param:inst:Speed", ArgumentSpec.reference("x", "param:inst:Speed").getReferenceExpr());
	}

	@Test
	public void reference_rejectsInvalidExpressionInJson() {
		String json = "{\"@type\":\"mdt:task_arg:reference\",\"id\":\"x\",\"reference\":\"param:\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonString(json));
		Assertions.assertTrue(e.getMessage().startsWith("invalid field 'reference': invalid reference expression: 'param:'"),
								e.getMessage());
	}

	// --- 입력 검증 ---

	@Test
	public void blankId_isRejected() {
		// 이전에는 빈 식별자도 받아들였다.
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
															() -> ArgumentSpec.literal(" ", 1));
		Assertions.assertEquals("id is blank", e.getMessage());
		String json = "{\"@type\":\"mdt:task_arg:task-output\",\"id\":\"\",\"taskId\":\"t\",\"argumentName\":\"a\"}";
		JacksonDeserializationException je = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonString(json));
		Assertions.assertEquals("invalid field 'id': blank", je.getMessage());
	}

	@Test
	public void nullBoxedLiteral_isRejected() {
		// 이전에는 값이 빈 PropertyValue를 가진 리터럴이 만들어졌다.
		Assertions.assertThrows(IllegalArgumentException.class, () -> ArgumentSpec.literal("a", (Integer)null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ArgumentSpec.literal("a", (String)null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> LiteralArgument.builder().id("a").value((Double)null));
	}

	// --- 동등성 ---

	@Test
	public void equalsComparesCommonAndOwnFields() throws IOException {
		Assertions.assertEquals(ArgumentSpec.literal("a", 1), ArgumentSpec.literal("a", 1));
		Assertions.assertEquals(ArgumentSpec.literal("a", 1).hashCode(), ArgumentSpec.literal("a", 1).hashCode());
		Assertions.assertNotEquals(ArgumentSpec.literal("a", 1), ArgumentSpec.literal("a", 2));
		Assertions.assertNotEquals(ArgumentSpec.literal("a", 1), ArgumentSpec.literal("b", 1));
		Assertions.assertNotEquals(ArgumentSpec.literal("a", 1),
									LiteralArgument.builder().id("a").name("A").value(1).build());

		Assertions.assertEquals(ArgumentSpec.reference("r", "param:i:x"), ArgumentSpec.reference("r", "param:i:x"));
		Assertions.assertNotEquals(ArgumentSpec.reference("r", "param:i:x"), ArgumentSpec.reference("r", "param:i:y"));
		Assertions.assertEquals(ArgumentSpec.taskOutput("o", "t", "a"), ArgumentSpec.taskOutput("o", "t", "a"));
		Assertions.assertNotEquals(ArgumentSpec.taskOutput("o", "t", "a"), ArgumentSpec.taskOutput("o", "t", "b"));

		// JSON 왕복 후에도 같다.
		ArgumentSpec spec = TaskOutputArgument.builder().id("o").name("Out").description("d").taskId("t").argumentName("a").build();
		Assertions.assertEquals(spec, ArgumentSpec.parseJsonString(spec.toJsonString()));
	}

	// --- 외부 검토(argument-spec-review.md AS-01 ~ AS-04, AS-07) ---

	@Test
	public void taskOutput_rejectsBlankSource() {
		// AS-01: 이전에는 공백뿐인 taskId·argumentName도 받아들였다.
		Assertions.assertEquals("taskId is blank", Assertions.assertThrows(IllegalArgumentException.class,
														() -> ArgumentSpec.taskOutput("in", " ", "out")).getMessage());
		Assertions.assertEquals("argumentName is blank", Assertions.assertThrows(IllegalArgumentException.class,
														() -> ArgumentSpec.taskOutput("in", "t", "")).getMessage());
		String json = "{\"@type\":\"mdt:task_arg:task-output\",\"id\":\"in\",\"taskId\":\"t\",\"argumentName\":\" \"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
															() -> ArgumentSpec.parseJsonString(json));
		Assertions.assertEquals("invalid field 'argumentName': blank", e.getMessage());
	}

	@Test
	public void identifiers_mustBeJsonStrings() throws IOException {
		// AS-02: 이전에는 1·true를 "1"·"true"로 받아들였다.
		for ( String field: new String[] { "id", "taskId", "argumentName" } ) {
			for ( String bad: new String[] { "1", "true", "[]", "{}" } ) {
				String json = taskOutputJson("in", "t", "out").replace("\"" + field + "\":\"" + valueOf(field) + "\"",
																	"\"" + field + "\":" + bad);
				JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
																	() -> ArgumentSpec.parseJsonString(json), json);
				Assertions.assertTrue(e.getMessage().startsWith("invalid field '" + field + "': string expected"),
										e.getMessage());
			}
		}
		// JSON 문자열이면 내용이 숫자처럼 보여도 받는다.
		ArgumentSpec spec = ArgumentSpec.parseJsonString(taskOutputJson("1", "true", "2"));
		Assertions.assertEquals(ArgumentSpec.taskOutput("1", "true", "2"), spec);
		// 참조 표현식도 문자열이어야 한다.
		String refJson = "{\"@type\":\"mdt:task_arg:reference\",\"id\":\"r\",\"reference\":1}";
		Assertions.assertThrows(JacksonDeserializationException.class, () -> ArgumentSpec.parseJsonString(refJson));
	}

	private static String valueOf(String field) {
		return switch ( field ) { case "id" -> "in"; case "taskId" -> "t"; default -> "out"; };
	}

	private static String taskOutputJson(String id, String taskId, String argName) {
		return "{\"@type\":\"mdt:task_arg:task-output\",\"id\":\"" + id + "\",\"taskId\":\"" + taskId
				+ "\",\"argumentName\":\"" + argName + "\"}";
	}

	@Test
	public void parseJsonString_rejectsTrailingInput() throws IOException {
		// AS-03: 이전에는 첫 JSON 값 뒤의 입력을 무시했다.
		String json = taskOutputJson("in", "t", "out");
		for ( String tail: new String[] { " garbage", " {}", " []", " 1", " {\"x\"" } ) {
			Assertions.assertThrows(IOException.class, () -> ArgumentSpec.parseJsonString(json + tail), tail);
		}
		// 뒤의 공백·개행은 허용한다.
		Assertions.assertEquals(ArgumentSpec.taskOutput("in", "t", "out"), ArgumentSpec.parseJsonString(json + " \n\t"));
		// 빈 입력도 거부한다(Jackson의 입력 없음 예외).
		Assertions.assertThrows(IOException.class, () -> ArgumentSpec.parseJsonString(""));
	}

	@Test
	public void taskOutput_outputArgumentIdAndToString() {
		// AS-04: argumentName은 원래 태스크의 출력 인자 id다(getter 이름 변경).
		TaskOutputArgument spec = ArgumentSpec.taskOutput("in", "prev", "result");
		Assertions.assertEquals("result", spec.getOutputArgumentId());
		// AS-07: toString에 식별자를 넣는다.
		Assertions.assertEquals("in=task_output(prev:result)", spec.toString());
		Assertions.assertEquals("r=reference(param:i:x)", ArgumentSpec.reference("r", "param:i:x").toString());
		Assertions.assertEquals("a=literal(1)", ArgumentSpec.literal("a", 1).toString());
	}

	@Test
	public void nullElementValue_isReportedAsNull() {
		// value(ElementValue)에 null을 넘기면 편의 메소드와 같은 메시지를 낸다(이전에는 "value is not set").
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
															() -> ArgumentSpec.literal("a", (ElementValue)null));
		Assertions.assertEquals("value is null", e.getMessage());
		e = Assertions.assertThrows(IllegalArgumentException.class, () -> LiteralArgument.builder().id("a").build());
		Assertions.assertEquals("value is not set", e.getMessage());
	}
}
