package mdt.model.sm.info;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utils.Indexed;

import mdt.model.sm.SubmodelUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class TwinCompositionTest {
	@Before
	public void setup() {
	}
	
	@Test
	public void updateAasModel() throws Exception {
		DefaultTwinComposition info = new DefaultTwinComposition();
		info.setCompositionID("compositionId");
		info.setCompositionType("compositionType");
		
		DefaultCompositionItem item1 = new DefaultCompositionItem();
		item1.setIdShort("id1");
		item1.setID("id1");
		item1.setReference("ref1");
		
		DefaultCompositionItem item2 = new DefaultCompositionItem();
		item2.setIdShort("id2");
		item2.setID("id2");
		item2.setReference("ref2");
		info.setCompositionItems(List.of(item1, item2));
		
		DefaultCompositionDependency dep1 = new DefaultCompositionDependency();
		dep1.setIdShort("dep1");
		dep1.setSourceId("src1");
		dep1.setTargetId("tar1");
		dep1.setDependencyType("type1");
		
		DefaultCompositionDependency dep2 = new DefaultCompositionDependency();
		dep2.setIdShort("dep2");
		dep2.setSourceId("src2");
		dep2.setTargetId("tar2");
		dep2.setDependencyType("type2");
		info.setCompositionDependencies(List.of(dep1, dep2));
		
		SubmodelElementCollection smc = info.newSubmodelElement();
		Indexed<Property> field;

		field = SubmodelUtils.getPropertyById(smc, "CompositionID");
		Assert.assertEquals("compositionId", field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "CompositionType");
		Assert.assertEquals("compositionType", field.value().getValue());
		
		List<SubmodelElement> items = SubmodelUtils.getFieldById(smc, "CompositionItems",
																		SubmodelElementList.class)
													.value().getValue();
		Assert.assertEquals(2, items.size());
		List<SubmodelElement> deps = SubmodelUtils.getFieldById(smc, "CompositionDependencies",
																SubmodelElementList.class).value().getValue();
		Assert.assertEquals(2, deps.size());
		
		smc.setIdShort("idShort2");
		SubmodelUtils.getPropertyById(smc, "CompositionID").value().setValue("compositionId2");
		SubmodelUtils.getPropertyById(smc, "CompositionType").value().setValue("compositionType2");
		
		SubmodelElementList sml = SubmodelUtils.getFieldById(smc, "CompositionItems", SubmodelElementList.class).value();
		sml.getValue().remove(0);
		SubmodelElementList sml2 = SubmodelUtils.getFieldById(smc, "CompositionDependencies", SubmodelElementList.class).value();
		sml2.getValue().clear();
		
		DefaultTwinComposition info2 = new DefaultTwinComposition();
		info2.updateFromAasModel(smc);
		
		Assert.assertEquals("idShort2", info2.getIdShort());
		Assert.assertEquals("compositionId2", info2.getCompositionID());
		Assert.assertEquals("compositionType2", info2.getCompositionType());
		
		List<CompositionItem> citems = info2.getCompositionItems();
		Assert.assertEquals(1, citems.size());
		Assert.assertEquals("id2", citems.get(0).getID());
		
		List<CompositionDependency> cdeps = info2.getCompositionDependencies();
		Assert.assertEquals(0, cdeps.size());
	}
}
