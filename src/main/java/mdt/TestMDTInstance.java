package mdt;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.registry.InvalidResourceStatusException;
import mdt.model.registry.ResourceNotFoundException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMDTInstance {
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient client = HttpMDTInstanceManagerClient.connect("http://localhost:12985/instance-manager");
		
		File dir = new File("D:\\Dropbox\\Temp\\fa3st-repository\\ispark\\models");
		HttpMDTInstanceClient inst1 = client.addInstance("KR3", null, null, new File(dir, "aas_KR3.json"),
												new File(dir, "conf_KR3.json"));
		assert inst1.getId().equals("KR3");
		assert inst1.getAasIdShort().equals("KR3");
		assert FStream.from(inst1.getInstanceSubmodelDescriptors())
						.map(InstanceSubmodelDescriptor::getIdShort)
						.toSet().equals(Set.of("Data", "InformationModel"));
		assert inst1.getStatus().equals(MDTInstanceStatus.STOPPED);
		assert inst1.getEndpoint() == null;
		assert inst1.getAssetType().equals("Line");
		
		HttpMDTInstanceClient inst2 = client.addInstance("CRF", null, null, new File(dir, "aas_CRF.json"),
														new File(dir, "conf_CRF.json"));
		assert inst2.getId().equals("CRF");
		assert inst2.getAasIdShort().equals("CRF");
		assert FStream.from(inst1.getInstanceSubmodelDescriptors())
						.map(InstanceSubmodelDescriptor::getIdShort)
						.toSet().equals(Set.of("Data", "InformationModel"));
		assert inst2.getStatus().equals(MDTInstanceStatus.STOPPED);
		assert inst2.getEndpoint() == null;
		assert inst2.getAssetType().equals("Process");

		HttpMDTInstanceClient inst3 = client.addInstance("KRCW-01EATT018", null, null,
														new File(dir, "aas_KRCW-01EATT018.json"),
														new File(dir, "conf_KRCW-01EATT018.json"));
		assert inst3.getId().equals("KRCW-01EATT018");
		assert inst3.getAasIdShort().equals("KRCW-01EATT018");
		assert FStream.from(inst1.getInstanceSubmodelDescriptors())
						.map(InstanceSubmodelDescriptor::getIdShort)
						.toSet().equals(Set.of("Data", "InformationModel"));
		assert inst3.getStatus().equals(MDTInstanceStatus.STOPPED);
		assert inst3.getEndpoint() == null;
		assert inst3.getAssetType().equals("Machine");
		
		inst2.start(Duration.ofSeconds(2), null);
		assert inst2.getStatus().equals(MDTInstanceStatus.RUNNING);
		
		client.removeInstance(inst1);
		try {
			assert inst1.getStatus().equals(MDTInstanceStatus.STOPPED);
		}
		catch ( ResourceNotFoundException expected ) { }
		FStream.from(client.getAllInstances())
				.map(MDTInstance::getId)
				.toSet().equals(Set.of("CRF", "KRCW-01EATT018"));

		try {
			client.removeInstance(inst2);
		}
		catch ( InvalidResourceStatusException expected ) { }
		inst2.stop(Duration.ofSeconds(1), null);
		
		MDTInstanceStatus status = inst2.getStatus();
		assert status.equals(MDTInstanceStatus.STOPPED);
		client.removeInstance(inst2);
		FStream.from(client.getAllInstances())
				.map(MDTInstance::getId)
				.toSet().equals(Set.of("KRCW-01EATT018"));
		
		client.removeInstance(inst3);
		List<? extends MDTInstance> instances = client.getAllInstances();
		assert instances.size() == 0;
	}
}
