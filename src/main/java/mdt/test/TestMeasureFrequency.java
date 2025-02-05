package mdt.test;

import java.util.concurrent.CompletableFuture;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.StopWatch;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.sm.value.PropertyValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestMeasureFrequency {
	public static final void main(String... args) throws Exception {
//		int port = 19009;
//		String assetId = "inspector";
//		String submodelId = String.format("http://www.lg.co.kr/refrigerator/Innercase/QualityInspector/Data", assetId);
		int port = 19000;
		String assetId = "test";
		String submodelId = String.format("https://www.samcheon.com/mdt/Test/sm/Data", assetId);
		
		String url = String.format("https://localhost:%d/api/v3.0/submodels/%s", port,
									AASUtils.encodeBase64UrlSafe(submodelId));
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		for ( int i = 0; i < 100; ++i ) {
			read(svc);
			write(svc);
		}
		
		CompletableFuture<Void> readAsync = CompletableFuture.runAsync(() -> iterateRead(svc, 5000));
		CompletableFuture<Void> writeAsync = CompletableFuture.runAsync(() -> iterateWrite(svc, 5000));
		readAsync.join();
		writeAsync.join();
	}
	
	private static void read(HttpSubmodelServiceClient svc) {
		String path0 = "DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue";
		SubmodelElement sme0 = svc.getSubmodelElementByPath(path0);
		
		String path1 = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		SubmodelElement sme1 = svc.getSubmodelElementByPath(path1);
		
		String path2 = "DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue";
		SubmodelElement sme2 = svc.getSubmodelElementByPath(path2);
	}
	
	private static void write(HttpSubmodelServiceClient svc) {
		String path0 = "DataInfo.Equipment.EquipmentParameterValues[0].ParameterValue";
		svc.updateSubmodelElementValueByPath(path0, new PropertyValue("11"));
		
		String path1 = "DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue";
		svc.updateSubmodelElementValueByPath(path1, new PropertyValue("12"));
		
		String path2 = "DataInfo.Equipment.EquipmentParameterValues[2].ParameterValue";
		svc.updateSubmodelElementValueByPath(path2, new PropertyValue("13"));
	}
	
	private static void iterateRead(HttpSubmodelServiceClient svc, int count) {
		System.out.println("start (read)");
		StopWatch watch = StopWatch.start();
		for ( int i = 0; i < count; ++i ) {
			read(svc);
			
			if ( (i + 1) % 500 == 0 ) {
				System.out.print("R");
				System.out.flush();
			}
		}
		watch.stop();
		double elapsed = watch.getElapsedInMillis() / (count * 3.0d);
		System.out.printf("\n read elapsed=%.1fms%n", elapsed);
	}
	
	private static void iterateWrite(HttpSubmodelServiceClient svc, int count) {
		System.out.println("start (write)");
		StopWatch watch = StopWatch.start();
		for ( int i = 0; i < count; ++i ) {
			write(svc);
			
			if ( (i + 1) % 500 == 0 ) {
				System.out.print("W");
				System.out.flush();
			}
		}
		watch.stop();
		double elapsed = watch.getElapsedInMillis() / (count * 3.0d);
		System.out.printf("\n write elapsed=%.1fms%n", elapsed);
	}
}
