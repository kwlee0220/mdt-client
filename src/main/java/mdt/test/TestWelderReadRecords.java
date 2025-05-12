package mdt.test;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange;

import com.google.common.collect.Lists;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstance;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.SubmodelService;
import mdt.model.timeseries.DefaultRecords;
import mdt.model.timeseries.Record;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestWelderReadRecords {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mgr = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager mdt = mgr.getInstanceManager();
		
		HttpMDTInstance welder = mdt.getInstance("welder");
		SubmodelService svc = welder.getSubmodelServiceByIdShort("WelderAmpereLog");
		
		List<OperationVariable> inputVars = Lists.newArrayList();
		DefaultRange timespan = new DefaultRange.Builder()
												.idShort("Timespan")
												.min("2023-05-25T04:14:15")
												.max("2023-05-25T04:14:37")
												.build();
		inputVars.add(new DefaultOperationVariable.Builder().value(timespan).build());
		OperationResult result = svc.runOperation("ReadRecords", inputVars, List.of(),
													Duration.ofMinutes(10), Duration.ofSeconds(1));
		
		SubmodelElement output = result.getOutputArguments().get(0).getValue();
		DefaultRecords records = new DefaultRecords();
		records.updateFromAasModel(output);
		
		for ( Record rec: records.getRecordList() ) {
			System.out.println(rec);
		}
	}
}
