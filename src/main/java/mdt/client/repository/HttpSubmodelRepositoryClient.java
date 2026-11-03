package mdt.client.repository;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import utils.InternalException;
import utils.Preconditions;
import utils.stream.FStream;

import mdt.aas.SubmodelRepository;
import mdt.client.Fa3stHttpClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.SubmodelService;

/**
 * HTTP(REST)를 통해 FA³ST 서버의 Submodel 저장소에 접근하는 {@link SubmodelRepository} 클라이언트.
 * <p>
 * Submodel을 반환하는 메소드들은 Submodel 자체가 아니라 해당 Submodel의 endpoint
 * ({@code <저장소 endpoint>/<Base64 URL-safe 인코딩된 submodel id>})에 접근하는
 * {@link HttpSubmodelServiceClient} 객체를 반환한다.
 * 모든 요청은 동기적으로 전송되며, 오류 처리 방식은 {@link Fa3stHttpClient}를 따른다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRepositoryClient extends Fa3stHttpClient implements SubmodelRepository {
	/**
	 * 클라이언트 객체를 생성한다.
	 *
	 * @param client	HTTP 요청 전송에 사용할 OkHttp 클라이언트.
	 * @param endpoint	Submodel 저장소 endpoint (예: {@code <instance-url>/submodels}).
	 */
	public HttpSubmodelRepositoryClient(OkHttpClient client, String endpoint) {
		super(client,endpoint);
	}

	@Override
	public List<SubmodelService> getAllSubmodels() {
		Request req = new Request.Builder().url(getEndpoint()).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public SubmodelService getSubmodelById(String submodelId) {
		Preconditions.checkNotNullArgument(submodelId, "submodelId must be not null");
		
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).get().build();
		Submodel submodel = call(req, Submodel.class);
		return (SubmodelService)toService(submodel);
	}
	
	@Override
	public List<SubmodelService> getAllSubmodelsByIdShort(String idShort) {
		Preconditions.checkNotNullArgument(idShort, "idShort must be not null");
		String url = withQuery(getEndpoint(), "idShort", idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public List<SubmodelService> getAllSubmodelBySemanticId(String semanticId) {
		Preconditions.checkNotNullArgument(semanticId, "semanticId must be not null");
		String url = withQuery(getEndpoint(), "semanticId", semanticId);
		
		Request req = new Request.Builder().url(url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public HttpSubmodelServiceClient postSubmodel(Submodel submodel) {
		Preconditions.checkNotNullArgument(submodel, "submodel must be not null");
		
		try {
			RequestBody reqBody = createRequestBody(submodel);
			
			Request req = new Request.Builder().url(getEndpoint()).post(reqBody).build();
			submodel = call(req, Submodel.class);
			return toService(submodel);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}
	
	@Override
	public HttpSubmodelServiceClient putSubmodelById(Submodel submodel) {
		Preconditions.checkNotNullArgument(submodel, "submodel must be not null");
		
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodel.getId()));
		try {
			RequestBody reqBody = createRequestBody(submodel);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			submodel = call(req, Submodel.class);
			return toService(submodel);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}
	
	@Override
	public void deleteSubmodelById(String submodelId) {
		Preconditions.checkNotNullArgument(submodelId, "submodelId must be not null");
		
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	private HttpSubmodelServiceClient toService(Submodel submodel) {
		Preconditions.checkNotNullArgument(submodel, "submodel must be not null");
		
		String urlPrefix = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodel.getId()));
		return new HttpSubmodelServiceClient(getHttpClient(), urlPrefix);
	}

	// 쿼리 값에 '#', '&', 공백 등이 있어도 요청이 깨지지 않도록 percent-encoding 한다.
	private static String withQuery(String endpoint, String name, String value) {
		return HttpUrl.get(endpoint).newBuilder().addQueryParameter(name, value).build().toString();
	}
}
