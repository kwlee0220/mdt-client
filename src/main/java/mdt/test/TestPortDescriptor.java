package mdt.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import mdt.model.AASUtils;
import mdt.model.workflow.descriptor.port.PortDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestPortDescriptor {
	public static final void main(String... args) throws Exception {
		JsonDeserializer deser = AASUtils.getJsonDeserializer();
		
		List<PortDescriptor> pdList;
		File descFile = new File("misc/test_port_descriptor.json");
		try ( FileInputStream fis = new FileInputStream(descFile) ) {
			pdList = deser.readList(fis, PortDescriptor.class);
		}
		System.out.println(pdList);
		
		JsonSerializer ser = AASUtils.getJsonSerializer();
		for ( PortDescriptor pd: pdList ) {
			System.out.println(ser.write(pd));
		}
	}
}
