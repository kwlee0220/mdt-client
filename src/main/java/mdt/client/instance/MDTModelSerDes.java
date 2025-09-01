package mdt.client.instance;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.MDTOperationDescriptor;
import mdt.model.instance.MDTParameterDescriptor;
import mdt.model.instance.MDTSubmodelDescriptor;
import mdt.model.instance.MDTTwinCompositionDescriptor;

import okhttp3.Headers;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTModelSerDes {
	static ResponseBodyDeserializer<AssetAdministrationShellDescriptor> AAS_SHELL_RESP = new ResponseBodyDeserializer<>() {
		@Override
		public AssetAdministrationShellDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, AssetAdministrationShellDescriptor.class);
		}
	};
	static ResponseBodyDeserializer<List<AssetAdministrationShellDescriptor>> AAS_SHELL_LIST_RESP = new ResponseBodyDeserializer<>() {
		@Override
		public List<AssetAdministrationShellDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, AssetAdministrationShellDescriptor.class);
		}
	};

	static ResponseBodyDeserializer<List<SubmodelDescriptor>> AAS_SM_LIST_RESP = new ResponseBodyDeserializer<>() {
		@Override
		public List<SubmodelDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, SubmodelDescriptor.class);
		}
	};
	
	public static String toJson(InstanceDescriptor instDesc) throws IOException {
		return MDTModelSerDe.toJsonString(instDesc);
	}
	
	static ResponseBodyDeserializer<InstanceDescriptor> INSTANCE_DESC_RESP = new ResponseBodyDeserializer<>() {
		@Override
		public InstanceDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, InstanceDescriptor.class);
		}
	};
	static ResponseBodyDeserializer<List<InstanceDescriptor>> INSTANCE_DESC_LIST = new ResponseBodyDeserializer<>() {
		@Override
		public List<InstanceDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, InstanceDescriptor.class);
		}
	};
	
	static ResponseBodyDeserializer<List<MDTSubmodelDescriptor>> MDT_SUBMODEL_LIST = new ResponseBodyDeserializer<>() {
		@Override
		public List<MDTSubmodelDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, MDTSubmodelDescriptor.class);
		}
	};
	
	static ResponseBodyDeserializer<List<MDTParameterDescriptor>> MDT_PARAM_LIST= new ResponseBodyDeserializer<>() {
		@Override
		public List<MDTParameterDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, MDTParameterDescriptor.class);
		}
	};
	
	static ResponseBodyDeserializer<List<MDTOperationDescriptor>> MDT_OP_LIST = new ResponseBodyDeserializer<>() {
		@Override
		public List<MDTOperationDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, MDTOperationDescriptor.class);
		}
	};

	static ResponseBodyDeserializer<MDTTwinCompositionDescriptor> MDT_TWIN_COMP = new ResponseBodyDeserializer<>() {
		@Override
		public MDTTwinCompositionDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, MDTTwinCompositionDescriptor.class);
		}
	};
}
