package mdt.model.sm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEntity;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.jupiter.api.Test;

import mdt.model.instance.AASOperationDescriptor;


public class SubmodelUtilsWalkTest {
	private static Operation op(String idShort, String... displayNames) {
		var builder = new DefaultOperation.Builder().idShort(idShort);
		if ( displayNames.length > 0 ) {
			builder.displayName(List.of(displayNames).stream()
								.map(text -> (org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType)
										new DefaultLangStringNameType.Builder().language("ko").text(text).build())
								.toList());
		}
		return builder.build();
	}

	private static SubmodelElement prop(String idShort) {
		return new DefaultProperty.Builder().idShort(idShort).value("1").build();
	}

	private static Submodel sampleSubmodel() {
		var listMember = new DefaultSubmodelElementCollection.Builder()
								.value(List.of(op("Inner")))
								.build();
		var list = new DefaultSubmodelElementList.Builder()
								.idShort("List")
								.value(List.of(op(null), listMember))
								.build();
		var smc = new DefaultSubmodelElementCollection.Builder()
								.idShort("Coll")
								.value(List.of(prop("P1"), op("Run", "실행", "Run"), list))
								.build();
		var entity = new DefaultEntity.Builder()
								.idShort("Ent")
								.statements(List.of(op("Stop")))
								.build();
		return new DefaultSubmodel.Builder()
								.id("urn:test:sm")
								.idShort("Control")
								.submodelElements(List.of(op("Top"), smc, entity, prop("P2")))
								.build();
	}

	@Test
	public void walkVisitsAllElementsDepthFirst() {
		List<String> paths = SubmodelUtils.walk(sampleSubmodel()).map(kv -> kv.key()).toList();
		assertEquals(List.of("Top",
							"Coll", "Coll.P1", "Coll.Run", "Coll.List",
							"Coll.List[0]", "Coll.List[1]", "Coll.List[1].Inner",
							"Ent", "Ent.Stop",
							"P2"), paths);
	}

	@Test
	public void walkEmptySubmodel() {
		Submodel sm = new DefaultSubmodel.Builder().id("urn:test:empty").idShort("Empty").build();
		assertTrue(SubmodelUtils.walk(sm).toList().isEmpty());
	}

	@Test
	public void collectAASOperations() {
		Submodel sm = sampleSubmodel();
		List<AASOperationDescriptor> descList
				= SubmodelUtils.walk(sm)
								.filter(kv -> kv.value() instanceof Operation)
								.map(kv -> AASOperationDescriptor.from(sm.getIdShort(), kv.key(),
																		(Operation)kv.value()))
								.toList();
		assertEquals(List.of("Top", "Coll.Run", "Coll.List[0]", "Coll.List[1].Inner", "Ent.Stop"),
					descList.stream().map(AASOperationDescriptor::path).toList());
		descList.forEach(desc -> assertEquals("Control", desc.submodel()));

		AASOperationDescriptor run = descList.get(1);
		assertEquals("Run", run.idShort());
		assertEquals("실행", run.displayName());		// 여러 개인 경우는 첫번째 값

		AASOperationDescriptor listMember = descList.get(2);
		assertNull(listMember.idShort());
		assertNull(listMember.displayName());
	}
}
