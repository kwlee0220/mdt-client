package mdt.model.instance;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import utils.stream.FStream;

import mdt.model.sm.info.MDTAssetType;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@Accessors(prefix="m_")
@NoArgsConstructor
public class DefaultInstanceDescriptor implements InstanceDescriptor {
	private String m_id;
	private MDTInstanceStatus m_status;
	private @Nullable String m_baseEndpoint;
	
	private String m_aasId;
	private @Nullable String m_aasIdShort;
	private @Nullable String m_globalAssetId;
	private @Nullable MDTAssetType m_assetType;
	private @Nullable AssetKind m_assetKind;

	@Getter(AccessLevel.NONE)
	private List<DefaultInstanceSubmodelDescriptor> m_submodels = Lists.newArrayList();

	@Getter(AccessLevel.NONE)
	private List<? extends MDTParameterDescriptor> m_parameters = Lists.newArrayList();

	@Getter(AccessLevel.NONE)
	private List<? extends MDTOperationDescriptor> m_operations = Lists.newArrayList();
	
	@Override
	public List<InstanceSubmodelDescriptor> getInstanceSubmodelDescriptorAll() {
		return FStream.from(m_submodels)
						.cast(InstanceSubmodelDescriptor.class)
						.toList();
	}

	public void setSubmodels(List<? extends InstanceSubmodelDescriptor> smDescs) {
		m_submodels = FStream.from(smDescs)
							.cast(DefaultInstanceSubmodelDescriptor.class)
							.toList();
	}

	@Override
	public List<MDTParameterDescriptor> getMDTParameterDescriptorAll() {
		return FStream.from(m_parameters)
						.cast(MDTParameterDescriptor.class)
						.toList();
	}
	
	public void setAssetParameters(List<? extends MDTParameterDescriptor> params) {
		m_parameters = params;
	}

	@Override
	public List<MDTOperationDescriptor> getMDTOperationDescriptorAll() {
		return FStream.from(m_operations)
						.cast(MDTOperationDescriptor.class)
						.toList();
	}
	
	public void setAssetOperations(List<? extends MDTOperationDescriptor> ops) {
		m_operations = ops;
	}
}