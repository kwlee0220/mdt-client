package mdt.model.sm.value;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdSubmodelReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ReferencesTest {
	private static final String SUBMODEL_ID = "http://customer.com/demo/sm/1234859590";

	@Test
	public void toSubmodelReference_singleKey() {
		Reference ref = newModelReference(KeyTypes.SUBMODEL, SUBMODEL_ID);

		ByIdSubmodelReference smRef = References.toSubmodelReference(ref);
		Assertions.assertEquals(SUBMODEL_ID, smRef.getSubmodelId());
	}

	@Test
	public void toSubmodelElementReference_twoKeys() {
		Reference ref = newModelReference(KeyTypes.SUBMODEL, SUBMODEL_ID,
											KeyTypes.PROPERTY, "Temperature");

		DefaultElementReference elmRef = References.toSubmodelElementReference(ref);
		Assertions.assertEquals("Temperature", elmRef.getIdShortPathString());
	}

	@Test
	public void toSubmodelElementReference_nestedKeys() {
		Reference ref = newModelReference(KeyTypes.SUBMODEL, SUBMODEL_ID,
											KeyTypes.SUBMODEL_ELEMENT_COLLECTION, "DataInfo",
											KeyTypes.PROPERTY, "Temperature");

		DefaultElementReference elmRef = References.toSubmodelElementReference(ref);
		Assertions.assertEquals("DataInfo.Temperature", elmRef.getIdShortPathString());
	}

	@Test
	public void toSubmodelElementReference_listIndex() {
		Reference ref = newModelReference(KeyTypes.SUBMODEL, SUBMODEL_ID,
											KeyTypes.SUBMODEL_ELEMENT_LIST, "Items",
											KeyTypes.PROPERTY, "0");

		DefaultElementReference elmRef = References.toSubmodelElementReference(ref);
		Assertions.assertEquals("Items[0]", elmRef.getIdShortPathString());
	}

	@Test
	public void toSubmodelElementReference_submodelOnly_throws() {
		Reference ref = newModelReference(KeyTypes.SUBMODEL, SUBMODEL_ID);

		Assertions.assertThrows(IllegalArgumentException.class,
								() -> References.toSubmodelElementReference(ref));
	}

	private static Reference newModelReference(Object... keyTypeValuePairs) {
		DefaultReference.Builder builder = new DefaultReference.Builder()
													.type(ReferenceTypes.MODEL_REFERENCE);
		for ( int i = 0; i < keyTypeValuePairs.length; i += 2 ) {
			builder.keys(new DefaultKey.Builder()
									.type((KeyTypes)keyTypeValuePairs[i])
									.value((String)keyTypeValuePairs[i+1])
									.build());
		}
		return builder.build();
	}
}
