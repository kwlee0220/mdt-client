package mdt.client.resource;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Resource;

import utils.InternalException;

import mdt.client.Fa3stHttpClient;
import mdt.model.AASUtils;
import mdt.model.AssetAdministrationShellService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * HTTP(REST)를 통해 FA³ST 서버의 개별 AAS에 접근하는 {@link AssetAdministrationShellService} 클라이언트.
 * <p>
 * endpoint는 대상 AAS 자원의 URL이며, submodel 참조({@code submodel-refs}), 자산 정보
 * ({@code asset-information}), 썸네일({@code asset-information/thumbnail})은 이 URL의 하위 경로로 접근한다.
 * 모든 요청은 동기적으로 전송되며, 오류 처리 방식은 {@link Fa3stHttpClient}를 따른다.
 * 요청 본문 직렬화에 실패하면 {@link InternalException}이 발생한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpAASServiceClient extends Fa3stHttpClient implements AssetAdministrationShellService {
	private static final String SUBMODEL_REFS = "submodel-refs";
	private static final String ASSET_INFO = "asset-information";
	private static final String THUMBNAIL = "asset-information/thumbnail";
	
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	대상 AAS 자원의 endpoint.
	 */
	public HttpAASServiceClient(OkHttpClient client, String endpoint) {
		super(client, endpoint);
	}

	@Override
	public AssetAdministrationShell getAssetAdministrationShell() {
		Request req = new Request.Builder().url(getEndpoint()).get().build();
		return call(req, AssetAdministrationShell.class);
	}

	@Override
	public AssetAdministrationShell putAssetAdministrationShell(AssetAdministrationShell aas) {
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(getEndpoint()).put(reqBody).build();
			return call(req, AssetAdministrationShell.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public List<Reference> getAllSubmodelReferences() {
		String url = String.format("%s/%s", getEndpoint(), SUBMODEL_REFS);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, Reference.class);
	}

	@Override
	public Reference postSubmodelReference(Reference ref) {
		try {
			RequestBody reqBody = createRequestBody(ref);

			String url = String.format("%s/%s", getEndpoint(), SUBMODEL_REFS);
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, Reference.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void deleteSubmodelReference(String submodelId) {
		String url = String.format("%s/%s/%s", getEndpoint(), SUBMODEL_REFS,
									AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	@Override
	public AssetInformation getAssetInformation() {
		String url = String.format("%s/%s", getEndpoint(), ASSET_INFO);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetInformation.class);
	}

	@Override
	public AssetInformation putAssetInformation(AssetInformation assetInfo) {
		String url = String.format("%s/%s", getEndpoint(), ASSET_INFO);
		try {
			RequestBody reqBody = createRequestBody(assetInfo);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, AssetInformation.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public Resource getThumbnail() {
		String url = String.format("%s/%s", getEndpoint(), THUMBNAIL);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, Resource.class);
	}

	@Override
	public Resource putThumbnail(Resource thumbnail) {
		try {
			RequestBody reqBody = createRequestBody(thumbnail);

			String url = String.format("%s/%s", getEndpoint(), THUMBNAIL);
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, Resource.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void deleteThumbnail() {
		String url = String.format("%s/%s", getEndpoint(), THUMBNAIL);
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
}
