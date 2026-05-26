package mdt.client.registry;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import com.google.common.base.Preconditions;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.aas.ShellRegistry;
import mdt.client.HttpMDTServiceProxy;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpShellRegistryClient implements ShellRegistry, HttpMDTServiceProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	public HttpShellRegistryClient(OkHttpClient client, String endpoint) {
		Preconditions.checkArgument(endpoint != null, getClass().getSimpleName() + ": endpoint is null");
		
		m_endpoint = endpoint;
		ErrorEntityDeserializer errorDeser = new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER);
		m_restfulClient = HttpRESTfulClient.builder()
											.httpClient(client)
											.errorEntityDeserializer(errorDeser)
											.build();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String shellId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(shellId));
		return m_restfulClient.get(url, m_shellDeser);
	}

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors() {
		String url = String.format("%s", m_endpoint);
		return m_restfulClient.get(url, m_shellListDeser);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorsByIdShort(String idShort) {
		String url = String.format("%s?idShort=%s", m_endpoint, idShort);
		return m_restfulClient.get(url, m_shellListDeser);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorByAssetId(String assetId) {
		String encoded = AASUtils.encodeBase64UrlSafe(assetId);
		String url = String.format("%s/asset/%s", m_endpoint, encoded);

		return m_restfulClient.get(url, m_shellListDeser);
	}

	@Override
	public AssetAdministrationShellDescriptor
	addAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor desc) {
		String url = String.format("%s", m_endpoint);
		
		String reqBodyStr = MDTModelSerDe.toJsonString(desc);
		return m_restfulClient.post(url, reqBodyStr, m_shellDeser);
	}

	@Override
	public AssetAdministrationShellDescriptor
	updateAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor descriptor) {
		String url = String.format("%s", m_endpoint);

		String reqBodyStr = MDTModelSerDe.toJsonString(descriptor);
		return m_restfulClient.put(url, reqBodyStr, m_shellDeser);
	}

	@Override
	public void removeAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(aasId));
		
		m_restfulClient.delete(url);
	}
	
	public void setAASRepositoryEndpoint(String aasId, String endpoint) {
		AssetAdministrationShellDescriptor desc = getAssetAdministrationShellDescriptorById(aasId);
		
		Endpoint ep = DescriptorUtils.newEndpoint(endpoint, "AAS-3.0");
		desc.setEndpoints(Arrays.asList(ep));
		
		updateAssetAdministrationShellDescriptor(desc);
	}
	
	@Override
	public String toString() {
		return String.format("AssetAdministrationShellRegistry: endpoint=%s", m_endpoint);
	}

	private ResponseBodyDeserializer<AssetAdministrationShellDescriptor> m_shellDeser
																				= new ResponseBodyDeserializer<>() {
		@Override
		public AssetAdministrationShellDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, AssetAdministrationShellDescriptor.class);
		}
	};
	private ResponseBodyDeserializer<List<AssetAdministrationShellDescriptor>> m_shellListDeser
																				= new ResponseBodyDeserializer<>() {
		@Override
		public List<AssetAdministrationShellDescriptor> deserialize(Headers headers, String respBody)
			throws IOException {
			return MDTModelSerDe.readValueList(respBody, AssetAdministrationShellDescriptor.class);
		}
	};
}
