package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utils.Indexed;

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
		Indexed<Property> field;

		field = SubmodelUtils.getPropertyById(smc, "ID");
		Assert.assertEquals("id", field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "Reference");
		Assert.assertEquals("reference", field.value().getValue());
		Assert.assertFalse(SubmodelUtils.containsFieldById(smc, "Description"));
		
		SubmodelUtils.getPropertyById(smc, "ID").value().setValue("id2");
		SubmodelUtils.getPropertyById(smc, "Reference").value().setValue("reference2");
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
