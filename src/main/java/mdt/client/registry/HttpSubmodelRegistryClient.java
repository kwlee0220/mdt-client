package mdt.client.registry;

import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import utils.InternalException;

import mdt.aas.SubmodelRegistry;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRegistryClient extends HttpRegistryClient implements SubmodelRegistry {
	private final String m_endpoint;
	
	public HttpSubmodelRegistryClient(OkHttpClient client, String endpoint) {
		super(client);
		
		m_endpoint = endpoint;
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
		String url = String.format("%s/submodel-descriptors", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

	@Override
	public List<SubmodelDescriptor>
	getAllSubmodelDescriptorsByIdShort(String idShort) {
		String url = String.format("%s/submodel-descriptors?idShort=%s", m_endpoint, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsBySemanticId(String semanticId) {
		String url = String.format("%s/submodel-descriptors?semanticId=%s", m_endpoint, semanticId);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

	@Override
	public SubmodelDescriptor getSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/submodel-descriptors/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, SubmodelDescriptor.class);
	}

	@Override
	public SubmodelDescriptor postSubmodelDescriptor(SubmodelDescriptor desc) {
		try {
			String url = String.format("%s/submodel-descriptors", m_endpoint);
			RequestBody reqBody = createRequestBody(desc);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public SubmodelDescriptor putSubmodelDescriptorById(SubmodelDescriptor descriptor) {
		String url = String.format("%s/submodel-descriptors", m_endpoint);
		try {
			RequestBody reqBody = createRequestBody(descriptor);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, SubmodelDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void deleteSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/submodel-descriptors/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
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
}
