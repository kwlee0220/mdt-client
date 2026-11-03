package mdt.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;


/**
 * AAS 모델 객체로부터 Registry용 descriptor 및 endpoint 정보를 생성하거나 해석하는 유틸리티 클래스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class DescriptorUtils {
	private DescriptorUtils() {
		throw new AssertionError("Should not be called: class=" + DescriptorUtils.class.getName());
	}
	
	static final DefaultEndpoint NULL_ENDPOINT = newEndpoint("", "UNKNOWN");
	
	/**
	 * 주어진 AAS 객체로부터 {@link DefaultAssetAdministrationShellDescriptor}를 생성한다.
	 * <p>
	 * 식별자, idShort, 자산 정보, 설명 등은 {@code aas}에서 복사되고, 서브모델 descriptor 목록은 빈 리스트로
	 * 설정된다. {@code endpoint}가 {@code null}이 아니면 인터페이스가 {@code "AAS-3.0"}인 endpoint 하나가,
	 * {@code null}이면 href가 빈 문자열이고 인터페이스가 {@code "UNKNOWN"}인 endpoint가 설정된다.
	 *
	 * @param aas	대상 AAS 객체.
	 * @param endpoint	AAS 서비스 endpoint. 없는 경우는 {@code null}.
	 * @return	생성된 AAS descriptor.
	 */
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

	/**
	 * 주어진 Submodel 객체로부터 {@link DefaultSubmodelDescriptor}를 생성한다.
	 * <p>
	 * 식별자, idShort, semantic id, 설명 등은 {@code submodel}에서 복사된다. {@code endpoint}가 {@code null}이
	 * 아니면 인터페이스가 {@code "SUBMODEL-3.0"}인 endpoint 하나가, {@code null}이면 href가 빈 문자열이고
	 * 인터페이스가 {@code "UNKNOWN"}인 endpoint가 설정된다.
	 *
	 * @param submodel	대상 Submodel 객체.
	 * @param endpoint	Submodel 서비스 endpoint. 없는 경우는 {@code null}.
	 * @return	생성된 Submodel descriptor.
	 */
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
	
	/**
	 * 주어진 주소와 인터페이스 이름으로 HTTP/1.1 프로토콜의 {@link DefaultEndpoint}를 생성한다.
	 *
	 * @param endpoint	endpoint 주소 (href).
	 * @param intfc	endpoint 인터페이스 이름 (예: {@code "AAS-3.0"}).
	 * @return	생성된 endpoint 객체.
	 */
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

	/**
	 * 주어진 주소와 인터페이스 이름으로 HTTP/1.1 프로토콜의 endpoint 하나를 담은 리스트를 생성한다.
	 *
	 * @param endpoint	endpoint 주소 (href).
	 * @param intfc	endpoint 인터페이스 이름.
	 * @return	endpoint 하나로 구성된 리스트.
	 */
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

	/**
	 * 주어진 endpoint의 주소(href)를 반환한다.
	 *
	 * @param ep	대상 endpoint ({@code null}이 아니어야 한다).
	 * @return	endpoint 주소. 주소가 {@code null}이거나 빈 문자열인 경우는 {@code null}.
	 * @throws NullPointerException	{@code ep}가 {@code null}인 경우.
	 */
	public static String getEndpointString(Endpoint ep) {
		Preconditions.checkNotNull(ep);
		
		String href = ep.getProtocolInformation().getHref();
		return (href != null && href.length() > 0) ? href : null;
	}

	/**
	 * 주어진 endpoint 리스트 중 첫 번째 endpoint의 주소(href)를 반환한다.
	 *
	 * @param epList	endpoint 리스트.
	 * @return	첫 번째 endpoint의 주소. 리스트가 비었거나 주소가 비어 있는 경우는 {@code null}.
	 */
	public static @Nullable String getEndpointString(List<Endpoint> epList) {
		if ( epList.size() > 0 ) {
			return getEndpointString(epList.get(0));
		}
		else {
			return null;
		}
	}

	/**
	 * MDTInstance 서비스 endpoint로부터 Submodel Repository endpoint({@code <endpoint>/submodels})를 만든다.
	 *
	 * @param instanceServiceEndpoint	MDTInstance 서비스 endpoint.
	 * @return	Submodel Repository endpoint. {@code instanceServiceEndpoint}가 {@code null}이면 {@code null}.
	 */
	public static String toSubmodelRepositoryEndpointString(String instanceServiceEndpoint) {
		if ( instanceServiceEndpoint != null ) {
			return String.format("%s/submodels", instanceServiceEndpoint);
		}
		else {
			return null;
		}
	}

	/**
	 * MDTInstance 서비스 endpoint로부터 AAS Repository endpoint({@code <endpoint>/shells})를 만든다.
	 *
	 * @param instanceServiceEndpoint	MDTInstance 서비스 endpoint.
	 * @return	AAS Repository endpoint. {@code instanceServiceEndpoint}가 {@code null}이면 {@code null}.
	 */
	public static String toAasRepositoryEndpointString(String instanceServiceEndpoint) {
		return (instanceServiceEndpoint != null)
				? String.format("%s/shells", instanceServiceEndpoint)
				: null;
	}

	/**
	 * MDTInstance 서비스 endpoint와 AAS 식별자로부터 AAS 서비스 endpoint
	 * ({@code <endpoint>/shells/<base64url(aasId)>})를 만든다.
	 * <p>
	 * AAS Repository endpoint({@code <endpoint>/shells})는
	 * {@link #toAasRepositoryEndpointString(String)}로 만든다.
	 *
	 * @param instanceServiceEndpoint	MDTInstance 서비스 endpoint.
	 * @param aasId	AAS 식별자.
	 * @return	AAS 서비스 endpoint. {@code instanceServiceEndpoint}가 {@code null}이면 {@code null}.
	 */
	public static String toAASServiceEndpointString(String instanceServiceEndpoint, String aasId) {
		if ( instanceServiceEndpoint != null ) {
			String encodedAasId = AASUtils.encodeBase64UrlSafe(aasId);
			return String.format("%s/shells/%s", instanceServiceEndpoint, encodedAasId);
		}
		else {
			return null;
		}
	}
}
