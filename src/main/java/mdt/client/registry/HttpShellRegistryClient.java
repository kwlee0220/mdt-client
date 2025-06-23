package mdt.client.registry;

import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import utils.InternalException;

import mdt.aas.ShellRegistry;
import mdt.client.HttpMDTServiceProxy;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpShellRegistryClient extends HttpRegistryClient implements ShellRegistry, HttpMDTServiceProxy {
	private final String m_registryEndpoint;
	
	public HttpShellRegistryClient(OkHttpClient client, String registryEndpoint) {
		super(client);
		
		m_registryEndpoint = registryEndpoint;
	}

	@Override
	public String getEndpoint() {
		return m_registryEndpoint;
	}

	@Override
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String shellId) {
		String url = String.format("%s/shell-descriptors/%s", m_registryEndpoint, AASUtils.encodeBase64UrlSafe(shellId));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors() {
		String url = String.format("%s/shell-descriptors", m_registryEndpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorsByIdShort(String idShort) {
		String url = String.format("%s/shell-descriptors?idShort=%s", m_registryEndpoint, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorByAssetId(String assetId) {
		String encoded = AASUtils.encodeBase64UrlSafe(assetId);
		String url = String.format("%s/shell-descriptors/asset/%s", m_registryEndpoint, encoded);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public AssetAdministrationShellDescriptor
	addAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor desc) {
		try {
			String url = String.format("%s/shell-descriptors", m_registryEndpoint);
			RequestBody reqBody = createRequestBody(desc);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, AssetAdministrationShellDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public AssetAdministrationShellDescriptor
	updateAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor descriptor) {
		try {
			String url = String.format("%s/shell-descriptors", m_registryEndpoint);
			RequestBody reqBody = createRequestBody(descriptor);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, AssetAdministrationShellDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void removeAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/shell-descriptors/%s", m_registryEndpoint, AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	public void setAASRepositoryEndpoint(String aasId, String endpoint) {
		AssetAdministrationShellDescriptor desc = getAssetAdministrationShellDescriptorById(aasId);
		
		Endpoint ep = DescriptorUtils.newEndpoint(endpoint, "AAS-3.0");
		desc.setEndpoints(Arrays.asList(ep));
		
		updateAssetAdministrationShellDescriptor(desc);
	}
	
	@Override
	public String toString() {
		return String.format("AssetAdministrationShellRegistry: endpoint=%s", m_registryEndpoint);
	}
}
