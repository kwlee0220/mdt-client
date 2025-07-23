package mdt.model.instance;

import java.util.LinkedHashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@JsonPropertyOrder({
	"instanceId", "status", "baseEndpoint", "aasId", "aasIdShort", "globalAssetId", "assetType", "assetKind",
	"submodels", "parameters", "operations"
})
@Accessors(prefix = "m_")
public class MDTModel {
	private String m_instanceId;
	private MDTInstanceStatus m_status;
	private String m_baseEndpoint;
	private String m_aasId;
	private String m_aasIdShort;
	private String m_globalAssetId;
	private MDTAssetType m_assetType;
	private AssetKind m_assetKind;
	
	private @Nullable LinkedHashMap<String,InstanceSubmodelDescriptor> m_submodels;
	private @Nullable LinkedHashMap<String, MDTParameterModel> m_parameters;
	private @Nullable LinkedHashMap<String, MDTOperationModel> m_operations;

	@Getter @Setter
	@JsonPropertyOrder({ "name", "reference", "value" })
	@Accessors(prefix = "m_")
	public static class MDTParameterModel {
		private String m_name;
		private String m_reference;
		private Object m_value;
//		private ElementValue m_value;
	}

	@Getter @Setter
	@JsonPropertyOrder({ "name", "operationType", "inputArguments", "outputArguments" })
	@Accessors(prefix = "m_")
	public static class MDTOperationModel {
		private String m_name;
		private String m_operationType;
		private LinkedHashMap<String, Argument> m_inputArguments;
		private LinkedHashMap<String, Argument> m_outputArguments;

		@Getter @Setter
		@JsonPropertyOrder({ "name", "reference", "value" })
		@Accessors(prefix = "m_")
		public static class Argument {
			private String m_name;
			private String m_reference;
			private Object m_value;
//			private ElementValue m_value;
		}
	}
}
