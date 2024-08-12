package mdt.client.resource;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.client.Fa3stHttpClient;
import mdt.model.AASUtils;
import mdt.model.registry.RegistryException;
import mdt.model.service.AssetAdministrationShellService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpAASServiceClient extends Fa3stHttpClient implements AssetAdministrationShellService {
	private final String m_urlPrefix;
	
	public HttpAASServiceClient(OkHttpClient client, String urlPrefix) {
		super(client);
		
		m_urlPrefix = urlPrefix;
	}

	@Override
	public AssetAdministrationShell getAssetAdministrationShell() {
		Request req = new Request.Builder().url(m_urlPrefix).get().build();
		return call(req, AssetAdministrationShell.class);
	}

	@Override
	public AssetAdministrationShell putAssetAdministrationShell(AssetAdministrationShell aas) {
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(m_urlPrefix).put(reqBody).build();
			return call(req, AssetAdministrationShell.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public List<Reference> getAllSubmodelReferences() {
		String url = String.format("%s/submodels", m_urlPrefix);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, Reference.class);
	}

	@Override
	public Reference postSubmodelReference(Reference ref) {
		try {
			RequestBody reqBody = createRequestBody(ref);
			
			Request req = new Request.Builder().url(m_urlPrefix).post(reqBody).build();
			return call(req, Reference.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void deleteSubmodelReference(String submodelId) {
		String url = String.format("%s/submodels/%s", m_urlPrefix, AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	@Override
	public AssetInformation getAssetInformation() {
		String url = String.format("%s/asset-information", m_urlPrefix);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetInformation.class);
	}

	@Override
	public AssetInformation putAssetInformation(AssetInformation assetInfo) {
		String url = String.format("%s/asset-information", m_urlPrefix);
		try {
			RequestBody reqBody = createRequestBody(assetInfo);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, AssetInformation.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}
}
