package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"id", "status", "baseEndpoint", "aasId", "aasIdShort", "globalAssetId", "assetType",
					"submodels", "parameters", "operations", "twinComposition"})
@Accessors(prefix="m_")
@Getter @Setter
public class MDTModel {
	private String m_id;
	private MDTInstanceStatus m_status;
	private @Nullable String m_baseEndpoint;
	
	private String m_aasId;
	private @Nullable String m_aasIdShort;
	private @Nullable String m_globalAssetId;
	private MDTAssetType m_assetType;
	
	private List<MDTSubmodelDescriptor> m_submodels;
	private List<MDTParameterDescriptor> m_parameters;
	private List<MDTOperationDescriptor> m_operations;
	private MDTTwinCompositionDescriptor m_twinComposition;
	
	public MDTModel(InstanceDescriptor desc) {
		m_id = desc.getId();
		m_status = desc.getStatus();
		m_baseEndpoint = desc.getBaseEndpoint();
		
		m_aasId = desc.getAasId();
		m_aasIdShort = desc.getAasIdShort();
		m_globalAssetId = desc.getGlobalAssetId();
		m_assetType = desc.getAssetType();
	}
}
