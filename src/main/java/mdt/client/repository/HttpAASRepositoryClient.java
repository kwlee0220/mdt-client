package mdt.client.repository;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

import utils.stream.FStream;

import mdt.client.Fa3stHttpClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.model.AASUtils;
import mdt.model.registry.RegistryException;
import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.service.AssetAdministrationShellService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpAASRepositoryClient extends Fa3stHttpClient
													implements AssetAdministrationShellRepository {
	private final String m_url;
	
	public HttpAASRepositoryClient(OkHttpClient client, String urlPrefix) {
		super(client);
		
		m_url = urlPrefix;
	}
	
	public String getUrlPrefix() {
		return m_url;
	}

	@Override
	public List<AssetAdministrationShellService> getAllAssetAdministrationShells() {
		Request req = new Request.Builder().url(m_url).get().build();
		List<AssetAdministrationShell> aasList = callList(req, AssetAdministrationShell.class);
		
		return FStream.from(aasList)
						.map(this::toService)
						.cast(AssetAdministrationShellService.class)
						.toList();
	}

	@Override
	public HttpAASServiceClient getAssetAdministrationShellById(String aasId) {
		String url = String.format("%s/%s", m_url, AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).get().build();
		AssetAdministrationShell aas = call(req, AssetAdministrationShell.class);
		return toService(aas);
	}

	@Override
	public List<AssetAdministrationShellService> getAssetAdministrationShellByAssetId(String assetId) {
		String url = String.format("%s?assetId=%s", m_url, assetId);
		
		Request req = new Request.Builder().url(url).get().build();
		List<AssetAdministrationShell> aasList = callList(req, AssetAdministrationShell.class);
		
		return FStream.from(aasList)
						.map(this::toService)
						.cast(AssetAdministrationShellService.class)
						.toList();
	}

	@Override
	public List<AssetAdministrationShellService> getAssetAdministrationShellByIdShort(String idShort) {
		String url = String.format("%s?idShort=%s", m_url, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		List<AssetAdministrationShell> aasList = callList(req, AssetAdministrationShell.class);
		
		return FStream.from(aasList)
						.map(this::toService)
						.cast(AssetAdministrationShellService.class)
						.toList();
	}

	@Override
	public AssetAdministrationShellService postAssetAdministrationShell(AssetAdministrationShell aas) {
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(m_url).post(reqBody).build();
			aas = call(req, AssetAdministrationShell.class);
			return toService(aas);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public AssetAdministrationShellService putAssetAdministrationShellById(AssetAdministrationShell aas) {
		String url = String.format("%s/%s", m_url, AASUtils.encodeBase64UrlSafe(aas.getId()));
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			aas = call(req, AssetAdministrationShell.class);
			return toService(aas);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void deleteAssetAdministrationShellById(String aasId) {
		String url = String.format("%s/%s", m_url, AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	private HttpAASServiceClient toService(AssetAdministrationShell aas) {
		String urlPrefix = String.format("%s/%s", m_url, AASUtils.encodeBase64UrlSafe(m_url));
		return new HttpAASServiceClient(getHttpClient(), urlPrefix);
	}
}
