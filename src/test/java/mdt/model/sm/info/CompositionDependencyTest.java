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
public class CompositionDependencyTest {
	@Before
	public void setup() {
	}
	
	@Test
	public void updateAasModel() throws Exception {
		DefaultCompositionDependency dep = new DefaultCompositionDependency();
		dep.setIdShort("idShort");
		dep.setSourceId("sourceId");
		dep.setTargetId("targetId");
		dep.setDependencyType("dependencyType");
		
		SubmodelElementCollection smc = dep.newSubmodelElement();
		Indexed<Property> field;

		Assert.assertEquals("idShort", smc.getIdShort());
		field = SubmodelUtils.getPropertyById(smc, "SourceId");
		Assert.assertEquals("sourceId", field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "TargetId");
		Assert.assertEquals("targetId", field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "DependencyType");
		Assert.assertEquals("dependencyType", field.value().getValue());

		smc.setIdShort("idShort2");
		SubmodelUtils.getPropertyById(smc, "SourceId").value().setValue("sourceId2");
		SubmodelUtils.getPropertyById(smc, "TargetId").value().setValue("targetId2");
		SubmodelUtils.getPropertyById(smc, "DependencyType").value().setValue("dependencyType2");
		dep.updateFromAasModel(smc);
		
		Assert.assertEquals("idShort2", dep.getIdShort());
		Assert.assertEquals("sourceId2", dep.getSourceId());
		Assert.assertEquals("targetId2", dep.getTargetId());
		Assert.assertEquals("dependencyType2", dep.getDependencyType());
	}
}
