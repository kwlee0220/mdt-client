package mdt.model.instance;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utils.stream.FStream;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@NoArgsConstructor
public class DefaultInstanceDescriptor implements InstanceDescriptor {
	private String id;
	private MDTInstanceStatus status;
	@Nullable private String baseEndpoint;
	
	private String aasId;
	@Nullable private String aasIdShort;
	@Nullable private String globalAssetId;
	@Nullable private String assetType;
	@Nullable private AssetKind assetKind;

	@Getter(AccessLevel.NONE)
	private List<DefaultInstanceSubmodelDescriptor> submodels = Lists.newArrayList();
	
	@Override
	public List<InstanceSubmodelDescriptor> getInstanceSubmodelDescriptors() {
		return FStream.from(this.submodels)
						.cast(InstanceSubmodelDescriptor.class)
						.toList();
	}

	public void setSubmodels(List<InstanceSubmodelDescriptor> smDescs) {
		submodels = FStream.from(smDescs)
							.cast(DefaultInstanceSubmodelDescriptor.class)
							.toList();
	}
}