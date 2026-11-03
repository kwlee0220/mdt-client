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
import mdt.client.instance.HttpMDTInstanceClient;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.timeseries.DefaultRecords;
import mdt.model.timeseries.Record;

/**
 * welder 인스턴스의 WelderAmpereLog 서브모델 {@code ReadRecords} 연산을 시간 구간으로 호출하여
 * 레코드를 출력하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestWelderReadRecords {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		HttpMDTManager mgr = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager mdt = mgr.getInstanceManager();
		
		HttpMDTInstanceClient welder = mdt.getInstance("welder");
		SubmodelService svc = welder.getSubmodelServiceByIdShort("WelderAmpereLog")
							.getOrThrow(() -> ResourceNotFoundException.ofIdShort("SubmodelService", "WelderAmpereLog"));
		
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
