package mdt.test;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.sm.PropertyUtils;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestRCKSimulation {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		MDTInstance instance = manager.getInstance("PressProcess");
		SubmodelService svc = instance.getSubmodelServiceByIdShort("PressSimulation");
		
		MDTElementReference ref = ElementReferences.parseExpr("oparg:PressProcess:PressSimulation:in:RCKServerEndpoint");
		ref.activate(manager);
		String endpoint = ref.readAsString();
		OperationVariable endpointVar;
		endpointVar = new DefaultOperationVariable.Builder()
													.value(PropertyUtils.STRING("RCKServerEndpoint", endpoint))
													.build();
		
		ref = ElementReferences.parseExpr("oparg:PressProcess:PressSimulation:in:ProcessName");
		ref.activate(manager);
		String procName = ref.readAsString();
		OperationVariable procNameVar = new DefaultOperationVariable.Builder()
													.value(PropertyUtils.STRING("ProcessName", procName))
													.build();

		ref = ElementReferences.parseExpr("oparg:PressProcess:PressSimulation:in:LayoutName");
		ref.activate(manager);
		String layoutName = ref.readAsString();
		OperationVariable layoutNameVar = new DefaultOperationVariable.Builder()
													.value(PropertyUtils.STRING("LayoutName", layoutName))
													.build();
		
		OperationResult result = svc.runOperation("Operation", List.of(endpointVar, procNameVar, layoutNameVar),
													List.of(), Duration.ofMinutes(5), Duration.ofSeconds(5));
		
		if ( result.getSuccess() ) {
			System.out.println("RCK simulation succeeded.");
		}
		else {
			System.out.println("RCK simulation failed: " + result.getExecutionState());
		}
	}
}
