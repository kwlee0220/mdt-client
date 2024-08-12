package mdt.client.registry;

import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import mdt.model.registry.AASRegistry;
import mdt.model.registry.RegistryException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpShellRegistryClient extends HttpRegistryClient implements AASRegistry {
	private final String m_endpoint;
	
	public HttpShellRegistryClient(OkHttpClient client, String endpoint) {
		super(client);
		
		m_endpoint = endpoint;
	}

	@Override
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/shell-descriptors/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors()
		throws RegistryException {
		String url = String.format("%s/shell-descriptors", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorsByIdShort(String idShort) throws RegistryException {
		String url = String.format("%s/shell-descriptors?idShort=%s", m_endpoint, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAssetAdministrationShellDescriptorByGlobalAssetId(String assetId) throws RegistryException {
		String url = String.format("%s/shell-descriptors?assetId=%s", m_endpoint, assetId);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public AssetAdministrationShellDescriptor
	postAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor desc) {
		try {
			String url = String.format("%s/shell-descriptors", m_endpoint);
			RequestBody reqBody = createRequestBody(desc);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, AssetAdministrationShellDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public AssetAdministrationShellDescriptor
	putAssetAdministrationShellDescriptorById(AssetAdministrationShellDescriptor descriptor) {
		try {
			String url = String.format("%s/shell-descriptors", m_endpoint);
			RequestBody reqBody = createRequestBody(descriptor);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, AssetAdministrationShellDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void deleteAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/shell-descriptors/%s", m_endpoint, AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	public void setAASRepositoryEndpoint(String aasId, String endpoint) {
		AssetAdministrationShellDescriptor desc = getAssetAdministrationShellDescriptorById(aasId);
		
		Endpoint ep = DescriptorUtils.newEndpoint(endpoint, "AAS-3.0");
		desc.setEndpoints(Arrays.asList(ep));
		
		putAssetAdministrationShellDescriptorById(desc);
	}
	
	@Override
	public String toString() {
		return String.format("AssetAdministrationShellRegistry: endpoint=%s", m_endpoint);
	}
}
