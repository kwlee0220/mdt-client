package mdt.client.instance;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.google.common.base.Preconditions;

import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.HttpMDTServiceProxy;
import mdt.model.InvalidResourceStatusException;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.service.MDTInstance;
import mdt.model.sm.DefaultSubmodelElementReference;

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
public class HttpMDTInstanceManagerClientOld extends HttpAASRESTfulClient
											implements MDTInstanceManager, HttpMDTServiceProxy {
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	private HttpMDTInstanceManagerClientOld(Builder builder) {
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
		return new HttpMDTInstanceClient(null, desc);
	}

    // @GetMapping({"/instances"})
	@Override
	public List<MDTInstance> getAllInstances() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		Request req = new Request.Builder().url(url).get().build();
		
		String descListJson = call(req, String.class);
		return FStream.from(m_serde.readInstanceDescriptorList(descListJson))
						.map(p -> (MDTInstance)new HttpMDTInstanceClient(null, p))
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
						.map(p -> (MDTInstance)new HttpMDTInstanceClient(null, p))
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
		String url = String.format("%s/instances?aggregate=count", getEndpoint());
		Request req = new Request.Builder().url(url).get().build();
		
		String countStr = call(req, String.class);
		return Long.parseLong(countStr);
	}
	
	private static final MediaType OCTET_TYPE = MediaType.parse("application/octet-stream");
	private static final MediaType JSON_TYPE = MediaType.parse("text/json");
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
		Request req = new Request.Builder().url(url).post(reqBody).build();
		String json = call(req, String.class);
		InstanceDescriptor desc = m_serde.readInstanceDescriptor(json);
		
		return new HttpMDTInstanceClient(null, desc);
	}
	public HttpMDTInstanceClient addInstance(String id, File jarFile, File modelFile, File confFile) {
		return addInstance(id, -1, jarFile, modelFile, confFile);
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
		Request req = new Request.Builder().url(url).post(reqBody).build();
		return call(req, String.class);
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
		return DefaultSubmodelElementReference.newInstance(this, ref).read();
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		
		private Builder() { }
		
		public HttpMDTInstanceManagerClientOld build() {
			Preconditions.checkState(m_endpoint != null, "MDTInstanceManager endpoint has not been set");
			Preconditions.checkNotNull(m_httpClient);
			
//			if ( m_httpClient == null ) {
//				try {
//					m_httpClient = OkHttpClientUtils.newTrustAllOkHttpClientBuilder()
//											.readTimeout(Duration.ofSeconds(30))
//											.build();
//				}
//				catch ( Exception e ) {
//					throw new MDTWorkflowManagerException("" + e);
//				}
//			}
			
			return new HttpMDTInstanceManagerClientOld(this);
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
}
