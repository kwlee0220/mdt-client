package mdt.client.instance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;

import utils.async.Guard;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.io.ZipFile;
import utils.stream.FStream;

import mdt.client.HttpMDTManager;
import mdt.client.HttpMDTServiceProxy;
import mdt.client.MDTManagerMqttMessage;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceStatusChangeEvent;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.ref.ResolvedElementReference;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


/**
 * <code>HttpMDTInstanceManagerClient</code>는 HTTP를 기반으로 하여 
 * MDTInstanceManager를 원격으로 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceManager implements MDTInstanceManager, HttpMDTServiceProxy {
	private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
	private static final String TOPIC_PREFIX = "/mdt/manager/instances/";
	private static final int TOPIC_PREFIX_LENGTH = TOPIC_PREFIX.length();
	public static EventBus EVENT_BUS = new EventBus();
	
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final Map<String,HttpMDTInstanceClient> m_instances = Maps.newHashMap();
	
	private HttpMDTInstanceManager(Builder builder) {
		m_endpoint = builder.m_endpoint;
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(builder.m_httpClient)
										.jsonMapper(MDTModelSerDe.getJsonMapper())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(InstanceDescriptorSerDe.MAPPER))
										.build();
		
		HttpMDTManager.register(this);
	}
	
	@Subscribe
	public void instanceStatusChanged(MDTManagerMqttMessage msg) {
		String topic = msg.getTopic();
		if ( !topic.startsWith(TOPIC_PREFIX) ) {
			return;
		}
		
		String instId = topic.substring(TOPIC_PREFIX_LENGTH);
		m_guard.lock();
		try {
			HttpMDTInstanceClient inst = m_instances.get(instId);
			if ( inst == null ) {
				return;
			}

			String payload = new String(msg.getMessage().getPayload(), StandardCharsets.UTF_8);
			InstanceStatusChangeEvent event = MAPPER.readValue(payload, InstanceStatusChangeEvent.class);
			switch ( event.getInstanceStatus() ) {
				case RUNNING:
				case STOPPED:
					// 일단 아무런 처리를 하지 않음.
					break;
				default:
					break;
			}
		}
		catch ( Throwable ignored ) { }
		finally {
			m_guard.unlock();
		}
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

    // @GetMapping({"instances/{id}"})
	InstanceDescriptor getInstanceDescriptor(String id) {
		String url = String.format("%s/instances/%s", getEndpoint(), id);
		return m_restfulClient.get(url, m_descDeser);
	}

	@Override
	public HttpMDTInstanceClient getInstance(String id) throws MDTInstanceManagerException {
		InstanceDescriptor desc = getInstanceDescriptor(id);
		return new HttpMDTInstanceClient(this, desc);
//		return createInstance(desc);
	}

    // @GetMapping({"/instances"})
	@Override
	public List<HttpMDTInstanceClient> getInstanceAll() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		return toInstances(m_restfulClient.get(url, m_descListDeser));
	}

    // @GetMapping({"/instances?filter={filter}"})
	@Override
	public List<HttpMDTInstanceClient> getInstanceAllByFilter(String filter) {
		String url = String.format("%s/instances", getEndpoint());
		HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
						 		.addQueryParameter("filter", filter)
					 			.build();
		return toInstances(m_restfulClient.get(httpUrl, m_descListDeser));
	}
	
	@Override
	public HttpMDTInstanceClient getInstanceByAasId(String aasId) throws ResourceNotFoundException {
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<HttpMDTInstanceClient> instList = getInstanceAllByFilter(filter);
		if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			return (HttpMDTInstanceClient)instList.get(0);
		}
	}
	
	@Override
	public List<HttpMDTInstanceClient> getInstanceAllByAasIdShort(String aasIdShort) {
		String filter = String.format("instance.aasIdShort = '%s'", aasIdShort);
		return getInstanceAllByFilter(filter);
	}

    // @GetMapping({"/instances?aggregate=count"})
	@Override
	public long countInstances() {
		String url = String.format("%s/instances?aggregate=count", getEndpoint());
		String countStr = m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
		return Long.parseLong(countStr);
	}
	
	private static final MediaType OCTET_TYPE = MediaType.parse("application/octet-stream");
	private static final MediaType JSON_TYPE = MediaType.parse("text/json");
	private static final MediaType ZIP_TYPE = MediaType.parse("application/zip");
    // @PostMapping({"/instances"})
	public HttpMDTInstanceClient addInstance(String id, File jarFile, File modelFile, File confFile)
		throws MDTInstanceManagerException {
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id);
		if ( jarFile != null ) {
			builder = builder.addFormDataPart("jar", MDTInstanceManager.MDT_INSTANCE_JAR_FILE_NAME,
												RequestBody.create(jarFile, OCTET_TYPE));
		}
		if ( modelFile != null ) {
			if ( FilenameUtils.getExtension(modelFile.getName()).equals("aasx") ) {
				builder = builder.addFormDataPart("initialModel", MDTInstanceManager.MODEL_AASX_NAME,
													RequestBody.create(modelFile, OCTET_TYPE));
			}
			else {
				builder = builder.addFormDataPart("initialModel", MDTInstanceManager.MODEL_FILE_NAME,
													RequestBody.create(modelFile, JSON_TYPE));
			}
		}
		if ( confFile != null ) {
			builder = builder.addFormDataPart("instanceConf", MDTInstanceManager.CONF_FILE_NAME,
												RequestBody.create(confFile, JSON_TYPE));
		}

		String url = String.format("%s/instances", getEndpoint());
		RequestBody reqBody = builder.build();
		InstanceDescriptor desc = m_restfulClient.post(url, reqBody, m_descDeser);
		
//		return new HttpMDTInstanceClient(this, desc);
		return createInstance(desc);
	}
	
	public HttpMDTInstanceClient addInstance(String id, File instanceFile)
		throws MDTInstanceManagerException {
		Preconditions.checkArgument(id != null, "MDTInstance id must not be null");
		Preconditions.checkArgument(instanceFile != null, "MDTInstance file must not be null");
		
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id);
		
		File zippedFile = instanceFile;
		if ( instanceFile.isDirectory() ) {
			try {
				// Instance 파일이 디렉토리인 경우, zip 파일로 압축한다.
				zippedFile = new File(instanceFile.getParentFile(), id + ".zip");
				ZipFile.zipDirectory(zippedFile.toPath(), instanceFile.toPath());
			}
			catch ( IOException e ) {
				throw new MDTInstanceManagerException("Failed to add an instance: dir=" + instanceFile, e);
			}
		}
		else if ( !Files.getFileExtension(zippedFile.getAbsolutePath()).equals("zip") ) {
			throw new MDTInstanceManagerException("MDTInstance file must be a zip file: " + zippedFile);
		}

		try {
			builder.addFormDataPart("bundle", id + ".zip", RequestBody.create(zippedFile, ZIP_TYPE));

			String url = String.format("%s/instances", getEndpoint());
			RequestBody reqBody = builder.build();
			InstanceDescriptor desc = m_restfulClient.post(url, reqBody, m_descDeser);
			
			return createInstance(desc);
		}
		finally {
			if ( zippedFile != instanceFile ) {
				// 임시로 생성한 zip 파일을 삭제한다.
				// (디렉토리인 경우에만 zip 파일로 압축하고, 압축한 후에는 삭제한다.)
				zippedFile.delete();
			}
		}
	}
	
	public HttpMDTInstanceClient addZippedInstance(String id, int port, File zippedInstanceDir) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(zippedInstanceDir);
		Preconditions.checkArgument(Files.getFileExtension(zippedInstanceDir.getAbsolutePath()).equals("zip"));
		
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id)
											.addFormDataPart("port", ""+port);
		
		// 주어진 디렉토리를 zip 파일로 만들어서 upload 시킨다.
		builder.addFormDataPart("bundle", id + ".zip", RequestBody.create(zippedInstanceDir, ZIP_TYPE));

		String url = String.format("%s/instances", getEndpoint());
		RequestBody reqBody = builder.build();
		InstanceDescriptor desc = m_restfulClient.post(url, reqBody, m_descDeser);
		
		return createInstance(desc);
	}
	
	private HttpMDTInstanceClient createInstance(InstanceDescriptor desc) throws ResourceAlreadyExistsException {
		return m_guard.get(() -> {
			HttpMDTInstanceClient inst = new HttpMDTInstanceClient(this, desc);
			HttpMDTInstanceClient prev = m_instances.putIfAbsent(desc.getId(), inst);
			if ( prev != null ) {
				throw new ResourceAlreadyExistsException("MDTInstance", "id=" + desc.getId());
			}
			
			return inst;
		});
	}

	public String bundleInstance(String id, File jarFile, File modelFile, File confFile)
		throws MDTInstanceManagerException {
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id);
		if ( jarFile != null ) {
			builder = builder.addFormDataPart("jar", MDTInstanceManager.MDT_INSTANCE_JAR_FILE_NAME,
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

		String url = String.format("%s/bundles", getEndpoint());
		RequestBody reqBody = builder.build();
		return m_restfulClient.post(url, reqBody, HttpRESTfulClient.STRING_DESER);
	}

    // @DeleteMapping("/instances/{id}")
	@Override
	public void removeInstance(String id) throws MDTInstanceManagerException {
		String url = String.format("%s/instances/%s", getEndpoint(), id);
		
		m_restfulClient.delete(url);
	}
	public void removeInstance(MDTInstance inst) throws MDTInstanceManagerException {
		removeInstance(inst.getId());
	}

    // @DeleteMapping("/instances")
	@Override
	public void removeInstanceAll() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		
		m_restfulClient.delete(url);
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
		throws ResourceNotFoundException, InvalidResourceStatusException, IOException {
		DefaultElementReference elmRef = DefaultElementReference.newInstance(ref);
		elmRef.activate(this);
		return elmRef.read();
	}

    // @GetMapping({"/utils/resolveElementReference?ref={ref}&encode={encode}"})
	@Override
	public ResolvedElementReference resolveElementReference(String ref) {
		String url = String.format("%s/utils/resolveElementReference", getEndpoint());
		HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
						 		.addQueryParameter("ref", ref)
					 			.build();
		return m_restfulClient.get(httpUrl, RESOLVED_REFERENCE_DESER);
	}
	private static ResponseBodyDeserializer<ResolvedElementReference> RESOLVED_REFERENCE_DESER = new ResponseBodyDeserializer<>() {
		@Override
		public ResolvedElementReference deserialize(Headers headers, String respBody) throws IOException {
			return MAPPER.readValue(respBody, ResolvedElementReference.class);
		}
	};

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		
		private Builder() { }
		
		public HttpMDTInstanceManager build() {
			Preconditions.checkState(m_endpoint != null, "MDTInstanceManager endpoint has not been set");
			Preconditions.checkNotNull(m_httpClient);
			
			return new HttpMDTInstanceManager(this);
		}
		
		public Builder httpClient(OkHttpClient client) {
			m_httpClient = client;
			return this;
		}
		
		public Builder endpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
	}
	
	private List<HttpMDTInstanceClient> toInstances(List<InstanceDescriptor> descList) {
		return FStream.from(descList)
						.map(desc -> new HttpMDTInstanceClient(HttpMDTInstanceManager.this, desc))
						.toList();
	}
	
	private ResponseBodyDeserializer<InstanceDescriptor> m_descDeser = new ResponseBodyDeserializer<>() {
		@Override
		public InstanceDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return m_serde.readInstanceDescriptor(respBody);
		}
	};
	
	private ResponseBodyDeserializer<List<InstanceDescriptor>> m_descListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<InstanceDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return m_serde.readInstanceDescriptorList(respBody);
		}
	};
}
