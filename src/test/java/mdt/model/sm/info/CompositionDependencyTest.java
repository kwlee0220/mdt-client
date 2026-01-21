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
		String field;

		Assert.assertEquals("idShort", smc.getIdShort());
		field = SubmodelUtils.getStringFieldById(smc, "SourceId");
		Assert.assertEquals("sourceId", field);
		field = SubmodelUtils.getStringFieldById(smc, "TargetId");
		Assert.assertEquals("targetId", field);
		field = SubmodelUtils.getStringFieldById(smc, "DependencyType");
		Assert.assertEquals("dependencyType", field);

		smc.setIdShort("idShort2");
		SubmodelUtils.getPropertyFieldById(smc, "SourceId").setValue("sourceId2");
		SubmodelUtils.getPropertyFieldById(smc, "TargetId").setValue("targetId2");
		SubmodelUtils.getPropertyFieldById(smc, "DependencyType").setValue("dependencyType2");
		dep.updateFromAasModel(smc);
		
		Assert.assertEquals("idShort2", dep.getIdShort());
		Assert.assertEquals("sourceId2", dep.getSourceId());
		Assert.assertEquals("targetId2", dep.getTargetId());
		Assert.assertEquals("dependencyType2", dep.getDependencyType());
	}
}
