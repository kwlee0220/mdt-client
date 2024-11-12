package mdt.client.repository;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import utils.InternalException;
import utils.stream.FStream;

import mdt.aas.SubmodelRepository;
import mdt.client.Fa3stHttpClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.service.SubmodelService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRepositoryClient extends Fa3stHttpClient implements SubmodelRepository {
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
		Preconditions.checkNotNull(submodelId);
		
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).get().build();
		Submodel submodel = call(req, Submodel.class);
		return (SubmodelService)toService(submodel);
	}
	
	@Override
	public List<SubmodelService> getAllSubmodelsByIdShort(String idShort) {
		Preconditions.checkNotNull(idShort);
		String url = String.format("%s?idShort=%s", getEndpoint(), idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public List<SubmodelService> getAllSubmodelBySemanticId(String semanticId) {
		Preconditions.checkNotNull(semanticId);
		String url = String.format("%s?semanticId=%s", getEndpoint(), semanticId);
		
		Request req = new Request.Builder().url(url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public HttpSubmodelServiceClient postSubmodel(Submodel submodel) {
		Preconditions.checkNotNull(submodel);
		
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
		Preconditions.checkNotNull(submodel);
		
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
		Preconditions.checkNotNull(submodelId);
		
		String url = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	private HttpSubmodelServiceClient toService(Submodel submodel) {
		Preconditions.checkNotNull(submodel);
		
		String urlPrefix = String.format("%s/%s", getEndpoint(), AASUtils.encodeBase64UrlSafe(submodel.getId()));
		return new HttpSubmodelServiceClient(getHttpClient(), urlPrefix);
	}
}
