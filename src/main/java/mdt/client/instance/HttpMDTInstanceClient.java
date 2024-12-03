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
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import utils.InternalException;
import utils.StateChangePoller;
import utils.Throwables;
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.Try;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
import utils.http.HttpRESTfulClient.ResponseBodyDeserializer;
import utils.http.JacksonErrorEntityDeserializer;
import utils.stream.FStream;

import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.DescriptorUtils;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.info.ComponentItem;
import mdt.model.sm.info.DefaultInformationModel;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.info.TwinComposition;
import mdt.model.sm.simulation.DefaultSimulation;
import mdt.model.sm.simulation.Simulation;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


/**
 * <code>HttpMDTInstanceClient</code>는 HTTP를 기반으로 하여 
 * {@link MDTInstance}를 원격에서 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClient implements MDTInstance, HttpClientProxy {
	private static final RequestBody EMPTY_BODY = RequestBody.create("", null);
	
	private final String m_endpoint;
	private final HttpMDTInstanceManagerClient m_manager;
	private final HttpRESTfulClient m_restfulClient;
	private AtomicReference<InstanceDescriptor> m_desc;
	private final AtomicReference<InformationModel> m_infoModel = new AtomicReference<>();
	private final AtomicReference<Data> m_data = new AtomicReference<>();
	private final AtomicReference<List<Simulation>> m_simulationList = new AtomicReference<>();
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	HttpMDTInstanceClient(HttpMDTInstanceManagerClient manager, InstanceDescriptor desc) {
		m_manager = manager;
		
		m_endpoint = String.format("%s/instances/%s", manager.getEndpoint(), desc.getId());
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(manager.getHttpClient())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(InstanceDescriptorSerDe.MAPPER))
										.build();
		
		m_desc = new AtomicReference<>(desc);
	}
	
	@Override
	public String getId() {
		return m_desc.get().getId();
	}

	@Override
	public MDTInstanceStatus getStatus() {
		return m_desc.get().getStatus();
	}

	@Override
	public String getBaseEndpoint() {
//		return m_desc.get().getBaseEndpoint();
		String endpoint = m_desc.get().getBaseEndpoint();
		if ( endpoint == null ) {
			reload();
			return m_desc.get().getBaseEndpoint();
		}
		else {
			return endpoint;
		}
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

	/**
	 * 대상 MDTInstance의 상태 정보를 재로드한다.
	 * 
	 * @return	재로드된 HTTP 기반 MDTInstance proxy 객체.
	 */
	public HttpMDTInstanceClient reload() {
		InstanceDescriptor desc = m_manager.getInstanceDescriptor(getId());
		m_desc.set(desc);
		
		return this;
	}
	
	void setInstanceDescriptor(InstanceDescriptor desc) {
		m_desc.set(desc);
	}

	@Override
	public void start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException {
		MDTInstanceStatus status = m_desc.get().getStatus(); 
		if ( status != MDTInstanceStatus.STOPPED && status != MDTInstanceStatus.FAILED ) {
			throw new InvalidResourceStatusException("MDTInstance", getId(), status);
		}
		
		InstanceDescriptor desc = sendStartRequest();
		m_desc.set(desc);
		
		switch ( desc.getStatus() ) {
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
				throw new InvalidResourceStatusException("MDTInstance", getId(), getStatus());
		}
	}

	@Override
	public void stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException, ExecutionException {
		Instant due = null;
		if ( timeout != null ) {
			due = Instant.now().plus(timeout);
		}
		
		MDTInstanceStatus status = m_desc.get().getStatus(); 
		if ( status != MDTInstanceStatus.RUNNING) {
			throw new InvalidResourceStatusException("MDTInstance", getId(), status);
		}
		
		InstanceDescriptor desc = sendStopRequest();
		m_desc.set(desc);
		
		switch ( desc.getStatus() ) {
			case STOPPED:
				return;
			case STOPPING:
				if ( pollInterval != null ) {
					waitWhileStatus(state -> state == MDTInstanceStatus.STOPPING, pollInterval, due);
				}
				break;
			default:
				throw new InvalidResourceStatusException("MDTInstance", getId(), getStatus());
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
	public List<SubmodelService> getAllSubmodelServices() throws InvalidResourceStatusException {
		return FStream.from(getAllInstanceSubmodelDescriptors())
						.map(desc -> toSubmodelService(desc.getId()))
						.toList();
	}

	@Override
	public SubmodelService getSubmodelServiceById(String submodelId) {
		InstanceSubmodelDescriptor desc = getInstanceSubmodelDescriptorById(submodelId);
		return toSubmodelService(desc.getId());
	}

	@Override
	public SubmodelService getSubmodelServiceByIdShort(String submodelIdShort) {
		InstanceSubmodelDescriptor desc = getInstanceSubmodelDescriptorByIdShort(submodelIdShort);
		return toSubmodelService(desc.getId());
	}

	@Override
	public List<SubmodelService> getAllSubmodelServiceBySemanticId(String semanticId) {
		return FStream.from(getAllInstanceSubmodelDescriptorBySemanticId(semanticId))
						.map(isd -> toSubmodelService(isd.getId()))
						.toList();
	}

	@Override
	public InformationModel getInformationModel() throws ResourceNotFoundException {
		return m_infoModel.updateAndGet(p -> FOption.getOrElse(p, this::loadInformationModel));
	}
	private InformationModel loadInformationModel() throws ResourceNotFoundException {
		List<SubmodelService> found = getAllSubmodelServiceBySemanticId(InformationModel.SEMANTIC_ID);
		SubmodelService svc = Funcs.getFirst(found).getOrThrow(() -> new ResourceNotFoundException("InformationModel",
																		"semanticId=" + InformationModel.SEMANTIC_ID));
		DefaultInformationModel infoModel = new DefaultInformationModel();
		infoModel.updateFromAasModel(svc.getSubmodel());
		return infoModel;
	}

	@Override
	public Data getData() throws ResourceNotFoundException {
		return m_data.updateAndGet(p -> FOption.getOrElseThrow(p, this::loadData));
	}
	private Data loadData() throws ResourceNotFoundException {
		List<SubmodelService> found = getAllSubmodelServiceBySemanticId(Data.SEMANTIC_ID);
		SubmodelService dataSvc = Funcs.getFirst(found)
										.getOrThrow(() -> new ResourceNotFoundException("DataSubmodel",
																				"semanticId=" + Data.SEMANTIC_ID));
		DefaultData data = new DefaultData();
		data.updateFromAasModel(dataSvc.getSubmodel());
		return data;
	}

	@Override
	public List<Simulation> getAllSimulations() {
		return m_simulationList.updateAndGet(lst -> FOption.getOrElseThrow(lst, this::loadAllSimulations));
	}
	private List<Simulation> loadAllSimulations() {
		return FStream.from(getAllSubmodelServiceBySemanticId(Simulation.SEMANTIC_ID))
						.map(smSvc -> {
							Submodel submodel = smSvc.getSubmodel();
							DefaultSimulation sim = new DefaultSimulation();
							sim.updateFromAasModel(submodel);
							return (Simulation)sim;
						})
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
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
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
		StateChangePoller.pollWhile(() -> waitCond.test(reload().getStatus()))
						.pollInterval(pollInterval)
						.due(due)
						.build()
						.run();
	}
	public void waitWhileStatus(Predicate<MDTInstanceStatus> waitCond, Duration pollInterval, Duration timeout)
			throws TimeoutException, InterruptedException, ExecutionException {
			StateChangePoller.pollWhile(() -> waitCond.test(reload().getStatus()))
							.pollInterval(pollInterval)
							.timeout(timeout)
							.build()
							.run();
		}
	
	public static void waitWhileStatus(List<HttpMDTInstanceClient> instances, Predicate<MDTInstance> pred,
										Duration pollInterval, Duration timeout)
		throws TimeoutException, InterruptedException, ExecutionException {
		StateChangePoller.pollWhile(() -> {
			for ( HttpMDTInstanceClient inst: instances ) {
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
	
	public List<HttpMDTInstanceClient> getAllComponents() {
		return getAllTargetInstances("contain");
	}
	
	public List<HttpMDTInstanceClient> getAllTargetInstances(String depType) {
		TwinComposition tcomp = getInformationModel().getTwinComposition();
		String myId = tcomp.getCompositionID();

		Map<String,ComponentItem> itemMap = FStream.from(tcomp.getComponentItems())
													.toMap(item -> item.getID());
		return FStream.from(tcomp.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals(depType) && dep.getSource().equals(myId))
						.flatMapNullable(dep -> itemMap.get(dep.getTarget()))
						.map(ComponentItem::getReference)
						.flatMapTry(aasId -> Try.get(() -> m_manager.getInstanceByAasId(aasId)))
						.toList();
	}
	
	public List<HttpMDTInstanceClient> getAllSourceInstances(String depType) {
		TwinComposition tcomp = getInformationModel().getTwinComposition();
		String myId = tcomp.getCompositionID();

		Map<String,ComponentItem> itemMap = FStream.from(tcomp.getComponentItems())
													.toMap(item -> item.getID());
		return FStream.from(tcomp.getCompositionDependencies())
						.filter(dep -> dep.getDependencyType().equals(depType) && dep.getTarget().equals(myId))
						.flatMapNullable(dep -> itemMap.get(dep.getTarget()))
						.map(ComponentItem::getReference)
						.flatMapTry(aasId -> Try.get(() -> m_manager.getInstanceByAasId(aasId)))
						.toList();
	}
	
	@Override
	public String toString() {
		String submodelIdStr = FStream.from(getAllInstanceSubmodelDescriptors())
										.map(InstanceSubmodelDescriptor::getIdShort)
										.join(", ");
		return String.format("[%s] AAS=%s SubmodelIdShorts=(%s) status=%s",
								getId(), getAasId(), submodelIdStr, getStatus());
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
		
		String smEp = DescriptorUtils.toSubmodelServiceEndpointString(baseEndpoint, id);
		return new HttpSubmodelServiceClient(getHttpClient(), smEp);
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
	
	private ResponseBodyDeserializer<InstanceDescriptor> m_descDeser = new ResponseBodyDeserializer<>() {
		@Override
		public InstanceDescriptor deserialize(Headers headers, String respBody) throws IOException {
			return m_serde.readInstanceDescriptor(respBody);
		}
	};
}
