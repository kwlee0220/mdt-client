package mdt.test;

import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestParameter {
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		MDTElementReference production = ElementReferences.parseExpr("param:Welder:NozzleProduction");
		production.activate(manager);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)production.read();
		
		ElementCollectionValue smev = (ElementCollectionValue)production.readValue();
		System.out.println(smev);
		
		Map<String,Object> vjo = smev.toValueObject();
		Object dv = vjo.get("DefectVolume");
		vjo.put("DefectVolume", ((Integer)dv) + 5);
		
		ElementValue updated = ElementValues.fromValueObject(vjo, smc);
		production.updateValue(updated);
	}
}
