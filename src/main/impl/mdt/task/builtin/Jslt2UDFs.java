package mdt.task.builtin;

import java.io.File;

import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import utils.DataUtils;

import jslt2.Jslt2;
import jslt2.Jslt2Exception;
import jslt2.Jslt2Function;
import jslt2.Template;
import jslt2.util.Jslt2Util;
import lombok.experimental.UtilityClass;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Jslt2UDFs {
	public static final JsonMapper MAPPER = new JsonMapper();
	
	public static void initialize(Jslt2 runtime) {
		runtime.addFunction("traverse_sme", 2, new TraverseSME());
		runtime.addFunction("get-value-by-path", 2, new GetValueByPath());
		runtime.addFunction("get-parameter-value", 2, new GetParameterValue());
		runtime.addFunction("to_string_property", 2, new ToStringProperty());
		runtime.addFunction("to_double_property", 2, new ToDoubleProperty());
		runtime.addFunction("to_smc", 0, new ToSMC());
		runtime.addFunction("to_sml", 0, new ToSML());
	}
	
	static class TraverseSME implements Jslt2Function {
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
            String path = Jslt2Util.toString(args[1], false);
			if ( args[0].isObject() ) {
				try {
					SubmodelElement top = MDTModelSerDe.readValue(args[0], SubmodelElement.class);
					SubmodelElement found = SubmodelUtils.traverse(top, path);
					String outputStr =  MDTModelSerDe.toJsonString(found);
					return MDTModelSerDe.getJsonMapper().readTree(outputStr);
				}
				catch ( Exception e ) {
					throw new Jslt2Exception("Function get-sme-by-path() failed", e);
				}
			}
            
            throw new Jslt2Exception("Function get-sme-by-path() cannot work on " + args[0]);
		}
	}
	
	static class GetValueByPath implements Jslt2Function {
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
            String path = Jslt2Util.toString(args[1], false);
			if ( args[0].isObject() ) {
				try {
					SubmodelElement top = MDTModelSerDe.readValue(args[0], SubmodelElement.class);
					SubmodelElement found = SubmodelUtils.traverse(top, path);
					if ( found instanceof Property ) {
						String outputStr =  MDTModelSerDe.toJsonString(found);
						JsonNode target = MDTModelSerDe.getJsonMapper().readTree(outputStr);
						return target.get("value");
					}
					else {
			            throw new Jslt2Exception("Function get-value-by-path() does not return Property: path="
			            							+ args[0]);
					}
				}
				catch ( Exception e ) {
					throw new Jslt2Exception("Function get-value-by-path() failed", e);
				}
			}
            
            throw new Jslt2Exception("Function get-value-by-path() cannot work on " + args[0]);
		}
	}
	
	static class GetParameterValue implements Jslt2Function {
		// get-parameter-value(., "TaktTimeEstimatedResult")
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
			String path = String.format("EquipmentParameterValues.%s.ParameterValue", args[1].asText());
			TextNode pathNode = ((ObjectNode)input).textNode(path);
			return new GetValueByPath().execute(input, args[0], pathNode);
		}
	}
	
	static class ToStringProperty implements Jslt2Function {
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
            String idShort = Jslt2Util.toString(args[1], false);
			if ( args[0].isValueNode() ) {
				ObjectNode created = MAPPER.createObjectNode();
				created.put("modelType", "Property");
				created.put("idShort", idShort);
				created.put("valueType", "xs:string");
				created.put("value", args[0].asText());
				
				return created;
			}
            
            throw new Jslt2Exception("Function get-sme-by-path() cannot work on " + args[0]);
		}
	}
	
	static class ToDoubleProperty implements Jslt2Function {
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
            String idShort = Jslt2Util.toString(args[1], false);
			if ( args[0].isValueNode() ) {
				ObjectNode created = MAPPER.createObjectNode();
				created.put("modelType", "Property");
				created.put("idShort", idShort);
				created.put("valueType", "xs:double");
				created.put("value", DataUtils.asDouble(args[0].asText()));
				
				return created;
			}
            
            throw new Jslt2Exception("Function get-sme-by-path() cannot work on " + args[0]);
		}
	}
	
	static class ToSML implements Jslt2Function {
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
            String idShort = Jslt2Util.toString(args[0], false);
            
            ObjectNode root = MAPPER.createObjectNode();
			root.put("modelType", "SubmodelElementList");
			root.put("idShort", idShort);
			if ( args.length >= 3 ) {
				root.put("orderRelevant", Jslt2Utils.getBoolean(args[1]));
			}
			if ( args.length >= 4 ) {
				root.put("typeValueListElement", AasSubmodelElements.valueOf(args[2].asText()).toString());
			}
			if ( args.length >= 5 ) {
				root.put("valueTypeListElement", DataTypeDefXsd.valueOf(args[3].asText()).toString());
			}
            if ( !(args[1] instanceof ArrayNode) ) {
                throw new Jslt2Exception("Function to-sml() takes an array for the first argument, but=" + args[1]);
            }
			root.replace("value", args[1]);
			
			return root;
		}
	}
	
	static class ToSMC implements Jslt2Function {
		@Override
		public JsonNode execute(JsonNode input, JsonNode... args) throws Jslt2Exception {
            String idShort = Jslt2Util.toString(args[0], false);
            
            ObjectNode root = MAPPER.createObjectNode();
			root.put("modelType", "SubmodelElementList");
			root.put("idShort", idShort);
			
			ArrayNode array = ((ObjectNode)input).arrayNode();
			for ( int i = 1; i < args.length; ++i ) {
				array.add(args[i]);
			}
			root.replace("value", args[1]);
			
			return root;
		}
	}
	
	public static final void main(String... args) throws Exception {
		Jslt2 runtime = Jslt2.builder().objectMapper(MAPPER).build();
		initialize(runtime);

		JsonNode root = MDTModelSerDe.getJsonMapper().readTree(new File("misc/test-sme.json"));
//		String code = """
//				let id =  get-value-by-path(., "EquipmentID")
//				let name =  get-value-by-path(., "EquipmentName")
//				let comp =  split(get-value-by-path(., "EquipmentParameterValues.TaktTimeEstimatedResult.ParameterValue"), ",")
//				
//				let prop_id = to_string_property($id, "MachineId")
//				let prop_name = to_string_property($name, "MachineName")
//				let prop_dist = to_string_property($comp[0], "TaktTimeDist")
//				let prop_loc = to_double_property($comp[1], "TaktTimeLoc")
//				let prop_scale = to_double_property($comp[2], "TaktTimeScale")
//				
//				to_smc("result", $prop_id, $prop_name, $prop_dist, $prop_loc, $prop_scale)
//				""";

		String code = """
				{
					"MachineId": get-value-by-path(., "EquipmentID"),
					"MachineName": get-value-by-path(., "EquipmentName"),
					"TaktTimeDist": get-parameter-value(., "TaktTimeEstimatedResult")
				}
				""";
		Template tmplt = runtime.compile(code);
		JsonNode result = tmplt.eval(root);
		
		System.out.println(result);
	}
}
