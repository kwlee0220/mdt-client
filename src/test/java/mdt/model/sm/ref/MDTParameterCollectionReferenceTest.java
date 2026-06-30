package mdt.model.sm.ref;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.func.FOption;

import mdt.model.ModelValidationException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.info.MDTAssetType;

/**
 * {@link MDTParameterCollectionReference}의 단위 테스트.
 * <p>
 * 활성화가 필요 없는 직렬화/표현식/동치성과, mock {@link SubmodelService}로 제어하는
 * 읽기/쓰기 변환({@code ParameterID}/{@code ParameterValue} ↔ {@code idShort -> 값}) 및
 * idShort path 생성을 검증한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterCollectionReferenceTest {
	private static final String EQUIP_PATH = "DataInfo.Equipment.EquipmentParameterValues";

	private ObjectMapper m_mapper = new ObjectMapper();
	private SubmodelService m_svc;

	@BeforeEach
	public void setUp() {
		m_svc = mock(SubmodelService.class);
	}

	// --- 기본 속성 / 직렬화 / 동치성 ---

	@Test
	public void getSerializationType_isParamType() {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		Assertions.assertEquals("mdt:ref:param", ref.getSerializationType());
		Assertions.assertEquals("mdt:ref:param", MDTParameterCollectionReference.SERIALIZATION_TYPE);
	}

	@Test
	public void toStringExpr_isStar() {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		Assertions.assertEquals("param:inst:*", ref.toStringExpr());
	}

	@Test
	public void exprRoundTrip_preservesReference() {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		String expr = ref.toStringExpr();
		Assertions.assertEquals("param:inst:*", expr);

		ElementReference restored = ElementReferences.parseExpr(expr);
		Assertions.assertTrue(restored instanceof MDTParameterCollectionReference);
		Assertions.assertEquals(ref, restored);
	}

	@Test
	public void serialize_writesInstanceIdAndStarExpr() throws JsonProcessingException {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		String json = m_mapper.writeValueAsString(ref);
		Assertions.assertEquals(
				"{\"@type\":\"mdt:ref:param\",\"instanceId\":\"inst\",\"parameterExpr\":\"*\"}", json);
	}

	@Test
	public void deserialize_roundTrip() throws IOException {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		String json = m_mapper.writeValueAsString(ref);

		MDTParameterCollectionReference restored
				= m_mapper.readValue(json, MDTParameterCollectionReference.class);
		Assertions.assertEquals(ref, restored);
	}

	@Test
	public void deserializeFields_nonStarParameterExpr_throws() {
		ObjectNode node = m_mapper.createObjectNode();
		node.put("instanceId", "inst");
		node.put("parameterExpr", "Temperature");

		Assertions.assertThrows(IllegalArgumentException.class,
								() -> MDTParameterCollectionReference.deserializeFields(node));
	}

	@Test
	public void deserializeFields_missingInstanceId_throws() {
		ObjectNode node = m_mapper.createObjectNode();
		node.put("parameterExpr", "*");

		Assertions.assertThrows(IOException.class,
								() -> MDTParameterCollectionReference.deserializeFields(node));
	}

	@Test
	public void equals_sameInstanceId_areEqual() {
		MDTParameterCollectionReference r1 = MDTParameterCollectionReference.newInstance("inst");
		MDTParameterCollectionReference r2 = MDTParameterCollectionReference.newInstance("inst");
		Assertions.assertEquals(r1, r2);
		Assertions.assertEquals(r1.hashCode(), r2.hashCode());
	}

	@Test
	public void equals_differentInstanceIdOrType_areNotEqual() {
		MDTParameterCollectionReference base = MDTParameterCollectionReference.newInstance("inst");
		Assertions.assertNotEquals(base, MDTParameterCollectionReference.newInstance("other"));
		Assertions.assertNotEquals(base, null);
		Assertions.assertNotEquals(base, "not-a-reference");
	}

	// --- 첨부파일 미지원 ---

	@Test
	public void attachment_isUnsupported() {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		OutputStream out = mock(OutputStream.class);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> ref.readAttachment(out));
		Assertions.assertThrows(UnsupportedOperationException.class, () -> ref.removeAttachment());
	}

	// --- buildIdShortPath ---

	@Test
	public void buildIdShortPath_machine() {
		MDTParameterCollectionReference ref = activate(MDTAssetType.Machine);
		Assertions.assertEquals(EQUIP_PATH, ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_process() {
		MDTParameterCollectionReference ref = activate(MDTAssetType.Process);
		Assertions.assertEquals("DataInfo.Operation.OperationParameterValues", ref.getIdShortPathString());
	}

	@Test
	public void buildIdShortPath_nullAssetType_throwsModelValidation() {
		MDTInstanceManager manager = managerFor(mockInstance(null));
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		Assertions.assertThrows(ModelValidationException.class, () -> ref.activate(manager));
	}

	@Test
	public void buildIdShortPath_unsupportedAssetType_throwsIllegalArgument() {
		MDTInstanceManager manager = managerFor(mockInstance(MDTAssetType.Line));
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		Assertions.assertThrows(IllegalArgumentException.class, () -> ref.activate(manager));
	}

	// --- read / write 변환 ---

	/**
	 * {@code read()}는 각 멤버 SMC의 {@code ParameterValue}를 꺼내 idShort를 {@code ParameterID} 값으로
	 * 설정한 목록으로 변환해야 한다.
	 */
	@Test
	public void read_transformsMembersToValueList() throws IOException {
		MDTParameterCollectionReference ref = activate(MDTAssetType.Machine);
		when(m_svc.getSubmodelElementByPath(EQUIP_PATH)).thenReturn(parameterCollection("temp", "20"));

		SubmodelElement result = ref.read();
		Assertions.assertTrue(result instanceof SubmodelElementList);

		List<SubmodelElement> members = ((SubmodelElementList)result).getValue();
		Assertions.assertEquals(1, members.size());
		Assertions.assertEquals("temp", members.get(0).getIdShort());
	}

	/**
	 * {@code write()}는 원본 컬렉션을 읽어 각 {@code ParameterID}에 대응하는 새 값으로 {@code ParameterValue}를
	 * 교체한 뒤 다시 기록해야 한다.
	 */
	@Test
	public void write_replacesParameterValueAndWritesBack() throws IOException {
		MDTParameterCollectionReference ref = activate(MDTAssetType.Machine);
		SubmodelElementList raw = parameterCollection("temp", "20");
		when(m_svc.getSubmodelElementByPath(EQUIP_PATH)).thenReturn(raw);

		Property newValue = property("temp", "99");
		SubmodelElementList input = new DefaultSubmodelElementList.Builder()
											.idShort("EquipmentParameterValues")
											.value(List.of(newValue))
											.build();

		ref.write(input);
		verify(m_svc).setSubmodelElementByPath(EQUIP_PATH, raw);
	}

	// --- 헬퍼 ---

	private MDTInstance mockInstance(MDTAssetType assetType) {
		MDTInstance instance = mock(MDTInstance.class);
		when(instance.getAssetType()).thenReturn(assetType);
		when(instance.getSubmodelServiceByIdShort("Data")).thenReturn(FOption.of(m_svc));
		return instance;
	}

	private MDTInstanceManager managerFor(MDTInstance instance) {
		MDTInstanceManager manager = mock(MDTInstanceManager.class);
		when(manager.getInstance("inst")).thenReturn(instance);
		return manager;
	}

	private MDTParameterCollectionReference activate(MDTAssetType assetType) {
		MDTParameterCollectionReference ref = MDTParameterCollectionReference.newInstance("inst");
		ref.activate(managerFor(mockInstance(assetType)));
		return ref;
	}

	private static Property property(String idShort, String value) {
		return new DefaultProperty.Builder()
						.idShort(idShort)
						.valueType(DataTypeDefXsd.STRING)
						.value(value)
						.build();
	}

	/** {@code ParameterID}/{@code ParameterValue} 한 쌍을 가진 멤버 SMC 하나를 담은 컬렉션 목록을 만든다. */
	private static SubmodelElementList parameterCollection(String paramId, String value) {
		Property pid = property("ParameterID", paramId);
		Property pval = property("ParameterValue", value);
		SubmodelElement member = new DefaultSubmodelElementCollection.Builder()
											.idShort(paramId)
											.value(new ArrayList<>(List.of(pid, pval)))
											.build();
		return new DefaultSubmodelElementList.Builder()
						.idShort("EquipmentParameterValues")
						.value(List.of(member))
						.build();
	}
}
