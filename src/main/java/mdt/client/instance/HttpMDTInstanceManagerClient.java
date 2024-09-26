package mdt.client.instance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;

import utils.func.FOption;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.HttpMDTServiceProxy;
import mdt.client.SSLUtils;
import mdt.model.AASUtils;
import mdt.model.InvalidResourceStatusException;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelUtils;
import mdt.model.instance.AddMDTInstancePayload;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.service.SubmodelService;
import mdt.model.workflow.MDTWorkflowManagerException;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * <code>HttpMDTInstanceManagerClient</code>는 HTTP를 기반으로 하여 
 * MDTInstanceManager를 원격으로 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceManagerClient extends HttpAASRESTfulClient
											implements MDTInstanceManager, HttpMDTServiceProxy {
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	private HttpMDTInstanceManagerClient(Builder builder) {
		super(builder.m_httpClient, builder.m_endpoint);
	}

    // @GetMapping({"instances/{id}"})
	InstanceDescriptor getInstanceDescriptor(String id) {
		String url = String.format("%s/instances/%s", getEndpoint(), id);

		Request req = new Request.Builder().url(url).get().build();
		String json = call(req, String.class);
		if ( json != null ) {
			return m_serde.readInstanceDescriptor(json);
		}
		else {
			throw new ResourceNotFoundException("MDTInstance", "id=" + id);
		}
	}

	@Override
	public HttpMDTInstanceClient getInstance(String id) throws MDTInstanceManagerException {
		InstanceDescriptor desc = getInstanceDescriptor(id);
		return new HttpMDTInstanceClient(this, desc);
	}

    // @GetMapping({"/instances"})
	@Override
	public List<MDTInstance> getAllInstances() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		Request req = new Request.Builder().url(url).get().build();
		
		String descListJson = call(req, String.class);
		return FStream.from(m_serde.readInstanceDescriptorList(descListJson))
						.map(p -> (MDTInstance)new HttpMDTInstanceClient(this, p))
						.toList();
	}

    // @GetMapping({"/instances?filter={filter}"})
	@Override
	public List<MDTInstance> getAllInstancesByFilter(String filter) {
		String url = String.format("%s/instances", getEndpoint());
		HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
						 		.addQueryParameter("filter", filter)
					 			.build();
		Request req = new Request.Builder().url(httpUrl).get().build();
		String descListJson = call(req, String.class);
		return FStream.from(m_serde.readInstanceDescriptorList(descListJson))
						.map(p -> (MDTInstance)new HttpMDTInstanceClient(this, p))
						.toList();
	}
	
	@Override
	public HttpMDTInstanceClient getInstanceByAasId(String aasId) throws ResourceNotFoundException {
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<MDTInstance> instList = getAllInstancesByFilter(filter);
		if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			return (HttpMDTInstanceClient)instList.get(0);
		}
	}
	
	@Override
	public List<MDTInstance> getAllInstancesByAasIdShort(String aasIdShort) {
		String filter = String.format("instance.aasIdShort = '%s'", aasIdShort);
		return getAllInstancesByFilter(filter);
	}

    // @GetMapping({"/instances?aggregate=count"})
	@Override
	public long countInstances() {
		String url = String.format("%s/instances", getEndpoint());
		Request req = new Request.Builder().url(url).get().build();
		
		String countStr = call(req, String.class);
		return Long.parseLong(countStr);
	}

    // @PostMapping({"/instances"})
	@Override
	public HttpMDTInstanceClient addInstance(String id, File aasFile, String arguments)
		throws MDTInstanceManagerException {
		try {
			// AAS Environment 정의 파일을 읽어서 AAS Registry에 등록한다.
			Environment env = AASUtils.readEnvironment(aasFile);
			
			AddMDTInstancePayload add = new AddMDTInstancePayload(id, env, arguments);
			RequestBody reqBody = createRequestBody(add);

			String url = String.format("%s/instances", getEndpoint());
			Request req = new Request.Builder().url(url).post(reqBody).build();
			String json = call(req, String.class);
			InstanceDescriptor desc = m_serde.readInstanceDescriptor(json);
			
			return new HttpMDTInstanceClient(this, desc);
		}
		catch ( IOException | SerializationException e ) {
			String params = String.format("%s: (%s)", id, arguments);
			throw new MDTInstanceManagerException("failed to register an instance: "
													+ params + ", cause=" + e);
		}
	}
	
	private static final MediaType OCTET_TYPE = MediaType.parse("application/octet-stream");
	private static final MediaType JSON_TYPE = MediaType.parse("text/json");
    // @PostMapping({"/instances"})
	public HttpMDTInstanceClient addInstance(String id, String imageId, File jarFile, File modelFile,
												File confFile)
		throws MDTInstanceManagerException {
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id);
		if ( imageId != null ) {
			builder = builder.addFormDataPart("imageId", imageId);
		}
		if ( jarFile != null ) {
			builder = builder.addFormDataPart("jar", MDTInstanceManager.FA3ST_JAR_FILE_NAME,
												RequestBody.create(jarFile, OCTET_TYPE));
		}
		if ( modelFile != null ) {
			builder = builder.addFormDataPart("initialModel", MDTInstanceManager.MODEL_FILE_NAME,
												RequestBody.create(modelFile, JSON_TYPE));
		}
		if ( confFile != null ) {
			builder = builder.addFormDataPart("instanceConf", MDTInstanceManager.CONF_FILE_NAME,
												RequestBody.create(confFile, JSON_TYPE));
		}

		String url = String.format("%s/instances", getEndpoint());
		RequestBody reqBody = builder.build();
		Request req = new Request.Builder().url(url).post(reqBody).build();
		String json = call(req, String.class);
		InstanceDescriptor desc = m_serde.readInstanceDescriptor(json);
		return new HttpMDTInstanceClient(this, desc);
	}

    // @DeleteMapping("/instances/{id}")
	@Override
	public void removeInstance(String id) throws MDTInstanceManagerException {
		String url = String.format("%s/instances/%s", getEndpoint(), id);
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	public void removeInstance(MDTInstance inst) throws MDTInstanceManagerException {
		removeInstance(inst.getId());
	}

    // @DeleteMapping("/instances")
	@Override
	public void removeAllInstances() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	/**
	 * 모델 {@link Reference}에 해당하는 {@link SubmodelElement} 객체를 반환한다.
	 * 
	 *  @param ref	접근하고자 하는 Reference 객체.
	 *  @return	{@link SubmodelElement} 객체
	 *  @throws	ResourceNotFoundException	Reference에 해당하는 SubmodelElement가 존재하지 않는 경우.
	 *  @throws InvalidResourceStatusException	Reference에 해당하는 SubmodelElement를 포함한
	 *  					MDTInstnace가 RUNNING 상태가 아닌 경우.
	 */
	public SubmodelElement getSubmodelElementByReference(Reference ref)
		throws ResourceNotFoundException, InvalidResourceStatusException {
		Tuple<String,String> info = SubmodelUtils.parseSubmodelReference(ref);
		
		MDTInstance inst = getInstanceBySubmodelId(info._1);
		SubmodelService svc = inst.getSubmodelServiceById(info._1);
		return svc.getSubmodelElementByPath(info._2);
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		private JsonMapper m_mapper;
		
		private Builder() { }
		
		public HttpMDTInstanceManagerClient build() {
			Preconditions.checkState(m_endpoint != null, "MDTInstanceManager endpoint has not been set");
			
			if ( m_httpClient == null ) {
				try {
					m_httpClient = SSLUtils.newTrustAllOkHttpClientBuilder().build();
				}
				catch ( Exception e ) {
					throw new MDTWorkflowManagerException("" + e);
				}
			}
			m_mapper = FOption.getOrElse(m_mapper, AASUtils::getJsonMapper);
			
			return new HttpMDTInstanceManagerClient(this);
		}
		
		public Builder httpClient(OkHttpClient client) {
			m_httpClient = client;
			return this;
		}
		
		public Builder endpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
		
		public Builder jsonMapper(JsonMapper mapper) {
			m_mapper = mapper;
			return this;
		}
	}
}
