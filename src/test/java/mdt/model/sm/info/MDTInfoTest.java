package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.json.JsonMapper;

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
		String field;

		field = SubmodelUtils.getStringFieldById(smc, "IdShort");
		Assert.assertEquals("mdt-info", field);
		field = SubmodelUtils.getStringFieldById(smc, "AssetName");
		Assert.assertEquals("inspector-1", field);
		field = SubmodelUtils.getStringFieldById(smc, "AssetType");
		Assert.assertEquals(MDTAssetType.Machine.name(), field);
		field = SubmodelUtils.getStringFieldById(smc, "Status");
		Assert.assertEquals(MDTAssetStatus.Created.name(), field);
		
		SubmodelUtils.getPropertyFieldById(smc, "IdShort").setValue("idShort2");
		SubmodelUtils.getPropertyFieldById(smc, "AssetName").setValue("name2");
		SubmodelUtils.getPropertyFieldById(smc, "AssetType").setValue(MDTAssetType.Process.name());
		SubmodelUtils.getPropertyFieldById(smc, "Status").setValue(MDTAssetStatus.Executed.name());
		info.updateFromAasModel(smc);
		
		Assert.assertEquals("idShort2", info.getIdShort());
		Assert.assertEquals("name2", info.getAssetName());
		Assert.assertEquals(MDTAssetType.Process, info.getAssetType());
		Assert.assertEquals(MDTAssetStatus.Executed, info.getStatus());
	}
}
