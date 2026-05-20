package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.json.JsonMapper;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.SubmodelUtils;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTInfoTest {
	public static final JsonMapper MAPPER = MDTModelSerDe.MAPPER;
	
	@BeforeEach
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
		Assertions.assertEquals("mdt-info", field);
		field = SubmodelUtils.getStringFieldById(smc, "AssetName");
		Assertions.assertEquals("inspector-1", field);
		field = SubmodelUtils.getStringFieldById(smc, "AssetType");
		Assertions.assertEquals(MDTAssetType.Machine.name(), field);
		field = SubmodelUtils.getStringFieldById(smc, "Status");
		Assertions.assertEquals(MDTAssetStatus.Created.name(), field);
		
		SubmodelUtils.getFieldById(smc, "IdShort", Property.class).setValue("idShort2");
		SubmodelUtils.getFieldById(smc, "AssetName", Property.class).setValue("name2");
		SubmodelUtils.getFieldById(smc, "AssetType", Property.class).setValue(MDTAssetType.Process.name());
		SubmodelUtils.getFieldById(smc, "Status", Property.class).setValue(MDTAssetStatus.Executed.name());
		info.updateFromAasModel(smc);
		
		Assertions.assertEquals("idShort2", info.getIdShort());
		Assertions.assertEquals("name2", info.getAssetName());
		Assertions.assertEquals(MDTAssetType.Process, info.getAssetType());
		Assertions.assertEquals(MDTAssetStatus.Executed, info.getStatus());
	}
}
