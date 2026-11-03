package mdt.test;

import java.util.Map;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;

/**
 * {@code param:} 참조 표현식으로 Welder 파라미터 값을 읽고 일부 필드를 수정하여 다시 갱신하는
 * 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestParameter {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = mdt.getInstanceManager();
		
		MDTElementReference production = ElementReferences.parseExpr("param:Welder:NozzleProduction");
		production.activate(manager);
		
		ElementCollectionValue smev = (ElementCollectionValue)production.readValue();
		System.out.println(smev);
		
		Map<String,Object> vjo = smev.toValueObject();
		Object dv = vjo.get("DefectVolume");
		vjo.put("DefectVolume", ((Integer)dv) + 5);
		
		ElementValue updated = ElementValues.fromValueObject(vjo, smev);
		production.updateValue(updated);
	}
}
