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
public class CompositionDependencyTest {
	@BeforeEach
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

		Assertions.assertEquals("idShort", smc.getIdShort());
		field = SubmodelUtils.getStringFieldById(smc, "SourceId");
		Assertions.assertEquals("sourceId", field);
		field = SubmodelUtils.getStringFieldById(smc, "TargetId");
		Assertions.assertEquals("targetId", field);
		field = SubmodelUtils.getStringFieldById(smc, "DependencyType");
		Assertions.assertEquals("dependencyType", field);

		smc.setIdShort("idShort2");
		SubmodelUtils.getFieldById(smc, "SourceId", Property.class).setValue("sourceId2");
		SubmodelUtils.getFieldById(smc, "TargetId", Property.class).setValue("targetId2");
		SubmodelUtils.getFieldById(smc, "DependencyType", Property.class).setValue("dependencyType2");
		dep.updateFromAasModel(smc);
		
		Assertions.assertEquals("idShort2", dep.getIdShort());
		Assertions.assertEquals("sourceId2", dep.getSourceId());
		Assertions.assertEquals("targetId2", dep.getTargetId());
		Assertions.assertEquals("dependencyType2", dep.getDependencyType());
	}
}
