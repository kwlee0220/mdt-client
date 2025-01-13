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

import mdt.client.HttpMDTManagerClient;
import mdt.client.HttpMDTServiceProxy;
import mdt.client.MDTManagerMqttMessage;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceStatusChangeEvent;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.service.MDTInstance;
import mdt.model.sm.ref.DefaultElementReference;

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
public class HttpMDTInstanceManagerClient implements MDTInstanceManager, HttpMDTServiceProxy {
	private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
	private static final String TOPIC_PREFIX = "/mdt/manager/instances/";
	private static final int TOPIC_PREFIX_LENGTH = TOPIC_PREFIX.length();
	public static EventBus EVENT_BUS = new EventBus();
	
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final Map<String,HttpMDTInstanceClient> m_instances = Maps.newHashMap();
	
	private HttpMDTInstanceManagerClient(Builder builder) {
		m_endpoint = builder.m_endpoint;
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(builder.m_httpClient)
										.jsonMapper(MDTModelSerDe.getJsonMapper())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(InstanceDescriptorSerDe.MAPPER))
										.build();
		
		HttpMDTManagerClient.register(this);
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
					inst.reload();
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
//		return new HttpMDTInstanceClient(this, desc);
		return getOrCreateInstance(desc);
	}

    // @GetMapping({"/instances"})
	@Override
	public List<HttpMDTInstanceClient> getAllInstances() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		return toInstances(m_restfulClient.get(url, m_descListDeser));
	}

    // @GetMapping({"/instances?filter={filter}"})
	@Override
	public List<HttpMDTInstanceClient> getAllInstancesByFilter(String filter) {
		String url = String.format("%s/instances", getEndpoint());
		HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
						 		.addQueryParameter("filter", filter)
					 			.build();
		return toInstances(m_restfulClient.get(httpUrl, m_descListDeser));
	}
	
	@Override
	public HttpMDTInstanceClient getInstanceByAasId(String aasId) throws ResourceNotFoundException {
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<HttpMDTInstanceClient> instList = getAllInstancesByFilter(filter);
		if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			return (HttpMDTInstanceClient)instList.get(0);
		}
	}
	
	@Override
	public List<HttpMDTInstanceClient> getAllInstancesByAasIdShort(String aasIdShort) {
		String filter = String.format("instance.aasIdShort = '%s'", aasIdShort);
		return getAllInstancesByFilter(filter);
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
	public HttpMDTInstanceClient addInstance(String id, int port, File jarFile, File modelFile, File confFile)
		throws MDTInstanceManagerException {
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id)
											.addFormDataPart("port", ""+port);
		if ( jarFile != null ) {
			builder = builder.addFormDataPart("jar", MDTInstanceManager.FA3ST_JAR_FILE_NAME,
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
		return getOrCreateInstance(desc);
	}
	public HttpMDTInstanceClient addInstance(String id, File jarFile, File modelFile, File confFile) {
		return addInstance(id, -1, jarFile, modelFile, confFile);
	}
	
	public HttpMDTInstanceClient addInstance(String id, int port, File instanceDir)
		throws MDTInstanceManagerException {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(instanceDir);
		Preconditions.checkArgument(instanceDir.isDirectory());
		
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id)
											.addFormDataPart("port", ""+port);

		File zippedFile = new File(instanceDir.getParentFile(), id + ".zip");
		try {
			// 주어진 디렉토리를 zip 파일로 만들어서 upload 시킨다.
			ZipFile.zipDirectory(zippedFile.toPath(), instanceDir.toPath());
			builder.addFormDataPart("bundle", id + ".zip", RequestBody.create(zippedFile, ZIP_TYPE));

			String url = String.format("%s/instances", getEndpoint());
			RequestBody reqBody = builder.build();
			InstanceDescriptor desc = m_restfulClient.post(url, reqBody, m_descDeser);
			
			return getOrCreateInstance(desc);
		}
		catch ( IOException e ) {
			throw new MDTInstanceManagerException("Failed to add an instance: dir=" + instanceDir, e);
		}
		finally {
			zippedFile.delete();
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
		
		return getOrCreateInstance(desc);
	}
	
	private HttpMDTInstanceClient getOrCreateInstance(InstanceDescriptor desc) {
		return m_guard.get(() -> {
			HttpMDTInstanceClient inst = m_instances.get(desc.getId());
			if ( inst != null ) {
				inst.setInstanceDescriptor(desc);
			}
			else {
				inst = new HttpMDTInstanceClient(this, desc);
				m_instances.put(desc.getId(), inst);
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
	public void removeAllInstances() throws MDTInstanceManagerException {
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
		throws ResourceNotFoundException, InvalidResourceStatusException {
		return DefaultElementReference.newInstance(this, ref).read();
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		
		private Builder() { }
		
		public HttpMDTInstanceManagerClient build() {
			Preconditions.checkState(m_endpoint != null, "MDTInstanceManager endpoint has not been set");
			Preconditions.checkNotNull(m_httpClient);
			
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
	}
	
	private List<HttpMDTInstanceClient> toInstances(List<InstanceDescriptor> descList) {
		return FStream.from(descList)
						.map(desc -> new HttpMDTInstanceClient(HttpMDTInstanceManagerClient.this, desc))
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
