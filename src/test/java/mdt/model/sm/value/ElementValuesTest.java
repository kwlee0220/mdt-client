package mdt.model.sm.value;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import utils.json.JacksonDeserializationException;

import mdt.model.MDTModelSerDe;

/**
 * {@link ElementValues}의 {@code @type}/{@code value} 형태 polymorphic JSON 역직렬화에 대한 단위 테스트.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ElementValuesTest {
	private static final String INT_TYPE = PropertyValue.IntegerPropertyValue.SERIALIZATION_TYPE;

	private static JsonNode tree(String json) throws IOException {
		return MDTModelSerDe.getJsonMapper().readTree(json);
	}

	@Test
	public void parseJsonNode_roundTrip() throws IOException {
		ElementValue value = PropertyValue.INTEGER(17);
		ElementValue restored = ElementValues.parseJsonString(MDTModelSerDe.getJsonMapper().writeValueAsString(value));
		Assertions.assertEquals(value, restored);
	}

	@Test
	public void parseJsonNode_missingType_throws() {
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> ElementValues.parseJsonNode(tree("{\"value\":1}")));
		Assertions.assertEquals("'@type' field is missing: base=ElementValue, available fields=[value]",
								e.getMessage());
	}

	@Test
	public void parseJsonNode_missingValue_throws() {
		String json = "{\"@type\":\"" + INT_TYPE + "\"}";
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
														() -> ElementValues.parseJsonNode(tree(json)));
		Assertions.assertEquals("'value' field is missing: type=" + INT_TYPE, e.getMessage());
	}

	@Test
	public void parseJsonNode_unregisteredType_throws() {
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
						() -> ElementValues.parseJsonNode(tree("{\"@type\":\"mdt:value:bogus\",\"value\":1}")));
		// 등록 타입 목록의 순서는 고정되어 있지 않으므로 앞부분만 비교한다.
		Assertions.assertTrue(e.getMessage().startsWith("Unregistered ElementValue type: mdt:value:bogus"),
								e.getMessage());
	}

	@Test
	public void parseJsonNode_passesJsonNullValueToTheType() throws IOException {
		// 'value'가 JSON null이면 필드 누락으로 보지 않고 해당 타입의 deserializeValue에 그대로 넘긴다.
		String json = "{\"@type\":\"" + PropertyValue.StringPropertyValue.SERIALIZATION_TYPE + "\",\"value\":null}";
		ElementValue restored = ElementValues.parseJsonNode(tree(json));
		Assertions.assertInstanceOf(PropertyValue.StringPropertyValue.class, restored);
	}

	@Test
	public void parseJsonString_rejectsTrailingInput() throws IOException {
		// 이전에는 첫 JSON 값 뒤의 입력을 무시했다.
		String json = MDTModelSerDe.getJsonMapper().writeValueAsString(PropertyValue.INTEGER(17));
		Assertions.assertThrows(IOException.class, () -> ElementValues.parseJsonString(json + " garbage"));
		Assertions.assertEquals(PropertyValue.INTEGER(17), ElementValues.parseJsonString(json + " "));
	}

	@Test
	public void parseJsonString_rejectsLossyPropertyValues() {
		// 이전에는 "abc"→0, 3.9→3, 넘침, "yes"→false로 오류 없이 바꿨다.
		for ( String json: new String[] {
				"{\"@type\":\"mdt:value:integer\",\"value\":\"abc\"}",
				"{\"@type\":\"mdt:value:integer\",\"value\":3.9}",
				"{\"@type\":\"mdt:value:integer\",\"value\":3000000000}",
				"{\"@type\":\"mdt:value:boolean\",\"value\":\"yes\"}" } ) {
			Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementValues.parseJsonString(json), json);
		}
	}

	@Test
	public void updateFromValueJson_rejectsLossyValue() throws IOException {
		org.eclipse.digitaltwin.aas4j.v3.model.Property prop
			= new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty.Builder()
					.idShort("p").valueType(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT).value("1").build();
		// 이전에는 정수 속성이 0으로 바뀌었다.
		Assertions.assertThrows(JacksonDeserializationException.class, () -> ElementValues.updateFromValueJson(prop, tree("\"abc\"")));
		Assertions.assertEquals("1", prop.getValue());
		ElementValues.updateFromValueJson(prop, tree("\"42\""));
		Assertions.assertEquals("42", prop.getValue());
	}

	// --- 속성의 데이터 타입 확인 ---

	private static org.eclipse.digitaltwin.aas4j.v3.model.Property prop(
										org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd type, String value) {
		return new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty.Builder()
					.idShort("p").valueType(type).value(value).build();
	}

	@Test
	public void update_rejectsTypeMismatch() {
		var p = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "1");
		// 이전에는 xs:int 속성에 "abc", 3.7, true가 그대로 기록됐다.
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
														() -> ElementValues.update(p, PropertyValue.STRING("abc")));
		Assertions.assertEquals("value type mismatch: property 'p' is INT, but value is STRING", e.getMessage());
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.update(p, PropertyValue.DOUBLE(3.7)));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.update(p, PropertyValue.BOOLEAN(true)));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.update(p, PropertyValue.LONG(3000000000L)));
		Assertions.assertEquals("1", p.getValue());
	}

	@Test
	public void update_allowsLosslessNumericConversion() throws IOException {
		var longProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.LONG, "0");
		ElementValues.update(longProp, PropertyValue.INTEGER(42));
		Assertions.assertEquals("42", longProp.getValue());

		var intProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "0");
		ElementValues.update(intProp, PropertyValue.DOUBLE(3.0));
		Assertions.assertEquals("3", intProp.getValue());
		ElementValues.updateValue(intProp, 5);				// update(sme, Integer)
		Assertions.assertEquals("5", intProp.getValue());

		// DataTypes에 없는 xs:integer도 정수면 받는다(모델에 실제로 쓰인다).
		var integerProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INTEGER, "0");
		ElementValues.update(integerProp, PropertyValue.LONG(12345678901L));
		Assertions.assertEquals("12345678901", integerProp.getValue());

		var doubleProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DOUBLE, "0");
		ElementValues.update(doubleProp, PropertyValue.INTEGER(7));
		Assertions.assertEquals("7.0", doubleProp.getValue());

		// 문자열 속성에는 어떤 값이든 값 문자열로 기록한다(예: OPC UA에서 읽은 double).
		var stringProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.STRING, "");
		ElementValues.update(stringProp, PropertyValue.DOUBLE(3.7));
		Assertions.assertEquals("3.7", stringProp.getValue());
	}

	@Test
	public void updateWithString_interpretsTargetType() throws IOException {
		var intProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "0");
		ElementValues.updateFromValueString(intProp, "42");
		Assertions.assertEquals("42", intProp.getValue());
		// 이전에는 "abc"가 그대로 기록됐다.
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(intProp, "abc"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(intProp, "3.7"));
		Assertions.assertEquals("42", intProp.getValue());

		// 원래 문자열을 그대로 기록한다(시간대 표기를 바꾸지 않음).
		var dtProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DATE_TIME, null);
		ElementValues.updateFromValueString(dtProp, "2026-11-01T09:00:00+09:00");
		Assertions.assertEquals("2026-11-01T09:00:00+09:00", dtProp.getValue());
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(dtProp, "tomorrow"));

		// PropertyValue가 없는 xs:integer·xs:date도 해석해서 확인한다.
		var integerProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INTEGER, "0");
		ElementValues.updateFromValueString(integerProp, "123456789012345678901");
		Assertions.assertEquals("123456789012345678901", integerProp.getValue());
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(integerProp, "1.5"));
		// 이전에는 "3.0"이 통과해 xs:integer 값으로 그대로 저장됐다. xs:int처럼 정수 문법만 받는다.
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(integerProp, "3.0"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(integerProp, " 3 "));
		Assertions.assertEquals("123456789012345678901", integerProp.getValue());
		var byteProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.BYTE, "0");
		ElementValues.updateFromValueString(byteProp, "-128");
		Assertions.assertEquals("-128", byteProp.getValue());
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(byteProp, "128"));
		var dateProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DATE, null);
		ElementValues.updateFromValueString(dateProp, "2026-11-01");
		Assertions.assertEquals("2026-11-01", dateProp.getValue());

		var stringProp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.STRING, "");
		ElementValues.updateFromValueString(stringProp, "abc");
		Assertions.assertEquals("abc", stringProp.getValue());
	}

	// --- null 인자 ---

	@Test
	public void update_rejectsNullArguments() {
		var p = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "1");
		// 이전에는 오류 메시지를 만들다 NullPointerException이 났다.
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
														() -> ElementValues.update(p, (ElementValue)null));
		Assertions.assertEquals("ElementValue is null", e.getMessage());
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> ElementValues.update(null, PropertyValue.INTEGER(1)));
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> ElementValues.updateFromValueJson(p, (com.fasterxml.jackson.databind.JsonNode)null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueString(null, "1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.updateFromValueJson(p, (String)null));
		Assertions.assertEquals("1", p.getValue());
	}

	@Test
	public void update_nullValueClearsProperty() throws IOException {
		// null '값'은 의미가 있으므로 계속 받는다: 문자열·숫자 overload의 null, JSON null.
		var p = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "1");
		ElementValues.updateFromValueString(p, (String)null);
		Assertions.assertNull(p.getValue());
		ElementValues.updateValue(p, 2);
		ElementValues.updateValue(p, (Integer)null);
		Assertions.assertNull(p.getValue());
		ElementValues.updateValue(p, 3);
		ElementValues.updateFromValueJson(p, com.fasterxml.jackson.databind.node.NullNode.getInstance());
		Assertions.assertNull(p.getValue());
	}

	// --- parseValueJsonString의 후행 입력 ---

	@Test
	public void parseValueJsonString_rejectsTrailingInput() throws IOException {
		// 이전에는 첫 JSON 값만 읽고 나머지를 무시했다("5 garbage" → 5).
		for ( String json: new String[] { "5 garbage", "3.5 sec", "5 6", "\"a\" {}" } ) {
			Assertions.assertThrows(IOException.class,
									() -> ElementValues.parseValueJsonString(json, PropertyValue.INTEGER(1)), json);
		}
		// 뒤의 공백·개행은 허용한다(파일에서 읽은 값).
		Assertions.assertEquals(PropertyValue.INTEGER(5), ElementValues.parseValueJsonString("5\n", PropertyValue.INTEGER(1)));
		// updateWithValueJsonString도 같은 경로를 거친다.
		var p = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "1");
		Assertions.assertThrows(IOException.class, () -> ElementValues.updateFromValueJson(p, "7 garbage"));
		Assertions.assertEquals("1", p.getValue());
		ElementValues.updateFromValueJson(p, " 7 ");
		Assertions.assertEquals("7", p.getValue());
	}

	// --- parseJsonNode(node, proto)의 @type 검사 (이전 이름 parseGeneralJsonNode) ---

	@Test
	public void parseGeneralJsonNode_typeField() throws IOException {
		ElementValue proto = PropertyValue.INTEGER(1);
		// @type이 있으면 그 타입, 없으면 proto의 타입을 따른다.
		Assertions.assertEquals(PropertyValue.STRING("x"),
				ElementValues.parseJsonNode(tree("{\"@type\":\"mdt:value:string\",\"value\":\"x\"}"), proto));
		Assertions.assertEquals(PropertyValue.INTEGER(7), ElementValues.parseJsonNode(tree("7"), proto));

		// 이전에는 숫자 @type을 "5"로 읽었고, 모르는 타입은 IllegalArgumentException, 빈 문자열은 '없음'이었다.
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
						() -> ElementValues.parseJsonNode(tree("{\"@type\":5,\"value\":1}"), proto));
		Assertions.assertEquals("invalid field '@type': string expected, but number", e.getMessage());
		e = Assertions.assertThrows(JacksonDeserializationException.class,
						() -> ElementValues.parseJsonNode(tree("{\"@type\":\"foo\",\"value\":1}"), proto));
		Assertions.assertTrue(e.getMessage().startsWith("Unregistered ElementValue type: foo"), e.getMessage());
		e = Assertions.assertThrows(JacksonDeserializationException.class,
						() -> ElementValues.parseJsonNode(tree("{\"@type\":\"\",\"value\":1}"), proto));
		Assertions.assertEquals("invalid field '@type': blank", e.getMessage());
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.parseJsonNode(null, proto));
	}

	// --- 갱신 메소드 이름의 의미 ---

	@Test
	public void updateMethods_distinguishValueStringJsonAndNumber() throws IOException {
		var sp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.STRING, "");
		// AAS 값 문자열: 따옴표 없이 그대로.
		ElementValues.updateFromValueString(sp, "abc");
		Assertions.assertEquals("abc", sp.getValue());
		// value-only JSON: 문자열은 JSON 문자열이어야 한다.
		ElementValues.updateFromValueJson(sp, "\"xyz\"");
		Assertions.assertEquals("xyz", sp.getValue());
		Assertions.assertThrows(IOException.class, () -> ElementValues.updateFromValueJson(sp, "abc"));

		var lp = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.LONG, "0");
		// Java 숫자: Long·BigDecimal도 받는다(이전 overload는 Integer·Float·Double뿐).
		ElementValues.updateValue(lp, 3000000000L);
		Assertions.assertEquals("3000000000", lp.getValue());
		ElementValues.updateValue(lp, new java.math.BigDecimal("42"));
		Assertions.assertEquals("42", lp.getValue());
		Assertions.assertThrows(IllegalArgumentException.class,
								() -> ElementValues.updateValue(lp, new java.math.BigDecimal("4.2")));
		// JSON 노드로도 갱신한다.
		ElementValues.updateFromValueJson(lp, tree("7"));
		Assertions.assertEquals("7", lp.getValue());
	}

	// --- Range 타입 확인 ---

	private static org.eclipse.digitaltwin.aas4j.v3.model.Range range(
										org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd type) {
		return new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange.Builder()
					.idShort("r").valueType(type).min("0").max("1").build();
	}

	@Test
	public void updateRange_rejectsTypeMismatch() {
		var rg = range(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT);
		// 이전에는 xs:int Range에 min=abc, max=xyz가 그대로 기록됐다.
		var strRange = new RangeValue<>(mdt.aas.DataTypes.STRING, "abc", "xyz");
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
														() -> ElementValues.update(rg, strRange));
		Assertions.assertEquals("value type mismatch: range 'r' is INT, but value is STRING", e.getMessage());
		// max만 손실이 있어도 min까지 바뀌지 않는다.
		var lossy = new RangeValue<>(mdt.aas.DataTypes.DOUBLE, 2.0, 3.5);
		Assertions.assertThrows(IllegalArgumentException.class, () -> ElementValues.update(rg, lossy));
		Assertions.assertEquals("0", rg.getMin());
		Assertions.assertEquals("1", rg.getMax());
	}

	@Test
	public void updateRange_allowsLosslessNumericAndString() {
		var rg = range(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.LONG);
		ElementValues.update(rg, new RangeValue<>(mdt.aas.DataTypes.INT, 1, null));
		Assertions.assertEquals("1", rg.getMin());
		Assertions.assertNull(rg.getMax());

		var strRg = range(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.STRING);
		ElementValues.update(strRg, new RangeValue<>(mdt.aas.DataTypes.DOUBLE, 1.5, 2.5));
		Assertions.assertEquals("1.5", strRg.getMin());
	}

	// --- List/Collection 노드 종류 확인 ---

	@Test
	public void parseJsonNode_listOrCollection_rejectsWrongNodeKind() {
		String list = ElementListValue.SERIALIZATION_TYPE;
		String coll = ElementCollectionValue.SERIALIZATION_TYPE;
		// 이전에는 모두 빈 리스트·컬렉션이 됐다.
		JacksonDeserializationException e = Assertions.assertThrows(JacksonDeserializationException.class,
					() -> ElementValues.parseJsonNode(tree("{\"@type\":\"" + list + "\",\"value\":42}")));
		Assertions.assertTrue(e.getMessage().contains("array expected, but number"), e.getMessage());
		Assertions.assertThrows(JacksonDeserializationException.class,
					() -> ElementValues.parseJsonNode(tree("{\"@type\":\"" + list + "\",\"value\":null}")));
		e = Assertions.assertThrows(JacksonDeserializationException.class,
					() -> ElementValues.parseJsonNode(tree("{\"@type\":\"" + coll + "\",\"value\":[]}")));
		Assertions.assertTrue(e.getMessage().contains("object expected, but array"), e.getMessage());
		Assertions.assertThrows(JacksonDeserializationException.class,
					() -> ElementValues.parseJsonNode(tree("{\"@type\":\"" + coll + "\",\"value\":null}")));
	}

	// --- List/Operation 갱신 사전 확인 ---

	@Test
	public void updateList_checksBeforeWriting() {
		var p1 = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "0");
		var p2 = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "0");
		var sml = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList.Builder()
						.idShort("l").value(java.util.List.of(p1, p2)).build();
		var withNull = new ElementListValue(java.util.Arrays.asList(PropertyValue.INTEGER(1), null));
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
														() -> ElementValues.update(sml, withNull));
		Assertions.assertEquals("element value is null: index=1", e.getMessage());
		Assertions.assertEquals("0", p1.getValue());
	}

	@Test
	public void updateOperation_checksAllCountsBeforeWriting() {
		var in = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "0");
		var out = prop(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT, "0");
		var op = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation.Builder()
					.idShort("op")
					.inputVariables(new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable.Builder().value(in).build())
					.outputVariables(new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable.Builder().value(out).build())
					.build();
		// output 개수가 틀리면 input도 바뀌지 않는다(이전에는 input이 먼저 기록됐다).
		var value = new OperationVariableValue(java.util.List.of(PropertyValue.INTEGER(1)), java.util.List.of(),
												java.util.List.of());
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
														() -> ElementValues.update(op, value));
		Assertions.assertEquals("output variable count mismatch: values.size=0, target.size=1", e.getMessage());
		Assertions.assertEquals("0", in.getValue());
	}

	// --- Collection의 없는 필드·JSON null 구분 ---

	private static org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection smc(
										org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement... members) {
		return new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection.Builder()
					.idShort("c").value(java.util.List.of(members)).build();
	}

	private static org.eclipse.digitaltwin.aas4j.v3.model.Property prop(String idShort, String value) {
		return new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty.Builder()
					.idShort(idShort).valueType(org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT)
					.value(value).build();
	}

	@Test
	public void updateCollection_missingFieldIsKept_jsonNullClears() throws IOException {
		var a = prop("a", "1");
		var b = prop("b", "2");
		var c = smc(a, b);
		// 없는 필드(b)는 그대로 둔다(부분 갱신).
		ElementValues.updateFromValueJson(c, tree("{\"a\":10}"));
		Assertions.assertEquals("10", a.getValue());
		Assertions.assertEquals("2", b.getValue());
		// JSON null 필드(b)는 값을 지운다. 이전에는 조용히 무시됐다.
		ElementValues.updateFromValueJson(c, tree("{\"b\":null}"));
		Assertions.assertEquals("10", a.getValue());
		Assertions.assertNull(b.getValue());
	}

	@Test
	public void parseCollection_distinguishesMissingAndNull() throws IOException {
		var template = ElementCollectionValue.from(smc(prop("a", "1"), prop("b", "2")));
		var parsed = (ElementCollectionValue)ElementValues.parseValueJsonNode(tree("{\"b\":null}"), template);
		Assertions.assertFalse(parsed.containsField("a"));
		Assertions.assertEquals(PropertyValue.INTEGER(null), parsed.getFieldMap().get("b"));
	}

	@Test
	public void parseCollection_jsonNullForNonClearableField_throws() throws IOException {
		var inner = smc(prop("x", "1"));
		var outer = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection.Builder()
						.idShort("o").value(java.util.List.of(prop("a", "1"), inner)).build();
		inner.setIdShort("in");
		Assertions.assertThrows(IOException.class,
								() -> ElementValues.updateFromValueJson(outer, tree("{\"a\":5,\"in\":null}")));
		Assertions.assertEquals("1", ((org.eclipse.digitaltwin.aas4j.v3.model.Property)outer.getValue().get(0)).getValue());
	}

	@Test
	public void updateCollection_nullFieldValue_checkedBeforeWriting() {
		var a = prop("a", "1");
		var b = prop("b", "2");
		var fields = new java.util.LinkedHashMap<String,ElementValue>();
		fields.put("a", PropertyValue.INTEGER(10));
		fields.put("b", null);
		IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
								() -> ElementValues.update(smc(a, b), new ElementCollectionValue(fields)));
		Assertions.assertEquals("field value is null: idShort=b", e.getMessage());
		Assertions.assertEquals("1", a.getValue());
	}
}
