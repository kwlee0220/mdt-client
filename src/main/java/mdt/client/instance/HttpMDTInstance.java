package mdt.client.instance;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import utils.InternalException;
import utils.StateChangePoller;
import utils.Throwables;
import utils.func.Funcs;
import utils.func.Try;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.stream.FStream;

import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;
import mdt.model.DescriptorUtils;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.MDTModelService;
import mdt.model.sm.info.CompositionItem;
import mdt.model.sm.info.TwinComposition;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


/**
 * <code>HttpMDTInstanceClient</code>는 HTTP를 기반으로 하여 
 * {@link MDTInstance}를 원격에서 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstance implements MDTInstance, HttpClientProxy {
	private static final RequestBody EMPTY_BODY = RequestBody.create("", null);
	
	private final String m_id;
	private final String m_endpoint;
	private final HttpMDTInstanceManager m_manager;
	private final HttpRESTfulClient m_restfulClient;
	private final AtomicReference<InstanceDescriptor> m_desc;
	
	HttpMDTInstance(HttpMDTInstanceManager manager, InstanceDescriptor desc) {
		m_manager = manager;
		
		m_id = desc.getId();
		m_endpoint = String.format("%s/instances/%s", manager.getEndpoint(), desc.getId());
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(manager.getHttpClient())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(InstanceDescriptorSerDe.MAPPER))
										.build();
		
		m_desc = new AtomicReference<>(desc);
	}

	@Override
	public MDTInstanceManager getInstanceManager() {
		return m_manager;
	}
	
	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public MDTInstanceStatus getStatus() {
		return m_desc.get().getStatus();
	}

	@Override
	public String getBaseEndpoint() {
		return m_desc.get().getBaseEndpoint();
	}
	
	@Override
	public String getAasId() {
		return m_desc.get().getAasId();
	}

	@Override
	public String getAasIdShort() {
		return m_desc.get().getAasIdShort();
	}

	@Override
	public String getGlobalAssetId() {
		return m_desc.get().getGlobalAssetId();
	}

	@Override
	public String getAssetType() {
		return m_desc.get().getAssetType();
	}

	@Override
	public AssetKind getAssetKind() {
		return m_desc.get().getAssetKind();
	}

	@Override
	public InstanceDescriptor getInstanceDescriptor() {
		return m_desc.get();
	}

	@Override
	public void start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException {
		MDTInstanceStatus status = reloadInstanceDescriptor().getStatus();
		if ( status != MDTInstanceStatus.STOPPED && status != MDTInstanceStatus.FAILED ) {
			throw new InvalidResourceStatusException("MDTInstance", getId(), status);
		}
		
		status = sendStartRequest().getStatus();
		switch ( status ) {
			case RUNNING:
				return;
			case STARTING:
				// pollInterval 값이 null인 경우는 MDTInstance의 시작 요청만 내린 상태에서 바로 반환한다.
				if ( pollInterval != null ) {
					try {
						waitWhileStatus(state -> state == MDTInstanceStatus.STARTING, pollInterval, timeout);
					}
					catch ( ExecutionException e ) {
						// 상태 체크만 하던 과정에서 오류가 발생할 경우가 거의(?) 없기 때문에
						// 이 경우는 InternalException을 발생시킨다. 
						throw new InternalException(Throwables.unwrapThrowable(e));
					}
				}
				break;
			default:
				throw new InvalidResourceStatusException("MDTInstance", getId(), status);
		}
	}

	@Override
	public void stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException, ExecutionException {
		Instant due = null;
		if ( timeout != null ) {
			due = Instant.now().plus(timeout);
		}
		
		MDTInstanceStatus status = sendStopRequest().getStatus();
		switch ( status ) {
			case STOPPED:
				return;
			case STOPPING:
				if ( pollInterval != null ) {
					waitWhileStatus(state -> state == MDTInstanceStatus.STOPPING, pollInterval, due);
				}
				break;
			default:
				throw new InvalidResourceStatusException("MDTInstance", getId(), status);
		}
	}

	@Override
	public HttpAASServiceClient getAssetAdministrationShellService() {
		String endpoint = DescriptorUtils.toAASServiceEndpointString(getBaseEndpoint(), getAasId());
		if ( endpoint == null ) {
			throw new InvalidResourceStatusException("AssetAdministrationShell",
													String.format("mdt=%s, id=%s", getId(), getAasId()),
													getStatus());
		}
		return new HttpAASServiceClient(getHttpClient(), endpoint);
	}

	@Override
	public List<SubmodelService> getSubmodelServiceAll() throws InvalidResourceStatusException {
		return FStream.from(getInstanceSubmodelDescriptorAll())
						.map(desc -> toSubmodelService(desc.getId()))
						.toList();
	}

	@Override
	public SubmodelService getSubmodelServiceById(String submodelId) {
		return Funcs.findFirst(getInstanceSubmodelDescriptorAll(), isd -> isd.getId().equals(submodelId))
						.map(desc -> toSubmodelService(desc.getId()))
						.getOrThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}

	@Override
	public SubmodelService getSubmodelServiceByIdShort(String submodelIdShort) {
		return Funcs.findFirst(getInstanceSubmodelDescriptorAll(), isd -> isd.getIdShort().equals(submodelIdShort))
						.map(desc -> toSubmodelService(desc.getId()))
						.getOrThrow(() -> new ResourceNotFoundException("Submodel", "idShort=" + submodelIdShort));
	}

	@Override
	public List<SubmodelService> getSubmodelServiceAllBySemanticId(String semanticId) {
		return FStream.from(getInstanceSubmodelDescriptorAll())
						.filter(isd -> isd.getSemanticId().equals(semanticId))
						.map(desc -> toSubmodelService(desc.getId()))
						.toList();
	}

    // @GetMapping({"instances/{id}/aas_descriptor"})
	@Override
	public AssetAdministrationShellDescriptor getAASDescriptor() {
		String url = String.format("%s/aas_descriptor", getEndpoint());
		return m_restfulClient.get(url, m_aasDeser);
	}

    // @GetMapping({"instances/{id}/submodel_descriptors"})
	@Override
	public List<SubmodelDescriptor> getSubmodelDescriptorAll() {
		String url = String.format("%s/submodel_descriptors", getEndpoint());
		return m_restfulClient.get(url, m_smListDeser);
	}

    // @GetMapping({"instances/{id}/log"})
	@Override
	public String getOutputLog() throws IOException {
		String url = String.format("%s/log", getEndpoint());
		return m_restfulClient.get(url, HttpRESTfulClient.STRING_DESER);
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}
	
	public void waitWhileStatus(Predicate<MDTInstanceStatus> waitCond, Duration pollInterval, Instant due)
		throws TimeoutException, InterruptedException, ExecutionException {
		StateChangePoller.pollWhile(() -> waitCond.test(reloadInstanceDescriptor().getStatus()))
						.pollInterval(pollInterval)
						.due(due)
						.build()
						.run();
	}
	public void waitWhileStatus(Predicate<MDTInstanceStatus> waitCond, Duration pollInterval, Duration timeout)
		throws TimeoutException, InterruptedException, ExecutionException {
		StateChangePoller.pollWhile(() -> waitCond.test(reloadInstanceDescriptor().getStatus()))
						.pollInterval(pollInterval)
						.timeout(timeout)
						.build()
						.run();
	}
	
	public static void waitWhileStatus(List<HttpMDTInstance> instances, Predicate<MDTInstance> pred,
										Duration pollInterval, Duration timeout)
		throws TimeoutException, InterruptedException, ExecutionException {
		StateChangePoller.pollWhile(() -> {
			for ( HttpMDTInstance inst: instances ) {
				if ( !pred.test(inst) ) {
					instances.remove(inst);
				}
			}
			return instances.size() > 0;
		})
		.pollInterval(pollInterval)
		.timeout(timeout)
		.build()
		.run();
	}
	
	public List<HttpMDTInstance> getComponentAll() {
		return getTargetOfDependency("contain");
	}
	
	public List<HttpMDTInstance> getTargetOfDependency(String depType) {
		MDTModelService mdtInfo =  MDTModelService.of(this);
		
		TwinComposition tcomp = mdtInfo.getInformationModel().getTwinComposition();
		String myId = tcomp.getCompositionID();

		Map<String,CompositionItem> itemMap = FStream.from(tcomp.getCompositionItems())
													.tagKey(item -> item.getID())
													.toMap();
		return FStream.from(tcomp.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals(depType) && dep.getSourceId().equals(myId))
						.flatMapNullable(dep -> itemMap.get(dep.getTargetId()))
						.map(CompositionItem::getReference)
						.flatMapTry(aasId -> Try.get(() -> m_manager.getInstanceByAasId(aasId)))
						.toList();
	}
	
	public List<HttpMDTInstance> getSourceInstanceAll(String depType) {
		MDTModelService mdtInfo =  MDTModelService.of(this);
		
		TwinComposition tcomp = mdtInfo.getInformationModel().getTwinComposition();
		String myId = tcomp.getCompositionID();

		Map<String,CompositionItem> itemMap = FStream.from(tcomp.getCompositionItems())
													.tagKey(item -> item.getID())
													.toMap();
		return FStream.from(tcomp.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals(depType) && dep.getSourceId().equals(myId))
						.flatMapNullable(dep -> itemMap.get(dep.getTargetId()))
						.map(CompositionItem::getReference)
						.flatMapTry(aasId -> Try.get(() -> m_manager.getInstanceByAasId(aasId)))
						.toList();
	}
	
	@Override
	public String toString() {
		String submodelIdStr = FStream.from(getInstanceSubmodelDescriptorAll())
										.map(InstanceSubmodelDescriptor::getIdShort)
										.join(", ");
		return String.format("[%s] AAS=%s SubmodelIdShorts=(%s) status=%s",
								getId(), getAasId(), submodelIdStr, getStatus());
	}
	
	private InstanceDescriptor reloadInstanceDescriptor() {
		InstanceDescriptor desc = m_manager.getInstanceDescriptor(m_id);
		m_desc.set(desc);
		return desc;
	}
	
    // @PutMapping({"instance-manager/instances/{id}/start"})
	private InstanceDescriptor sendStartRequest() {
		String url = String.format("%s/start", getEndpoint());
		return m_restfulClient.put(url, EMPTY_BODY, m_descDeser);
	}

    // @PutMapping({"instance-manager/instances/{id}/stop"})
	private InstanceDescriptor sendStopRequest() {
		String url = String.format("%s/stop", getEndpoint());
		return m_restfulClient.put(url, EMPTY_BODY, m_descDeser);
	}
	
	private SubmodelService toSubmodelService(String id) {
		String baseEndpoint = getBaseEndpoint();
		if ( baseEndpoint == null ) {
			throw new InvalidResourceStatusException("MDTInstance", "id=" + getId(), getStatus());
		}
		
		String encodedSubmodelId = AASUtils.encodeBase64UrlSafe(id);
		String smSvcEndpoint = String.format("%s/submodels/%s", baseEndpoint, encodedSubmodelId);
		return new HttpSubmodelServiceClient(getHttpClient(), smSvcEndpoint);
	}

	public String getSubmodelServiceEndpoint(String submodelId) {
		String instanceServiceEndpoint = getBaseEndpoint();
		if ( instanceServiceEndpoint != null ) {
			String encodedSubmodelId = AASUtils.encodeBase64UrlSafe(submodelId);
			return String.format("%s/submodels/%s", instanceServiceEndpoint, encodedSubmodelId);
		}
		else {
			return null;
		}
	}
	
	private ResponseBodyDeserializer<AssetAdministrationShellDescriptor> m_aasDeser = new ResponseBodyDeserializer<>() {
		@Override
		public AssetAdministrationShellDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValue(respBody, AssetAdministrationShellDescriptor.class);
		}
	};
	private ResponseBodyDeserializer<List<SubmodelDescriptor>> m_smListDeser = new ResponseBodyDeserializer<>() {
		@Override
		public List<SubmodelDescriptor> deserialize(Headers headers, String respBody) throws IOException {
			return MDTModelSerDe.readValueList(respBody, SubmodelDescriptor.class);
		}
	};

	private static final InstanceDescriptorSerDe INST_DESC_SERDE = new InstanceDescriptorSerDe();
	private ResponseBodyDeserializer<InstanceDescriptor> m_descDeser = new ResponseBodyDeserializer<>() {
		@Override
		public InstanceDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return INST_DESC_SERDE.readInstanceDescriptor(respBody);
		}
	};
}
