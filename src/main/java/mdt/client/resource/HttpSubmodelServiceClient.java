package mdt.client.resource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.datatype.Duration;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationRequest;

import com.google.common.base.Preconditions;

import utils.func.Tuple;

import mdt.client.Fa3stHttpClient;
import mdt.client.MDTClientException;
import mdt.client.SSLUtils;
import mdt.model.registry.RegistryException;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.resource.MDTOperationHandle;
import mdt.model.resource.value.SubmodelElementValue;
import mdt.model.service.SubmodelService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelServiceClient extends Fa3stHttpClient implements SubmodelService {
	private final String m_url;
	
	public HttpSubmodelServiceClient(OkHttpClient client, String url) {
		super(client);
		
		m_url = url;
	}
	
	public static HttpSubmodelServiceClient newTrustAllSubmodelServiceClient(String url) {
		try {
			OkHttpClient client = SSLUtils.newTrustAllOkHttpClientBuilder().build();
			return new HttpSubmodelServiceClient(client, url);
		}
		catch ( Exception e ) {
			throw new MDTClientException("Failed to create a trust-all client", e);
		}
	}
	
	public String getUrl() {
		return m_url;
	}
	
	@Override
	public Submodel getSubmodel() {
		Request req = new Request.Builder().url(m_url).get().build();
		return call(req, Submodel.class);
	}
	
	@Override
	public Submodel putSubmodel(Submodel aas) {
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(m_url).put(reqBody).build();
			return call(req, Submodel.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public List<SubmodelElement> getAllSubmodelElements() {
		String url = m_url + "/submodel-elements";
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelElement.class);
	}

	@Override
	public SubmodelElement getSubmodelElementByPath(String idShortPath) {
		idShortPath = encodeIdShortPath(idShortPath);
		String url = String.format("%s/submodel-elements/%s", m_url, idShortPath);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, SubmodelElement.class);
	}

	@Override
	public SubmodelElement postSubmodelElement(SubmodelElement element) {
		try {
			String url = m_url + "/submodel-elements";
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public SubmodelElement postSubmodelElementByPath(String idShortPath, SubmodelElement element) {
		try {
			String url = String.format("%s/submodel-elements/%s", m_url, encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public SubmodelElement putSubmodelElementByPath(String idShortPath, SubmodelElement element) {
		try {
			String url = String.format("%s/submodel-elements/%s", m_url, encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void patchSubmodelElementByPath(String idShortPath, SubmodelElement element)
			throws ResourceNotFoundException {
		try {
			String url = String.format("%s/submodel-elements/%s",
										m_url, encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).patch(reqBody).build();
			send(req);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void patchSubmodelElementValueByPath(String idShortPath, SubmodelElementValue value) {
		try {
			String url = String.format("%s/submodel-elements/%s/$value",
										m_url, encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(value.toJsonObject());
			
			Request req = new Request.Builder().url(url).patch(reqBody).build();
			send(req);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void deleteSubmodelElementByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s", m_url, encodeIdShortPath(idShortPath));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	@Override
	public OperationResult invokeOperationSync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout) {
		try {
			String url = String.format("%s/submodel-elements/%s/invoke",
										m_url, encodeIdShortPath(idShortPath));
			DefaultOperationRequest request = new DefaultOperationRequest.Builder()
													.inputArguments(inputArguments)
													.inoutputArguments(inoutputArguments)
													.clientTimeoutDuration(timeout)
													.build();
			RequestBody reqBody = createRequestBody(request);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, OperationResult.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public OperationHandle invokeOperationAsync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout) {
		try {
			String url = String.format("%s/submodel-elements/%s/invoke-async",
										m_url, encodeIdShortPath(idShortPath));
			DefaultOperationRequest request = new DefaultOperationRequest.Builder()
													.inputArguments(inputArguments)
													.inoutputArguments(inoutputArguments)
													.clientTimeoutDuration(timeout)
													.build();
			RequestBody reqBody = createRequestBody(request);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			Tuple<String,String> ret = callAsync(req, String.class);
			
			return new MDTOperationHandle(encodeIdShortPath(idShortPath), ret._1);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public OperationResult getOperationAsyncResult(OperationHandle handle) {
		Preconditions.checkArgument(handle instanceof MDTOperationHandle);

		MDTOperationHandle mdtHandle = (MDTOperationHandle)handle;
		String url = String.format("%s/submodel-elements/%s/operation-results/%s",
									m_url, mdtHandle.getIdShortPathEncoded(), mdtHandle.getHandleId());
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, OperationResult.class);
	}

	@Override
	public BaseOperationResult getOperationAsyncStatus(OperationHandle handle) {
		Preconditions.checkArgument(handle instanceof MDTOperationHandle);
		
		MDTOperationHandle mdtHandle = (MDTOperationHandle)handle;
		String url = String.format("%s/submodel-elements/%s/%s",
									m_url, mdtHandle.getIdShortPathEncoded(), mdtHandle.getStatusLocation());

		Request req = new Request.Builder().url(url).get().build();
		return call(req, BaseOperationResult.class);
	}
	
	private String encodeIdShortPath(String idShortPath) {
		return URLEncoder.encode(idShortPath, StandardCharsets.UTF_8);
	}
}
