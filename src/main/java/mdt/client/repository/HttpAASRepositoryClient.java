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
import mdt.model.AssetAdministrationShellService;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * HTTP(REST)를 통해 FA³ST 서버의 AAS 저장소에 접근하는 {@link AssetAdministrationShellRepository} 클라이언트.
 * <p>
 * AAS를 반환하는 메소드들은 AAS 자체가 아니라 해당 AAS에 접근하는 {@link HttpAASServiceClient} 객체를 반환한다.
 * 모든 요청은 동기적으로 전송되며, 오류 처리 방식은 {@link Fa3stHttpClient}를 따른다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpAASRepositoryClient extends Fa3stHttpClient
										implements AssetAdministrationShellRepository {
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	AAS 저장소 endpoint (예: {@code <instance-url>/shells}).
	 */
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
		String url = withQuery(getEndpoint(), "assetId", assetId);
		
		Request req = new Request.Builder().url(url).get().build();
		List<AssetAdministrationShell> aasList = callList(req, AssetAdministrationShell.class);
		
		return FStream.from(aasList)
						.map(this::toService)
						.cast(AssetAdministrationShellService.class)
						.toList();
	}

	@Override
	public List<AssetAdministrationShellService> getAssetAdministrationShellByIdShort(String idShort) {
		String url = withQuery(getEndpoint(), "idShort", idShort);
		
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
		String urlPrefix = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(aas.getId()));
		return new HttpAASServiceClient(getHttpClient(), urlPrefix);
	}

	// 쿼리 값에 '#', '&', 공백 등이 있어도 요청이 깨지지 않도록 percent-encoding 한다.
	private static String withQuery(String endpoint, String name, String value) {
		return HttpUrl.get(endpoint).newBuilder().addQueryParameter(name, value).build().toString();
	}
}
