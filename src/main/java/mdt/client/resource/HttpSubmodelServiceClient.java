package mdt.client.resource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.datatype.Duration;

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationRequest;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.InternalException;
import utils.func.Tuple;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;

import lombok.Data;
import mdt.client.Fa3stHttpClient;
import mdt.model.MDTOperationHandle;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.SubmodelService;
import mdt.model.sm.MDTFile;
import mdt.model.sm.value.SubmodelElementValue;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelServiceClient extends Fa3stHttpClient implements SubmodelService {
	public static HttpSubmodelServiceClient newTrustAllSubmodelServiceClient(String url) {
		try {
			OkHttpClient client = OkHttpClientUtils.newTrustAllOkHttpClientBuilder().build();
			return new HttpSubmodelServiceClient(client, url);
		}
		catch ( Exception e ) {
			throw new RESTfulIOException("Failed to create a trust-all client", e);
		}
	}
	
	public HttpSubmodelServiceClient(OkHttpClient client, String endpoint) {
		super(client, endpoint);
	}
	
	@Override
	public Submodel getSubmodel() {
		Request req = new Request.Builder().url(getEndpoint()).get().build();
		return call(req, Submodel.class);
	}
	
	@Override
	public Submodel putSubmodel(Submodel aas) {
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(getEndpoint()).put(reqBody).build();
			return call(req, Submodel.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public List<SubmodelElement> getAllSubmodelElements() {
		String url = getEndpoint() + "/submodel-elements";
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelElement.class);
	}

	@Override
	public SubmodelElement getSubmodelElementByPath(String idShortPath) {
		idShortPath = encodeIdShortPath(idShortPath);
		String url = String.format("%s/submodel-elements/%s", getEndpoint(), idShortPath);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, SubmodelElement.class);
	}

	@Override
	public SubmodelElement postSubmodelElement(SubmodelElement element) {
		try {
			String url = getEndpoint() + "/submodel-elements";
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public SubmodelElement postSubmodelElementByPath(String idShortPath, SubmodelElement element) {
		try {
			String url = String.format("%s/submodel-elements/%s", getEndpoint(), encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public SubmodelElement putSubmodelElementByPath(String idShortPath, SubmodelElement element) {
		try {
			String url = String.format("%s/submodel-elements/%s", getEndpoint(), encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void patchSubmodelElementByPath(String idShortPath, SubmodelElement element)
			throws ResourceNotFoundException {
		try {
			String url = String.format("%s/submodel-elements/%s",
										getEndpoint(), encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).patch(reqBody).build();
			send(req);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void patchSubmodelElementValueByPath(String idShortPath, SubmodelElementValue value) {
		try {
			String url = String.format("%s/submodel-elements/%s/$value",
										getEndpoint(), encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(value);
			
			Request req = new Request.Builder().url(url).patch(reqBody).build();
			send(req);
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public void deleteSubmodelElementByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s", getEndpoint(), encodeIdShortPath(idShortPath));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

//	@Override
//	public byte[] getFileByPath(String idShortPath) {
//		String url = String.format("%s/submodel-elements/%s/attachment",
//									getEndpoint(), encodeIdShortPath(idShortPath));
//		
//		Request req = new Request.Builder().url(url).get().build();
//		return call(req, byte[].class);
//	}
//
//	@Override
//	public void putFileByPath(String idShortPath, byte[] payload) {
//		try {
//			String url = String.format("%s/submodel-elements/%s/attachment",
//										getEndpoint(), encodeIdShortPath(idShortPath));
//			RequestBody reqBody = createRequestBody(payload);
//			
//			Request req = new Request.Builder().url(url).put(reqBody).build();
//			send(req);
//		}
//		catch ( IOException e ) {
//			throw new InternalException("" + e);
//		}
//	}
//
//	@Override
//	public void deleteFileByPath(String idShortPath) {
//		String url = String.format("%s/submodel-elements/%s/attachment",
//									getEndpoint(), encodeIdShortPath(idShortPath));
//		Request req = new Request.Builder().url(url).delete().build();
//		send(req);
//	}
	
	@Data
	@JsonInclude(Include.NON_NULL)
	public static class OperationResultResponse {
		private List<OperationVariable> inoutputArguments = Lists.newArrayList();
		private List<OperationVariable> outputArguments = Lists.newArrayList();
		private ExecutionState executionState;
		private boolean success;
		private List<String> messages;
	}

	@Override
	public OperationResult invokeOperationSync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout) {
		try {
			String url = String.format("%s/submodel-elements/%s/invoke",
										getEndpoint(), encodeIdShortPath(idShortPath));
			DefaultOperationRequest request = new DefaultOperationRequest.Builder()
													.inputArguments(inputArguments)
													.inoutputArguments(inoutputArguments)
													.clientTimeoutDuration(timeout)
													.build();
			RequestBody reqBody = createRequestBody(request);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			OperationResultResponse resp = call(req, OperationResultResponse.class);
			
			if ( resp.success ) {
				return new DefaultOperationResult.Builder()
												.inoutputArguments(resp.inoutputArguments)
												.outputArguments(resp.outputArguments)
												.build();
			}
			else {
				throw new RuntimeException("Operation invocation failed: idShortPath=" + idShortPath);
			}
		}
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public OperationHandle invokeOperationAsync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout) {
		try {
			String url = String.format("%s/submodel-elements/%s/invoke-async",
										getEndpoint(), encodeIdShortPath(idShortPath));
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
		catch ( IOException e ) {
			throw new InternalException("" + e);
		}
	}

	@Override
	public OperationResult getOperationAsyncResult(OperationHandle handle) {
		Preconditions.checkArgument(handle instanceof MDTOperationHandle);

		MDTOperationHandle mdtHandle = (MDTOperationHandle)handle;
		String url = String.format("%s/submodel-elements/%s/operation-results/%s",
									getEndpoint(), mdtHandle.getIdShortPathEncoded(), mdtHandle.getHandleId());
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, OperationResult.class);
	}

	@Override
	public BaseOperationResult getOperationAsyncStatus(OperationHandle handle) {
		Preconditions.checkArgument(handle instanceof MDTOperationHandle);
		
		MDTOperationHandle mdtHandle = (MDTOperationHandle)handle;
		String url = String.format("%s/submodel-elements/%s/%s",
									getEndpoint(), mdtHandle.getIdShortPathEncoded(), mdtHandle.getStatusLocation());

		Request req = new Request.Builder().url(url).get().build();
		return call(req, BaseOperationResult.class);
	}

	@Override
	public MDTFile getFileByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s/attachment",
									getEndpoint(), encodeIdShortPath(idShortPath));
		Request req = new Request.Builder().url(url).get().build();
		MDTFile mdtFile = call(req, MDTFile.class);
		
		File aasFile = (File)getSubmodelElementByPath(idShortPath);
		mdtFile.setPath(aasFile.getValue());
		
		return mdtFile;
	}

	@Override
	public byte[] getFileContentByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s/attachment",
									getEndpoint(), encodeIdShortPath(idShortPath));
		Request req = new Request.Builder().url(url).get().build();
		return call(req, byte[].class);
	}

	@Override
	public void putFileByPath(String idShortPath, MDTFile mdtFile) {
		MultipartBody.Builder builder
			= new MultipartBody.Builder()
								.setType(MultipartBody.FORM)
								.addFormDataPart("fileName", mdtFile.getPath())
								.addFormDataPart("contentType", mdtFile.getContentType())
								.addFormDataPart("content", null,
												RequestBody.create(mdtFile.getContent(), mdtFile.getMediaType()));

		String url = String.format("%s/submodel-elements/%s/attachment",
									getEndpoint(), encodeIdShortPath(idShortPath));
		RequestBody reqBody = builder.build();
		Request req = new Request.Builder().url(url).put(reqBody).build();
		call(req, void.class);
	}

	@Override
	public void deleteFileByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s/attachment",
									getEndpoint(), encodeIdShortPath(idShortPath));
		Request req = new Request.Builder().url(url).delete().build();
		call(req, void.class);
	}
	
	private String encodeIdShortPath(String idShortPath) {
		return URLEncoder.encode(idShortPath, StandardCharsets.UTF_8);
	}
}
