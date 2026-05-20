package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mdt.model.sm.SubmodelUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class CompositionItemTest {
	@BeforeEach
	public void setup() {
	}
	
	@Test
	public void updateAasModel() throws Exception {
		DefaultCompositionItem item = new DefaultCompositionItem();
		item.setIdShort("idShort");
		item.setID("id");
		item.setReference("reference");
		
		SubmodelElementCollection smc = item.newSubmodelElement();
		String field;

		field = SubmodelUtils.getStringFieldById(smc, "ID");
		Assertions.assertEquals("id", field);
		field = SubmodelUtils.getStringFieldById(smc, "Reference");
		Assertions.assertEquals("reference", field);
		Assertions.assertFalse(SubmodelUtils.containsFieldById(smc, "Description"));
		
		SubmodelUtils.getFieldById(smc, "ID", Property.class).setValue("id2");
		SubmodelUtils.getFieldById(smc, "Reference", Property.class).setValue("reference2");
		item.updateFromAasModel(smc);
		
		Assertions.assertEquals("id2", item.getIdShort());
		Assertions.assertEquals("id2", item.getID());
		Assertions.assertEquals("reference2", item.getReference());
	}
	
	@Test
	public void updateAasModels() throws Exception {
		DefaultCompositionItem item1 = new DefaultCompositionItem();
		item1.setIdShort("item1");
		item1.setID("id1");
		item1.setReference("ref1");
		
		DefaultCompositionItem item2 = new DefaultCompositionItem();
		item2.setIdShort("item2");
		item2.setID("id2");
		item2.setReference("ref2");
	}
}
