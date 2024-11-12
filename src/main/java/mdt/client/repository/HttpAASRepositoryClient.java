package mdt.client.repository;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

import utils.InternalException;
import utils.stream.FStream;

import mdt.aas.AssetAdministrationShellRepository;
import mdt.client.Fa3stHttpClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.model.AASUtils;
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
	public HttpAASRepositoryClient(OkHttpClient client, String endpoint) {
		super(client, endpoint);
	}

	@Override
	public List<AssetAdministrationShellService> getAllAssetAdministrationShells() {
		Request req = new Request.Builder().url(getEndpoint()).get().build();
		List<AssetAdministrationShell> aasList = callList(req, AssetAdministrationShell.class);
		
		return FStream.from(aasList)
						.map(this::toService)
						.cast(AssetAdministrationShellService.class)
						.toList();
	}

	@Override
	public HttpAASServiceClient getAssetAdministrationShellById(String aasId) {
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).get().build();
		AssetAdministrationShell aas = call(req, AssetAdministrationShell.class);
		return toService(aas);
	}

	@Override
	public List<AssetAdministrationShellService> getAssetAdministrationShellByAssetId(String assetId) {
		String url = String.format("%s?assetId=%s", getEndpoint(), assetId);
		
		Request req = new Request.Builder().url(url).get().build();
		List<AssetAdministrationShell> aasList = callList(req, AssetAdministrationShell.class);
		
		return FStream.from(aasList)
						.map(this::toService)
						.cast(AssetAdministrationShellService.class)
						.toList();
	}

	@Override
	public List<AssetAdministrationShellService> getAssetAdministrationShellByIdShort(String idShort) {
		String url = String.format("%s?idShort=%s", getEndpoint(), idShort);
		
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
			
			Request req = new Request.Builder().url(getEndpoint()).post(reqBody).build();
			aas = call(req, AssetAdministrationShell.class);
			return toService(aas);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public AssetAdministrationShellService putAssetAdministrationShellById(AssetAdministrationShell aas) {
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(aas.getId()));
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			aas = call(req, AssetAdministrationShell.class);
			return toService(aas);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void deleteAssetAdministrationShellById(String aasId) {
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(aasId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	private HttpAASServiceClient toService(AssetAdministrationShell aas) {
		String urlPrefix = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(getEndpoint()));
		return new HttpAASServiceClient(getHttpClient(), urlPrefix);
	}
}
