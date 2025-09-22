package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.json.JsonMapper;

import utils.Indexed;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.SubmodelUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInfoTest {
	public static final JsonMapper MAPPER = MDTModelSerDe.MAPPER;
	
	@Before
	public void setup() {
	}
	
	@Test
	public void updateAasModel() throws Exception {
		DefaultMDTInfo info = new DefaultMDTInfo();
		info.setAssetName("inspector-1");
		info.setAssetType(MDTAssetType.Machine);
		info.setStatus(MDTAssetStatus.Created);
		info.setIdShort("mdt-info");
		
		SubmodelElementCollection smc = info.newSubmodelElement();
		Indexed<Property> field;

		field = SubmodelUtils.getPropertyById(smc, "IdShort");
		Assert.assertEquals("mdt-info", field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "AssetName");
		Assert.assertEquals("inspector-1", field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "AssetType");
		Assert.assertEquals(MDTAssetType.Machine.name(), field.value().getValue());
		field = SubmodelUtils.getPropertyById(smc, "Status");
		Assert.assertEquals(MDTAssetStatus.Created.name(), field.value().getValue());
		
		SubmodelUtils.getPropertyById(smc, "IdShort").value().setValue("idShort2");
		SubmodelUtils.getPropertyById(smc, "AssetName").value().setValue("name2");
		SubmodelUtils.getPropertyById(smc, "AssetType").value().setValue(MDTAssetType.Process.name());
		SubmodelUtils.getPropertyById(smc, "Status").value().setValue(MDTAssetStatus.Executed.name());
		info.updateFromAasModel(smc);
		
		Assert.assertEquals("idShort2", info.getIdShort());
		Assert.assertEquals("name2", info.getAssetName());
		Assert.assertEquals(MDTAssetType.Process, info.getAssetType());
		Assert.assertEquals(MDTAssetStatus.Executed, info.getStatus());
	}
}
