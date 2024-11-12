package mdt.client.instance;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import utils.StateChangePoller;
import utils.func.FOption;
import utils.func.Funcs;
import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.DescriptorUtils;
import mdt.model.InvalidResourceStatusException;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.info.DefaultInformationModel;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.DefaultSimulation;
import mdt.model.sm.simulation.Simulation;

import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * <code>HttpMDTInstanceClient</code>는 HTTP를 기반으로 하여 
 * {@link MDTInstance}를 원격에서 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClientOld extends HttpAASRESTfulClient implements MDTInstance {
	private static final RequestBody EMPTY_BODY = RequestBody.create("", null);
	
	private final HttpMDTInstanceManagerClient m_manager;
	private AtomicReference<InstanceDescriptor> m_desc;
	private final AtomicReference<InformationModel> m_infoModel = new AtomicReference<>();
	private final AtomicReference<Data> m_data = new AtomicReference<>();
	private final AtomicReference<List<Simulation>> m_simulationList = new AtomicReference<>();
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	HttpMDTInstanceClientOld(HttpMDTInstanceManagerClient manager, InstanceDescriptor desc) {
		super(manager.getHttpClient(), String.format("%s/instances/%s", manager.getEndpoint(), desc.getId()));
		
		m_manager = manager;
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

	/**
	 * 대상 MDTInstance의 상태 정보를 재로드한다.
	 * 
	 * @return	재로드된 HTTP 기반 MDTInstance proxy 객체.
	 */
	public HttpMDTInstanceClientOld reload() {
		InstanceDescriptor desc = m_manager.getInstanceDescriptor(getId());
		m_desc.set(desc);
		
		return this;
	}

	@Override
	public void start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException, InvalidResourceStatusException {
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
				if ( pollInterval != null ) {
					try {
						waitWhileStatus(state -> state == MDTInstanceStatus.STARTING, pollInterval, timeout);
					}
					catch ( ExecutionException e ) {
						throw new MDTInstanceManagerException(e.getCause());
					}
				}
				break;
			default:
				throw new InvalidResourceStatusException("MDTInstance", getId(), getStatus());
		}
	}

	@Override
	public void stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException, InvalidResourceStatusException {
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
					try {
						waitWhileStatus(state -> state == MDTInstanceStatus.STOPPING, pollInterval, timeout);
					}
					catch ( ExecutionException e ) {
						throw new MDTInstanceManagerException(e.getCause());
					}
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
						.map(SubmodelService::getSubmodel)
						.map(submodel -> {
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
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetAdministrationShellDescriptor.class);
	}

    // @GetMapping({"instances/{id}/submodel_descriptors"})
	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
		String url = String.format("%s/submodel_descriptors", getEndpoint());
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

    // @GetMapping({"instances/{id}/stdout"})
	@Override
	public String getOutputLog() throws IOException {
		String url = String.format("%s/stdout", getEndpoint());
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, String.class);
	}
	
	public void waitWhileStatus(Predicate<MDTInstanceStatus> waitCond, Duration pollInterval, Duration timeout)
		throws TimeoutException, InterruptedException, ExecutionException {
		StateChangePoller.pollWhile(() -> waitCond.test(reload().getStatus()))
						.interval(pollInterval)
						.timeout(timeout)
						.build()
						.run();
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
		Request req = new Request.Builder().url(url).put(EMPTY_BODY).build();
		
		String descJson = call(req, String.class);
		return m_serde.readInstanceDescriptor(descJson);
	}

    // @PutMapping({"instance-manager/instances/{id}/stop"})
	private InstanceDescriptor sendStopRequest() {
		String url = String.format("%s/stop", getEndpoint());
		Request req = new Request.Builder().url(url).put(EMPTY_BODY).build();
		
		String descJson = call(req, String.class);
		return  m_serde.readInstanceDescriptor(descJson);
	}
	
	private SubmodelService toSubmodelService(String id) {
		String baseEndpoint = getBaseEndpoint();
		if ( baseEndpoint == null ) {
			throw new InvalidResourceStatusException("MDTInstance", "id=" + getId(), getStatus());
		}
		
		String smEp = DescriptorUtils.toSubmodelServiceEndpointString(baseEndpoint, id);
		return new HttpSubmodelServiceClient(getHttpClient(), smEp);
	}
}
