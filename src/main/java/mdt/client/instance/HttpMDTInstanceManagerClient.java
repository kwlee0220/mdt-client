package mdt.client.instance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.func.Tuple;
import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.HttpServiceFactory;
import mdt.client.MDTClientConfig;
import mdt.model.AASUtils;
import mdt.model.SubmodelUtils;
import mdt.model.instance.AddMDTInstancePayload;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerClient;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.registry.AASRegistry;
import mdt.model.registry.InvalidResourceStatusException;
import mdt.model.registry.RegistryException;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.registry.SubmodelRegistry;
import mdt.model.service.SubmodelService;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * <code>HttpMDTInstanceManagerClient</code>는 HTTP를 기반으로 하여 
 * MDTInstanceManager를 원격으로 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceManagerClient extends HttpAASRESTfulClient
											implements MDTInstanceManagerClient {
	public static final String PATH_INSTANCE_MANAGER = "/instance-manager";
	public static final String PATH_AAS_REPOSITORY = "/shell-registry";
	public static final String PATH_SUBMODEL_REPOSITORY = "/submodel-registry";
	
	private final HttpServiceFactory m_serviceFactory;
	private final String m_baseUrl;
	private final String m_endpoint;
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	public static HttpMDTInstanceManagerClient connect(MDTClientConfig config) {
		return connect(config.getEndpoint());
	}
	
	public static HttpMDTInstanceManagerClient connect(String host, int port) {
		try {
			HttpServiceFactory svcFact = new HttpServiceFactory();
			String endpoint = String.format("http://%s:%d/%s", host, port, PATH_INSTANCE_MANAGER);
			return new HttpMDTInstanceManagerClient(svcFact, endpoint);
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}
	
	public static HttpMDTInstanceManagerClient connect(String endpoint) {
		try {
			HttpServiceFactory svcFact = new HttpServiceFactory();
			return new HttpMDTInstanceManagerClient(svcFact, endpoint);
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}
	
	private HttpMDTInstanceManagerClient(HttpServiceFactory svcFact, String endpoint) {
		super(svcFact.getHttpClient());
		
		m_serviceFactory = svcFact;
		
		int idx = endpoint.lastIndexOf('/');
		m_baseUrl = endpoint.substring(0, idx);
		m_endpoint = endpoint;
	}
	
	public String getEndpoint() {
		return m_endpoint;
	}

	@Override
	public AASRegistry getAssetAdministrationShellRegistry() {
		return m_serviceFactory.getAssetAdministrationShellRegistry(m_baseUrl + PATH_AAS_REPOSITORY);
	}

	@Override
	public SubmodelRegistry getSubmodelRegistry() {
		return m_serviceFactory.getSubmodelRegistry(m_baseUrl + PATH_SUBMODEL_REPOSITORY);
	}

    // @GetMapping({"instances/{id}"})
	InstanceDescriptor getInstanceDescriptor(String id) {
		String url = String.format("%s/instances/%s", m_endpoint, id);

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
		String url = String.format("%s/instances", m_endpoint);
		Request req = new Request.Builder().url(url).get().build();
		
		String descListJson = call(req, String.class);
		return FStream.from(m_serde.readInstanceDescriptorList(descListJson))
						.map(p -> (MDTInstance)new HttpMDTInstanceClient(this, p))
						.toList();
	}

    // @GetMapping({"/instances?filter={filter}"})
	@Override
	public List<MDTInstance> getAllInstancesByFilter(String filter) {
		String url = String.format("%s/instances", m_endpoint);
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
		String url = String.format("%s/instances", m_endpoint);
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

			String url = String.format("%s/instances", m_endpoint);
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
			builder = builder.addFormDataPart("jar", MDTInstanceManager.CANONICAL_FA3ST_JAR_FILE,
												RequestBody.create(jarFile, OCTET_TYPE));
		}
		if ( modelFile != null ) {
			builder = builder.addFormDataPart("initialModel", MDTInstanceManager.CANONICAL_MODEL_FILE,
												RequestBody.create(modelFile, JSON_TYPE));
		}
		if ( confFile != null ) {
			builder = builder.addFormDataPart("instanceConf", MDTInstanceManager.CANONICAL_CONF_FILE,
												RequestBody.create(confFile, JSON_TYPE));
		}

		String url = String.format("%s/instances", m_endpoint);
		RequestBody reqBody = builder.build();
		Request req = new Request.Builder().url(url).post(reqBody).build();
		String json = call(req, String.class);
		InstanceDescriptor desc = m_serde.readInstanceDescriptor(json);
		return new HttpMDTInstanceClient(this, desc);
	}

    // @DeleteMapping("/instances/{id}")
	@Override
	public void removeInstance(String id) throws MDTInstanceManagerException {
		String url = String.format("%s/instances/%s", m_endpoint, id);
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	public void removeInstance(MDTInstance inst) throws MDTInstanceManagerException {
		removeInstance(inst.getId());
	}

    // @DeleteMapping("/instances")
	@Override
	public void removeAllInstances() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", m_endpoint);
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	public SubmodelElement getSubmodelElementByReference(Reference ref)
		throws ResourceNotFoundException, InvalidResourceStatusException, RegistryException {
		Tuple<String,String> info = SubmodelUtils.parseSubmodelReference(ref);
		
		MDTInstance inst = getInstanceBySubmodelId(info._1);
		SubmodelService svc = inst.getSubmodelServiceById(info._1);
		return svc.getSubmodelElementByPath(info._2);
	}
}
