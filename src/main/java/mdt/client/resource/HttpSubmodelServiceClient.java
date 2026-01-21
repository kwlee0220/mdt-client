package mdt.client.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.datatype.Duration;

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.Message;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationRequest;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import lombok.Data;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import utils.InternalException;
import utils.Tuple;
import utils.http.OkHttpClientUtils;
import utils.http.RESTfulIOException;
import utils.io.IOUtils;

import mdt.client.Fa3stHttpClient;
import mdt.model.MDTOperationHandle;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelServiceClient extends Fa3stHttpClient implements SubmodelService {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpSubmodelServiceClient.class);
	
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
	public Submodel getSubmodel(Modifier modifier) {
		String endpoint = getEndpoint();
		switch (modifier) {
			case NONE:
				// no-op
				break;
			case METADATA:
				endpoint += "/$metadata";
				break;
			case VALUE:
				throw new IllegalArgumentException("Unsupported modifier: VALUE");
		}
		Request req = new Request.Builder().url(endpoint).get().build();
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
	public SubmodelElement getSubmodelElementByPath(String idShortPath, Modifier modifier) {
		idShortPath = encodeIdShortPath(idShortPath);
		String format = switch (modifier) {
			case NONE -> "%s/submodel-elements/%s";
			case METADATA -> "%s/submodel-elements/%s/$metadata";
			case VALUE -> throw new IllegalArgumentException("Unsupported modifier: VALUE");
		};
		String url = String.format(format, getEndpoint(), idShortPath);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, SubmodelElement.class);
	}

	@Override
	public ElementValue getSubmodelElementValueByPath(String idShortPath, SubmodelElement prototype)
		throws ResourceNotFoundException {
		idShortPath = encodeIdShortPath(idShortPath);
		String url = String.format("%s/submodel-elements/%s/$value", getEndpoint(), idShortPath);
		
		Request req = new Request.Builder().url(url).get().build();
		JsonNode jnode = call(req, JsonNode.class);
		
		JsonNode root = jnode.get(prototype.getIdShort());
		try {
			return ElementValues.parseValueJsonNode(root, prototype);
		}
		catch ( IOException e ) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public SubmodelElement addSubmodelElement(SubmodelElement element) {
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
	public SubmodelElement addSubmodelElementByPath(String idShortPath, SubmodelElement element) {
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
	public SubmodelElement setSubmodelElementByPath(String idShortPath, SubmodelElement element) {
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
	public void updateSubmodelElementByPath(String idShortPath, SubmodelElement element)
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
	public void updateSubmodelElementValueByPath(String idShortPath, String valueJsonString) {
		try {
			String url = String.format("%s/submodel-elements/%s/$value",
										getEndpoint(), encodeIdShortPath(idShortPath));
			RequestBody reqBody = createRequestBody(valueJsonString);
			
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
	
	@Data
	@JsonInclude(Include.NON_NULL)
	public static class OperationResultResponse {
		private List<OperationVariable> inoutputArguments = Lists.newArrayList();
		private List<OperationVariable> outputArguments = Lists.newArrayList();
		private ExecutionState executionState;
		private boolean success;
		private List<Message> messages;
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
			
			if ( resp != null && resp.success ) {
				return new DefaultOperationResult.Builder()
												.inoutputArguments(resp.inoutputArguments)
												.outputArguments(resp.outputArguments)
												.build();
			}
			else {
				throw new RuntimeException("Operation invocation failed: idShortPath=" + idShortPath
											+ ", state=" + resp.executionState + ", messages=" + resp.messages);
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
			
			return new MDTOperationHandle(idShortPath, encodeIdShortPath(idShortPath), ret._1);
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
	public void getAttachmentByPath(String idShortPath, OutputStream output) {
		String url = String.format("%s/submodel-elements/%s/attachment",
									getEndpoint(), encodeIdShortPath(idShortPath));
		Request req = new Request.Builder().url(url).get().build();
		call(req, OutputStream.class, output);
	}

	@Override
	public long putAttachmentByPath(String idShortPath, FileValue mdtFile, InputStream attachment) {
		Preconditions.checkArgument(idShortPath != null, "idShortPath is null");
		Preconditions.checkArgument(mdtFile != null, "AASFile is null");
		Preconditions.checkArgument(attachment != null, "attachment is null");
		
		String url = String.format("%s/submodel-elements/%s/attachment",
									getEndpoint(), encodeIdShortPath(idShortPath));
		
		StreamRequestBody fileBody = new StreamRequestBody(attachment,
															MediaType.parse(mdtFile.getMimeType()));
		RequestBody reqBody = new MultipartBody.Builder()
												.setType(MultipartBody.FORM)
												.addFormDataPart("fileName", mdtFile.getValue())
												.addFormDataPart("content", null, fileBody)
												.build();
		Request req = new Request.Builder().url(url).put(reqBody).build();
		call(req, void.class);
		
		return fileBody.getUploadedBytes();
	}

	@Override
	public void deleteAttachmentByPath(String idShortPath) {
		SubmodelElement sme = getSubmodelElementByPath(idShortPath);
		if ( sme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File fileElm ) {
			if ( fileElm.getValue() != null && !fileElm.getValue().isEmpty() ) {
				String url = String.format("%s/submodel-elements/%s/attachment",
											getEndpoint(), encodeIdShortPath(idShortPath));
				Request req = new Request.Builder().url(url).delete().build();
				call(req, void.class);
			}
		}
		else {
			throw new IllegalArgumentException(
					String.format("the SubmodelElement is not a File: idShortPath=%s", idShortPath));
		}
	}
	
	private String encodeIdShortPath(String idShortPath) {
		return URLEncoder.encode(idShortPath, StandardCharsets.UTF_8);
	}
	
	private static class StreamRequestBody extends RequestBody {
		private final MediaType m_mediaType;
		private final InputStream m_is;
		private long m_uploadeds = 0;
		
		private StreamRequestBody(InputStream is, MediaType mediaType) {
			m_is = is;
			m_mediaType = mediaType;
		}
		
		public long getUploadedBytes() {
			return m_uploadeds;
		}

		@Override
		public MediaType contentType() {
			return m_mediaType;
		}

		@Override
		public void writeTo(okio.BufferedSink sink) throws IOException {
			try ( InputStream input = m_is ) {
				m_uploadeds = IOUtils.copy(input, sink.outputStream());
			}
		}
	}
}
