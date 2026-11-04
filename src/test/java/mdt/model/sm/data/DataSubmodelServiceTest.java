package mdt.model.sm.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.junit.jupiter.api.Test;

import mdt.model.SubmodelService;


/**
 * {@link DataSubmodelService} 생성자의 인자 검사(mdt-client API 검토 D4, 2026-11-03).
 * <p>
 * 2026-11-03 이전에는 semantic id가 없는 서브모델에서 {@link IllegalArgumentException} 대신
 * {@link NullPointerException}이 났다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DataSubmodelServiceTest {
	private static SubmodelService serviceOf(DefaultSubmodel submodel) {
		SubmodelService service = mock(SubmodelService.class);
		when(service.getSubmodel()).thenReturn(submodel);
		return service;
	}

	@Test
	public void submodelWithoutSemanticIdIsIllegalArgument() {
		DefaultSubmodel submodel = new DefaultSubmodel.Builder().id("urn:sm:1").idShort("X").build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
													() -> new DataSubmodelService(serviceOf(submodel)));
		assertEquals("Not Data Submodel: id=urn:sm:1, semanticId=null", e.getMessage());
	}

	@Test
	public void submodelWithOtherSemanticIdIsIllegalArgument() {
		DefaultSubmodel submodel = new DefaultSubmodel.Builder()
										.id("urn:sm:2")
										.semanticId(new DefaultReference.Builder()
															.type(ReferenceTypes.EXTERNAL_REFERENCE)
															.keys(new DefaultKey.Builder().type(KeyTypes.GLOBAL_REFERENCE)
																						.value("urn:other").build())
															.build())
										.build();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
													() -> new DataSubmodelService(serviceOf(submodel)));
		assertEquals(true, e.getMessage().startsWith("Not Data Submodel: id=urn:sm:2, semanticId="), e.getMessage());
	}

	@Test
	public void nullServiceIsIllegalArgument() {
		assertThrows(IllegalArgumentException.class, () -> new DataSubmodelService(null));
	}
}
