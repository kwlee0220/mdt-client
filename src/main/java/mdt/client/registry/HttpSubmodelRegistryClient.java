package mdt.client.registry;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

import utils.Preconditions;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ErrorEntityDeserializer;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;

import mdt.aas.SubmodelRegistry;
import mdt.client.HttpMDTServiceProxy;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import mdt.model.MDTModelSerDe;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRegistryClient implements SubmodelRegistry, HttpMDTServiceProxy {
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	public HttpSubmodelRegistryClient(OkHttpClient client, String endpoint) {
		Preconditions.checkNotNullArgument(endpoint, "endpoint is null");
		
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
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
		String url = String.format("%s", m_endpoint);
		return m_restfulClient.get(url, m_smDescListDeser);
	}

	@Override
	public List<SubmodelDescriptor>
	getAllSubmodelDescriptorsByIdShort(String idShort) {
		String url = String.format("%s?idShort=%s", m_endpoint, idShort);
		return m_restfulClient.get(url, m_smDescListDeser);
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsBySemanticId(String semanticId) {
		String url = String.format("%s?semanticId=%s", m_endpoint, semanticId);
		return m_restfulClient.get(url, m_smDescListDeser);
	}

	@Override
	public SubmodelDescriptor getSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(submodelId));

		return m_restfulClient.get(url, m_smDescDeser);
	}

	@Override
	public SubmodelDescriptor postSubmodelDescriptor(SubmodelDescriptor desc) {
		String url = String.format("%s", m_endpoint);
		String reqBodyStr = MDTModelSerDe.toJsonString(desc);
		return m_restfulClient.post(url, reqBodyStr, m_smDescDeser);
	}

	@Override
	public SubmodelDescriptor putSubmodelDescriptorById(SubmodelDescriptor descriptor) {
		String url = String.format("%s", m_endpoint);
		String reqBodyStr = MDTModelSerDe.toJsonString(descriptor);
		return m_restfulClient.put(url, reqBodyStr, m_smDescDeser);
	}

	@Override
	public void deleteSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(submodelId));
		
		m_restfulClient.delete(url);
	}
	
	public void setSubmodelRepositoryEndpoint(List<String> submodelIdList, String endpoint) {
		for ( String submodelId: submodelIdList ) {
			SubmodelDescriptor desc = getSubmodelDescriptorById(submodelId);
			Endpoint ep = DescriptorUtils.newEndpoint(endpoint, "SUBMODEL-3.0");
			desc.setEndpoints(Arrays.asList(ep));
			
			putSubmodelDescriptorById(desc);
		}
	}
	
	@Override
	public String toString() {
		return String.format("SubmodelRegistry: endpoint=%s", m_endpoint);
	}

	private ResponseBodyDeserializer<SubmodelDescriptor> m_smDescDeser = new ResponseBodyDeserializer<>() {
		@Override
		public SubmodelDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, SubmodelDescriptor.class);
		}
	};
	private ResponseBodyDeserializer<List<SubmodelDescriptor>> m_smDescListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<SubmodelDescriptor> deserialize(Headers headers, String respBody)
			throws IOException {
			return MDTModelSerDe.readValueList(respBody, SubmodelDescriptor.class);
		}
	};
}
