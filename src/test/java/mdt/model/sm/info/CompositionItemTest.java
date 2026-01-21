package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mdt.model.sm.SubmodelUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class CompositionItemTest {
	@Before
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
		Assert.assertEquals("id", field);
		field = SubmodelUtils.getStringFieldById(smc, "Reference");
		Assert.assertEquals("reference", field);
		Assert.assertFalse(SubmodelUtils.containsFieldById(smc, "Description"));
		
		SubmodelUtils.getPropertyFieldById(smc, "ID").setValue("id2");
		SubmodelUtils.getPropertyFieldById(smc, "Reference").setValue("reference2");
		item.updateFromAasModel(smc);
		
		Assert.assertEquals("id2", item.getIdShort());
		Assert.assertEquals("id2", item.getID());
		Assert.assertEquals("reference2", item.getReference());
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
