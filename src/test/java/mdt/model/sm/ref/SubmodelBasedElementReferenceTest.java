package mdt.model.sm.ref;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.http.RESTfulRemoteException;

import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.FileValue;

/**
 * {@link SubmodelBasedElementReference}의 기본 동작 단위 테스트.
 * <p>
 * 이 클래스는 추상 클래스이므로, 고정된 idShort path를 돌려주는 최소 구현
 * ({@link TestElementReference})으로 테스트한다. 외부 의존인 {@link MDTSubmodelReference}와
 * {@link SubmodelService}는 mock으로 대체하여, 부모 클래스가 제공하는 활성화/조회 위임/prototype
 * 캐싱/{@code updateValue} 폴백 로직만 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelBasedElementReferenceTest {
	private static final String PATH = "DataInfo.Equipment.EquipmentID";

	private MDTSubmodelReference m_smRef;
	private SubmodelService m_svc;
	private MDTInstanceManager m_manager;

	@BeforeEach
	public void setUp() {
		m_smRef = mock(MDTSubmodelReference.class);
		m_svc = mock(SubmodelService.class);
		m_manager = mock(MDTInstanceManager.class);
		when(m_smRef.get()).thenReturn(m_svc);
	}

	// --- 활성화(activation) ---

	/**
	 * 활성화 전에는 idShort path가 확정되지 않았으므로 {@code isActivated()}가 {@code false}여야 한다.
	 */
	@Test
	public void notActivated_isActivatedReturnsFalse() {
		when(m_smRef.isActivated()).thenReturn(true);	// Submodel 참조는 활성화되었더라도

		TestElementReference ref = newRef(PATH);
		Assertions.assertFalse(ref.isActivated());		// idShort path가 없으면 비활성으로 간주
	}

	/**
	 * {@code activate()}는 Submodel 참조를 활성화하고 idShort path를 확정해야 한다.
	 */
	@Test
	public void activate_activatesSubmodelRefAndResolvesPath() {
		when(m_smRef.isActivated()).thenReturn(true);

		TestElementReference ref = newRef(PATH);
		ref.activate(m_manager);

		verify(m_smRef).activate(m_manager);
		Assertions.assertTrue(ref.isActivated());
		Assertions.assertEquals(PATH, ref.getIdShortPathString());
	}

	/**
	 * {@code activate(null)}은 {@link IllegalArgumentException}을 던져야 한다.
	 */
	@Test
	public void activate_nullManager_throws() {
		TestElementReference ref = newRef(PATH);
		Assertions.assertThrows(IllegalArgumentException.class, () -> ref.activate(null));
	}

	/**
	 * 하위 구현의 {@code buildIdShortPath()}가 {@code null}을 반환하면 {@link IllegalStateException}을
	 * 던져야 한다.
	 */
	@Test
	public void activate_buildIdShortPathReturnsNull_throws() {
		TestElementReference ref = newRef(null);
		Assertions.assertThrows(IllegalStateException.class, () -> ref.activate(m_manager));
	}

	/**
	 * 활성화 전에 {@code getIdShortPathString()}을 호출하면 {@link IllegalStateException}을 던져야 한다.
	 */
	@Test
	public void getIdShortPathString_beforeActivate_throws() {
		TestElementReference ref = newRef(PATH);
		Assertions.assertThrows(IllegalStateException.class, () -> ref.getIdShortPathString());
	}

	// --- 정보 조회 위임 ---

	@Test
	public void getInstanceId_delegatesToSubmodelRef() {
		when(m_smRef.getInstanceId()).thenReturn("inst-1");

		Assertions.assertEquals("inst-1", newRef(PATH).getInstanceId());
	}

	@Test
	public void getInstance_delegatesToSubmodelRef() {
		MDTInstance instance = mock(MDTInstance.class);
		when(m_smRef.getInstance()).thenReturn(instance);

		Assertions.assertSame(instance, newRef(PATH).getInstance());
	}

	@Test
	public void getSubmodelReference_returnsConstructorArgument() {
		Assertions.assertSame(m_smRef, newRef(PATH).getSubmodelReference());
	}

	@Test
	public void getSubmodelService_delegatesToSubmodelRef() {
		Assertions.assertSame(m_svc, newRef(PATH).getSubmodelService());
	}

	// --- 읽기/prototype ---

	/**
	 * {@code read()}는 서비스에서 SubmodelElement를 읽어 반환하고, 아직 로드되지 않은 prototype을
	 * 그 결과로 채워 추가 서버 조회를 막아야 한다.
	 */
	@Test
	public void read_returnsElementAndCachesPrototype() throws IOException {
		SubmodelElement sme = mock(SubmodelElement.class);
		when(m_svc.getSubmodelElementByPath(PATH)).thenReturn(sme);

		TestElementReference ref = activated();
		Assertions.assertSame(sme, ref.read());

		// read()가 prototype을 채웠으므로 getPrototype()은 추가 조회 없이 같은 객체를 돌려준다.
		Assertions.assertSame(sme, ref.getPrototype());
		verify(m_svc, times(1)).getSubmodelElementByPath(PATH);
	}

	/**
	 * {@code read()} 없이 {@code getPrototype()}을 먼저 호출하면 서비스에서 lazy 로딩해야 한다.
	 */
	@Test
	public void getPrototype_lazilyLoadsWhenNotRead() {
		SubmodelElement sme = mock(SubmodelElement.class);
		when(m_svc.getSubmodelElementByPath(PATH)).thenReturn(sme);

		TestElementReference ref = activated();
		Assertions.assertSame(sme, ref.getPrototype());
		verify(m_svc, times(1)).getSubmodelElementByPath(PATH);
	}

	/**
	 * {@code readValue()}는 prototype을 함께 넘겨 서비스의 값 조회를 위임해야 한다.
	 */
	@Test
	public void readValue_delegatesWithPrototype() throws IOException {
		SubmodelElement proto = mock(SubmodelElement.class);
		ElementValue value = mock(ElementValue.class);
		when(m_svc.getSubmodelElementByPath(PATH)).thenReturn(proto);
		when(m_svc.getSubmodelElementValueByPath(eq(PATH), any())).thenReturn(value);

		TestElementReference ref = activated();
		Assertions.assertSame(value, ref.readValue());
		verify(m_svc).getSubmodelElementValueByPath(PATH, proto);
	}

	// --- 쓰기/값 갱신 ---

	@Test
	public void write_delegatesToService() throws IOException {
		SubmodelElement sme = mock(SubmodelElement.class);

		TestElementReference ref = activated();
		ref.write(sme);
		verify(m_svc).setSubmodelElementByPath(PATH, sme);
	}

	/**
	 * {@code updateValue(ElementValue)}는 값의 JSON 표현으로 변환하여 문자열 버전에 위임해야 한다.
	 */
	@Test
	public void updateValue_elementValue_delegatesViaJsonString() throws IOException {
		ElementValue value = mock(ElementValue.class);
		when(value.toValueJsonString()).thenReturn("{\"v\":1}");

		TestElementReference ref = activated();
		ref.updateValue(value);
		verify(m_svc).updateSubmodelElementValueByPath(PATH, "{\"v\":1}");
	}

	@Test
	public void updateValue_string_delegatesToService() throws IOException {
		TestElementReference ref = activated();
		ref.updateValue("{\"v\":1}");
		verify(m_svc).updateSubmodelElementValueByPath(PATH, "{\"v\":1}");
	}

	/**
	 * FAST 버그 우회와 무관한 {@link RESTfulRemoteException}은 그대로 전파되어야 하며, 로컬 갱신
	 * (write)을 시도해서는 안 된다.
	 */
	@Test
	public void updateValue_string_rethrowsUnrelatedRemoteException() {
		String json = "{\"v\":1}";
		doThrow(new RESTfulRemoteException("some other server error"))
				.when(m_svc).updateSubmodelElementValueByPath(PATH, json);

		TestElementReference ref = activated();
		Assertions.assertThrows(RESTfulRemoteException.class, () -> ref.updateValue(json));
		verify(m_svc, never()).setSubmodelElementByPath(any(), any());
	}

	/**
	 * "no type information found" 메시지를 가진 {@link RESTfulRemoteException}이 발생하면, 전체
	 * SubmodelElement를 읽어 값을 갱신한 뒤 다시 쓰는 로컬 갱신으로 폴백해야 한다.
	 */
	@Test
	public void updateValue_string_fallsBackToLocalUpdateOnTypeInfoError() throws IOException {
		Property proto = new DefaultProperty.Builder()
								.idShort("EquipmentID")
								.valueType(DataTypeDefXsd.STRING)
								.value("old")
								.build();
		String json = "\"newval\"";
		doThrow(new RESTfulRemoteException("no type information found"))
				.when(m_svc).updateSubmodelElementValueByPath(PATH, json);
		when(m_svc.getSubmodelElementByPath(PATH)).thenReturn(proto);

		TestElementReference ref = activated();
		Assertions.assertDoesNotThrow(() -> ref.updateValue(json));

		// 폴백 경로에서 전체 요소를 읽어(read) 갱신한 뒤 다시 기록(write)해야 한다.
		verify(m_svc).getSubmodelElementByPath(PATH);
		verify(m_svc).setSubmodelElementByPath(PATH, proto);
		Assertions.assertEquals("newval", proto.getValue());
	}

	// --- 첨부파일 위임 ---

	@Test
	public void readAttachment_delegatesToService() throws IOException {
		OutputStream out = mock(OutputStream.class);

		TestElementReference ref = activated();
		ref.readAttachment(out);
		verify(m_svc).getAttachmentByPath(PATH, out);
	}

	@Test
	public void updateAttachment_delegatesToService() throws IOException {
		FileValue file = new FileValue("attachment.bin", "application/octet-stream");
		InputStream content = mock(InputStream.class);

		TestElementReference ref = activated();
		ref.updateAttachment(file, content);
		verify(m_svc).putAttachmentByPath(PATH, file, content);
	}

	@Test
	public void removeAttachment_delegatesToService() throws IOException {
		TestElementReference ref = activated();
		ref.removeAttachment();
		verify(m_svc).deleteAttachmentByPath(PATH);
	}

	// --- checkJsonField 헬퍼 ---

	@Test
	public void checkJsonField_returnsExistingField() throws IOException {
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("name", "value");

		JsonNode field = SubmodelBasedElementReference.checkJsonField(node, "name");
		Assertions.assertEquals("value", field.asText());
	}

	@Test
	public void checkJsonField_throwsWhenFieldMissing() {
		ObjectNode node = new ObjectMapper().createObjectNode();
		Assertions.assertThrows(IOException.class,
								() -> SubmodelBasedElementReference.checkJsonField(node, "missing"));
	}

	// --- 헬퍼 / 테스트용 구현 ---

	private TestElementReference newRef(String path) {
		return new TestElementReference(m_smRef, path);
	}

	private TestElementReference activated() {
		when(m_smRef.isActivated()).thenReturn(true);
		TestElementReference ref = newRef(PATH);
		ref.activate(m_manager);
		return ref;
	}

	/**
	 * 고정된 idShort path를 사용하는 최소 {@link SubmodelBasedElementReference} 구현.
	 */
	private static final class TestElementReference extends SubmodelBasedElementReference {
		private final String m_path;

		private TestElementReference(MDTSubmodelReference smRef, String path) {
			super(smRef);
			m_path = path;
		}

		@Override
		protected String buildIdShortPath() {
			return m_path;
		}

		@Override
		public String toStringExpr() {
			return "test:" + m_path;
		}

		@Override
		public String getSerializationType() {
			return "mdt:ref:test";
		}

		@Override
		public void serializeFields(JsonGenerator gen) throws IOException {
		}
	}
}
