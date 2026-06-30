package mdt.model.sm.ref;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import utils.func.FOption;

import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.model.sm.ref.OperationVariableReference.Kind;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationVariableReferenceTest {
	private ObjectMapper m_mapper = new ObjectMapper();

	private static final String JSON_STRING
		= "{\"@type\":\"mdt:ref:opvar\","
				+ "\"submodelReference\":{\"instanceId\":\"test\",\"submodelIdShort\":\"AddAndSleep\"},"
				+ "\"elementPath\":\"Operation\","
				+ "\"kind\":\"inout\",\"ordinal\":0}";
	private static final String EXPR = "opvar:test:AddAndSleep:Operation:out:1";
	
	@Test
	public void testSerializeOperationVariableReference() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		DefaultElementReference opRef = DefaultElementReference.newInstance(smRef, "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.INOUTPUT, 0);
		
		String json = m_mapper.writeValueAsString(ref);
		Assertions.assertEquals(JSON_STRING, json);
	}

	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException {
		OperationVariableReference ref = (OperationVariableReference)m_mapper.readValue(JSON_STRING, ElementReference.class);
		
		Assertions.assertEquals("test", ref.getInstanceId());
		DefaultElementReference opRef = (DefaultElementReference)ref.getOperationReference();
		Assertions.assertEquals(DefaultSubmodelReference.ofIdShort("test", "AddAndSleep"), opRef.getSubmodelReference());
		Assertions.assertEquals(Kind.INOUTPUT, ref.getVariableKind());
		Assertions.assertEquals(0, ref.getVariableOrdinal());
	}
	
	@Test
	public void testParseExpr() throws JsonProcessingException {
		MDTElementReference ref = ElementReferences.parseExpr(EXPR);
		
		Assertions.assertTrue(ref instanceof OperationVariableReference);
		
		OperationVariableReference varRef = (OperationVariableReference)ref;
		Assertions.assertEquals("test", ref.getInstanceId());
		Assertions.assertEquals(Kind.OUTPUT, varRef.getVariableKind());
		Assertions.assertEquals(1, varRef.getVariableOrdinal());
	}

	@Test
	public void testExpr() {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		DefaultElementReference opRef = DefaultElementReference.newInstance(smRef, "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.OUTPUT, 1);
		
		String expr = ref.toStringExpr();
		Assertions.assertEquals(EXPR, expr);
		
		ElementReference ref2 = ElementReferences.parseExpr(expr);
		Assertions.assertEquals(ref, ref2);
	}
	
	@Test
	public void testToStringExpr() throws JsonProcessingException {
		ByIdShortSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("test", "AddAndSleep");
		DefaultElementReference opRef = DefaultElementReference.newInstance(smRef, "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.OUTPUT, 1);
		Assertions.assertEquals(EXPR, ref.toStringExpr());
	}

	@Test
	public void jsonRoundTrip_preservesReference() throws JsonProcessingException {
		DefaultElementReference opRef = DefaultElementReference.newInstance(
				DefaultSubmodelReference.ofIdShort("test", "AddAndSleep"), "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.INOUTPUT, 0);
		String json = m_mapper.writeValueAsString(ref);
		OperationVariableReference restored = m_mapper.readValue(json, OperationVariableReference.class);
		Assertions.assertEquals(ref, restored);
	}

	@Test
	public void exprRoundTrip_preservesReference() {
		DefaultElementReference opRef = DefaultElementReference.newInstance(
				DefaultSubmodelReference.ofIdShort("test", "AddAndSleep"), "Operation");
		OperationVariableReference ref = OperationVariableReference.newInstance(opRef, Kind.OUTPUT, 1);
		Assertions.assertEquals(ref, ElementReferences.parseExpr(ref.toStringExpr()));
	}

	// --- Kind ---

	@Test
	public void kind_fromString_numeric() {
		Assertions.assertEquals(Kind.INPUT, Kind.fromString("0"));
		Assertions.assertEquals(Kind.OUTPUT, Kind.fromString("1"));
		Assertions.assertEquals(Kind.INOUTPUT, Kind.fromString("2"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> Kind.fromString("3"));
	}

	@Test
	public void kind_fromString_names() {
		Assertions.assertEquals(Kind.INPUT, Kind.fromString("in"));
		Assertions.assertEquals(Kind.OUTPUT, Kind.fromString("out"));
		Assertions.assertEquals(Kind.INOUTPUT, Kind.fromString("inout"));
		Assertions.assertNull(Kind.fromString("*"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> Kind.fromString("bogus"));
	}

	@Test
	public void kind_toString() {
		Assertions.assertEquals("in", Kind.INPUT.toString());
		Assertions.assertEquals("out", Kind.OUTPUT.toString());
		Assertions.assertEquals("inout", Kind.INOUTPUT.toString());
	}

	// --- 기본 속성 / 검증 ---

	@Test
	public void getSerializationType_isOpVarType() {
		OperationVariableReference ref = newRef(Kind.INPUT, 0);
		Assertions.assertEquals("mdt:ref:opvar", ref.getSerializationType());
		Assertions.assertEquals("mdt:ref:opvar", OperationVariableReference.SERIALIZATION_TYPE);
	}

	@Test
	public void accessors_returnConstructorArguments() {
		OperationVariableReference ref = newRef(Kind.OUTPUT, 2);
		Assertions.assertEquals(Kind.OUTPUT, ref.getVariableKind());
		Assertions.assertEquals(2, ref.getVariableOrdinal());
		Assertions.assertTrue(ref.getOperationReference() instanceof DefaultElementReference);
	}

	@Test
	public void getIdShortPathString_isOperation() {
		OperationVariableReference ref = newRef(Kind.INPUT, 0);
		Assertions.assertEquals("Operation", ref.getIdShortPathString());
	}

	@Test
	public void newInstance_nullKind_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> OperationVariableReference.newInstance(opRef(), null, 0));
	}

	@Test
	public void newInstance_negativeOrdinal_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> OperationVariableReference.newInstance(opRef(), Kind.INPUT, -1));
	}

	@Test
	public void newInstance_nullSubmodelRef_throws() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> OperationVariableReference.newInstance(null, Kind.INPUT, 0));
	}

	// --- equals / hashCode ---

	@Test
	public void equals_sameValues_areEqual() {
		OperationVariableReference r1 = newRef(Kind.INPUT, 0);
		OperationVariableReference r2 = newRef(Kind.INPUT, 0);
		Assertions.assertEquals(r1, r2);
		Assertions.assertEquals(r1.hashCode(), r2.hashCode());
	}

	@Test
	public void equals_differentFields_areNotEqual() {
		OperationVariableReference base = newRef(Kind.INPUT, 0);
		Assertions.assertNotEquals(base, newRef(Kind.OUTPUT, 0));
		Assertions.assertNotEquals(base, newRef(Kind.INPUT, 1));
		Assertions.assertNotEquals(base,
				OperationVariableReference.newInstance(
						DefaultElementReference.newInstance(DefaultSubmodelReference.ofIdShort("other", "Op"), "Operation"),
						Kind.INPUT, 0));
		Assertions.assertNotEquals(base, null);
		Assertions.assertNotEquals(base, "not-a-reference");
	}

	// --- read / write ---

	@Test
	public void read_returnsTargetVariableValue() throws IOException {
		Property val = property("in0", "v");
		Operation op = operationWithInput(val);
		when(m_svc.getSubmodelElementByPath("Operation")).thenReturn(op);

		OperationVariableReference ref = activate(Kind.INPUT, 0);
		Assertions.assertSame(val, ref.read());
	}

	@Test
	public void read_notActivated_throws() {
		OperationVariableReference ref = newRef(Kind.INPUT, 0);
		Assertions.assertThrows(IllegalStateException.class, () -> ref.read());
	}

	@Test
	public void read_targetNotOperation_throws() {
		when(m_svc.getSubmodelElementByPath("Operation")).thenReturn(property("x", "v"));

		OperationVariableReference ref = activate(Kind.INPUT, 0);
		Assertions.assertThrows(IllegalStateException.class, () -> ref.read());
	}

	@Test
	public void read_ordinalOutOfRange_throws() {
		Operation op = operationWithInput(property("in0", "v"));
		when(m_svc.getSubmodelElementByPath("Operation")).thenReturn(op);

		OperationVariableReference ref = activate(Kind.INPUT, 5);
		Assertions.assertThrows(IllegalArgumentException.class, () -> ref.read());
	}

	@Test
	public void write_setsVariableValueAndWritesBack() throws IOException {
		Operation op = operationWithInput(property("in0", "old"));
		when(m_svc.getSubmodelElementByPath("Operation")).thenReturn(op);

		OperationVariableReference ref = activate(Kind.INPUT, 0);
		Property newValue = property("in0", "new");
		ref.write(newValue);

		Assertions.assertSame(newValue, op.getInputVariables().get(0).getValue());
		verify(m_svc).setSubmodelElementByPath("Operation", op);
	}

	// --- 헬퍼 ---

	private SubmodelService m_svc;

	@BeforeEach
	public void setUp() {
		m_svc = mock(SubmodelService.class);
	}

	private static DefaultElementReference opRef() {
		MDTSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inst", "testSm");
		return DefaultElementReference.newInstance(smRef, "Operation");
	}

	private static OperationVariableReference newRef(Kind kind, int ordinal) {
		return OperationVariableReference.newInstance(opRef(), kind, ordinal);
	}

	private OperationVariableReference activate(Kind kind, int ordinal) {
		MDTInstance instance = mock(MDTInstance.class);
		when(instance.getSubmodelServiceByIdShort("testSm")).thenReturn(FOption.of(m_svc));
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		when(manager.getInstance("inst")).thenReturn(instance);

		OperationVariableReference ref = newRef(kind, ordinal);
		ref.activate(manager);
		return ref;
	}

	private static Property property(String idShort, String value) {
		return new DefaultProperty.Builder()
						.idShort(idShort)
						.valueType(DataTypeDefXsd.STRING)
						.value(value)
						.build();
	}

	private static Operation operationWithInput(SubmodelElement value) {
		OperationVariable opv = new DefaultOperationVariable.Builder().value(value).build();
		Operation op = new DefaultOperation.Builder().idShort("Operation").build();
		op.setInputVariables(new ArrayList<>(List.of(opv)));
		return op;
	}
}
