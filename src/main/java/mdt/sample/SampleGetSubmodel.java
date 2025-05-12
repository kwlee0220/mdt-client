package mdt.sample;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Lists;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.ReferenceUtils;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.ElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleGetSubmodel {
	private static final String ENDPOINT = "http://localhost:12985";
	
	private static String getOfEmpty(Object str) {
		return (str != null) ? ""+str : "";
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManager manager = HttpMDTManager.connect(ENDPOINT).getInstanceManager();
		
		MDTInstance inst = manager.getInstance("Test");
		SubmodelService svc = inst.getSubmodelServiceByIdShort("Data");
		Submodel submodel = svc.getSubmodel();
		
		System.out.printf("%-20s: %s%n", "id", submodel.getId());
		System.out.printf("%-20s: %s%n", "idShort", submodel.getIdShort());
		
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
		System.out.printf("%-20s: %s%n", "semanticId", semanticId);
		
		List<LangStringTextType> descList = submodel.getDescription();
		if ( descList.size() > 0 ) {
			System.out.printf("%-20s: %s%n", "description", descList.get(0).getText());
		}
		if ( submodel.getDisplayName().size() > 0 ) {
			System.out.printf("%-20s: %s%n", "displayName", submodel.getDisplayName().get(0).getText());
		}
		System.out.printf("%-20s: %s%n", "category", getOfEmpty(submodel.getCategory()));
		System.out.printf("%-20s: %s%n", "kind", getOfEmpty(submodel.getKind()));
		
		JsonMapper mapper = JsonMapper.builder().build();
		List<JsonNode> jnodeList = Lists.newArrayList();
		for ( SubmodelElement sme: submodel.getSubmodelElements() ) {
			ElementValue value = ElementValues.getValue(sme);
			jnodeList.add(mapper.valueToTree(value));
		}
		
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		String jsonStr = mapper.writeValueAsString(jnodeList);
		System.out.printf("%-20s: %n", "SubmodelElements");
		System.out.println(jsonStr);
		
		for ( SubmodelElement sme: svc.getAllSubmodelElements() ) {
			ElementValue value = ElementValues.getValue(sme);
			jnodeList.add(mapper.valueToTree(value));
		}
		
		svc.getFileByPath("xxx");
	}
}
