package mdt.model.instance;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"id", "status", "baseEndpoint", "aasId", "aasIdShort", "globalAssetId", "assetType",
					"submodels", "parameters", "operations", "twinComposition"})
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
	
	public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		m_id = id;
	}
	
	public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	public void setStatus(MDTInstanceStatus status) {
		m_status = status;
	}
	
	public @Nullable String getBaseEndpoint() {
		return m_baseEndpoint;
	}
	
	public void setBaseEndpoint(@Nullable String baseEndpoint) {
		m_baseEndpoint = baseEndpoint;
	}
	
	public String getAasId() {
		return m_aasId;
	}
	
	public void setAasId(String aasId) {
		m_aasId = aasId;
	}
	
	public @Nullable String getAasIdShort() {
		return m_aasIdShort;
	}
	
	public void setAasIdShort(@Nullable String aasIdShort) {
		m_aasIdShort = aasIdShort;
	}
	
	public @Nullable String getGlobalAssetId() {
		return m_globalAssetId;
	}
	
	public void setGlobalAssetId(@Nullable String globalAssetId) {
		m_globalAssetId = globalAssetId;
	}
	
	public MDTAssetType getAssetType() {
		return m_assetType;
	}
	
	public void setAssetType(MDTAssetType assetType) {
		m_assetType = assetType;
	}
	
	public List<MDTSubmodelDescriptor> getSubmodels() {
		return m_submodels;
	}
	
	public void setSubmodels(List<MDTSubmodelDescriptor> submodels) {
		m_submodels = submodels;
	}
	
	public List<MDTParameterDescriptor> getParameters() {
		return m_parameters;
	}
	
	public void setParameters(List<MDTParameterDescriptor> parameters) {
		m_parameters = parameters;
	}
	
	public List<MDTOperationDescriptor> getOperations() {
		return m_operations;
	}
	
	public void setOperations(List<MDTOperationDescriptor> operations) {
		m_operations = operations;
	}
	
	public MDTTwinCompositionDescriptor getTwinComposition() {
		return m_twinComposition;
	}
	
	public void setTwinComposition(MDTTwinCompositionDescriptor twinComposition) {
		m_twinComposition = twinComposition;
	}
}
