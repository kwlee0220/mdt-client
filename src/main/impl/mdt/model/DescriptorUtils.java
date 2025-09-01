package mdt.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;

import com.google.common.base.Preconditions;

import lombok.experimental.UtilityClass;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class DescriptorUtils {
	static final DefaultEndpoint NULL_ENDPOINT = newEndpoint("", "UNKNOWN");
	
	public static DefaultAssetAdministrationShellDescriptor createAssetAdministrationShellDescriptor(
																				AssetAdministrationShell aas,
																				@Nullable String endpoint) {
		AssetInformation assetInfo = aas.getAssetInformation();
		DefaultEndpoint ep = (endpoint != null) ? newEndpoint(endpoint, "AAS-3.0") : DescriptorUtils.NULL_ENDPOINT;
		
		DefaultAssetAdministrationShellDescriptor.Builder builder
			= new DefaultAssetAdministrationShellDescriptor.Builder()
					.administration(aas.getAdministration())
					.assetKind(aas.getAssetInformation().getAssetKind())
					.assetType(assetInfo.getAssetType())
					.globalAssetId(aas.getAssetInformation().getGlobalAssetId())
					.endpoints(Arrays.asList(ep))
					.id(aas.getId())
					.idShort(aas.getIdShort())
					.description(aas.getDescription())
					.displayName(aas.getDisplayName())
					.specificAssetIds(assetInfo.getSpecificAssetIds())
					.extensions(aas.getExtensions())
					.submodelDescriptors(Collections.emptyList());
		return builder.build();
	}

	public static DefaultSubmodelDescriptor createSubmodelDescriptor(Submodel submodel,
																	@Nullable String endpoint) {
		DefaultEndpoint ep = (endpoint != null) ? newEndpoint(endpoint, "SUBMODEL-3.0") : DescriptorUtils.NULL_ENDPOINT;
		return new DefaultSubmodelDescriptor.Builder()
					.description(submodel.getDescription())
					.displayName(submodel.getDisplayName())
					.administration(submodel.getAdministration())
					.endpoints(Arrays.asList(ep))
					.id(submodel.getId())
					.idShort(submodel.getIdShort())
					.semanticId(submodel.getSemanticId())
					.supplementalSemanticId(submodel.getSupplementalSemanticIds())
					.build();
	}
	
	public static DefaultEndpoint newEndpoint(String endpoint, String intfc) {
		DefaultProtocolInformation protoInfo = new DefaultProtocolInformation.Builder()
													.href(endpoint)
													.endpointProtocol("HTTP")
													.endpointProtocolVersion("1.1")
													.build();
		return new DefaultEndpoint.Builder()
					._interface(intfc)
					.protocolInformation(protoInfo)
					.build();
	}

	public static List<Endpoint> newEndpoints(String endpoint, String intfc) {
		DefaultProtocolInformation protoInfo = new DefaultProtocolInformation.Builder()
													.href(endpoint)
													.endpointProtocol("HTTP")
													.endpointProtocolVersion("1.1")
													.build();
		Endpoint ep = new DefaultEndpoint.Builder()
							._interface(intfc)
							.protocolInformation(protoInfo)
							.build();
		return Arrays.asList(ep);
	}

	public static String getEndpointString(Endpoint ep) {
		Preconditions.checkNotNull(ep);
		
		String href = ep.getProtocolInformation().getHref();
		return (href != null && href.length() > 0) ? href : null;
	}

	public static @Nullable String getEndpointString(List<Endpoint> epList) {
		if ( epList.size() > 0 ) {
			return getEndpointString(epList.get(0));
		}
		else {
			return null;
		}
	}

	public static String toSubmodelRepositoryEndpointString(String instanceServiceEndpoint) {
		if ( instanceServiceEndpoint != null ) {
			return String.format("%s/submodels", instanceServiceEndpoint);
		}
		else {
			return null;
		}
	}

	public static String toAasRepositoryEndpointString(String instanceServiceEndpoint) {
		return (instanceServiceEndpoint != null)
				? String.format("%s/shells", instanceServiceEndpoint)
				: null;
	}

	public static String toAASServiceEndpointString(String instanceServiceEndpoint, String aasId) {
		if ( instanceServiceEndpoint != null ) {
			String encodedAasId = AASUtils.encodeBase64UrlSafe(aasId);
			return String.format("%s/shells", instanceServiceEndpoint, encodedAasId);
		}
		else {
			return null;
		}
	}
}
