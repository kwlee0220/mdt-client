package mdt.client.instance;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.InternalException;
import utils.StateChangePoller;
import utils.Throwables;
import utils.async.Guard;
import utils.func.Funcs;
import utils.func.Try;
import utils.http.HttpClientProxy;
import utils.http.HttpRESTfulClient;
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
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.MDTOperationDescriptor;
import mdt.model.instance.MDTParameterDescriptor;
import mdt.model.instance.MDTSubmodelDescriptor;
import mdt.model.instance.MDTTwinCompositionDescriptor;
import mdt.model.instance.MDTTwinCompositionDescriptor.MDTCompositionDependency;
import mdt.model.instance.MDTTwinCompositionDescriptor.MDTCompositionItem;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ai.AISubmodelService;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultDataInfo;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.simulation.SimulationSubmodelService;


/**
 * <code>HttpMDTInstanceClient</code>는 HTTP를 기반으로 하여 
 * {@link MDTInstance}를 원격에서 활용하기 위한 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClient implements MDTInstance, HttpClientProxy {
	private static final Logger s_logger = org.slf4j.LoggerFactory.getLogger(HttpMDTInstanceClient.class);
	
	private static final RequestBody EMPTY_BODY = RequestBody.create("", null);
	private static final Duration VALID_PERIOD = Duration.ofSeconds(3);
	
	private final String m_id;
	private final String m_registryEndpoint;
	private final HttpMDTInstanceManager m_manager;
	private final HttpRESTfulClient m_restfulClient;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private InstanceDescriptor m_desc;
	@GuardedBy("m_guard") private Instant m_updated;
	
	HttpMDTInstanceClient(HttpMDTInstanceManager manager, InstanceDescriptor desc) {
		m_manager = manager;
		
		m_id = desc.getId();
		m_registryEndpoint = String.format("%s/instances/%s", manager.getEndpoint(), desc.getId());
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(manager.getHttpClient())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER))
										.build();
		
		m_guard.run(() -> {
			m_desc = desc;
			m_updated = Instant.now();
		});
	}

	@Override
	public HttpMDTInstanceManager getInstanceManager() {
		return m_manager;
	}

	@Override
	public InstanceDescriptor getInstanceDescriptor() {
		Instant now = Instant.now();
		return m_guard.get(() -> {
			Duration age = Duration.between(m_updated, now);
			if ( age.compareTo(VALID_PERIOD) > 0 ) {
				reloadInstanceDescriptor();
			}
			return m_desc;
		});
	}
	
	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public MDTInstanceStatus getStatus() {
		return getInstanceDescriptor().getStatus();
	}

	@Override
	public String getServiceEndpoint() {
		return getInstanceDescriptor().getBaseEndpoint();
	}
	
	@Override
	public String getAasId() {
		return getInstanceDescriptor().getAasId();
	}

	@Override
	public String getAasIdShort() {
		return getInstanceDescriptor().getAasIdShort();
	}

	@Override
	public String getGlobalAssetId() {
		return getInstanceDescriptor().getGlobalAssetId();
	}

	@Override
	public MDTAssetType getAssetType() {
		return getInstanceDescriptor().getAssetType();
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
		String endpoint = DescriptorUtils.toAASServiceEndpointString(getServiceEndpoint(), getAasId());
		if ( endpoint == null ) {
			throw new InvalidResourceStatusException("AssetAdministrationShell",
													String.format("mdt=%s, id=%s", getId(), getAasId()),
													getStatus());
		}
		return new HttpAASServiceClient(getHttpClient(), endpoint);
	}
	
	@Override
	public List<MDTSubmodelDescriptor> getMDTSubmodelDescriptorAll() {
		String url = String.format("%s/model/submodels", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.MDT_SUBMODEL_LIST);
	}
	
	@Override
	public List<MDTParameterDescriptor> getMDTParameterDescriptorAll() {
		String url = String.format("%s/model/parameters", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.MDT_PARAM_LIST);
	}
	
	@Override
	public List<MDTOperationDescriptor> getMDTOperationDescriptorAll() {
		String url = String.format("%s/model/operations", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.MDT_OP_LIST);
	}
	
	@Override
	public MDTTwinCompositionDescriptor getMDTTwinCompositionDescriptor() {
		String url = String.format("%s/model/compositions", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.MDT_TWIN_COMP);
	}

	@Override
	public List<SubmodelService> getSubmodelServiceAll() throws InvalidResourceStatusException {
		return FStream.from(getMDTSubmodelDescriptorAll())
						.map(desc -> toSubmodelService(desc))
						.toList();
	}

	@Override
	public SubmodelService getSubmodelServiceById(String submodelId) {
		return Funcs.findFirst(getMDTSubmodelDescriptorAll(), isd -> isd.getId().equals(submodelId))
						.map(desc -> toSubmodelService(desc))
						.orElseThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}

	@Override
	public SubmodelService getSubmodelServiceByIdShort(String submodelIdShort) {
		return Funcs.findFirst(getMDTSubmodelDescriptorAll(), isd -> isd.getIdShort().equals(submodelIdShort))
						.map(desc -> toSubmodelService(desc))
						.orElseThrow(() -> new ResourceNotFoundException("Submodel", "idShort=" + submodelIdShort));
	}

	@Override
	public List<SubmodelService> getSubmodelServiceAllBySemanticId(String semanticId) {
		return FStream.from(getMDTSubmodelDescriptorAll())
						.filter(isd -> isd.getSemanticId().equals(semanticId))
						.map(desc -> toSubmodelService(desc))
						.toList();
	}
	
	@Override
	public ParameterCollection getParameterCollection() {
		SubmodelService svc = FStream.from(getSubmodelServiceAllBySemanticId(Data.SEMANTIC_ID))
									.findFirst()
									.getOrThrow(() -> new ResourceNotFoundException("Submodel",
																				"semanticId=" + Data.SEMANTIC_ID));
		DefaultDataInfo dataInfo = new DefaultDataInfo();
		dataInfo.updateFromAasModel(svc.getSubmodelElementByPath("DataInfo"));
		if ( dataInfo.isEquipment() ) {
			return dataInfo.getEquipment();
		}
		else if ( dataInfo.isOperation() ) {
			return dataInfo.getOperation();
		}
		else {
			throw new ResourceNotFoundException("ParameterCollection", "id=" + getId());
		}
	}

	@Override
	public AssetAdministrationShellDescriptor getAASShellDescriptor() {
		String url = String.format("%s/aas/shell_descriptor", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.AAS_SHELL_RESP);
	}

	@Override
	public List<SubmodelDescriptor> getAASSubmodelDescriptorAll() {
		String url = String.format("%s/aas/submodel_descriptors", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.AAS_SM_LIST_RESP);
	}

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
		return m_registryEndpoint;
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
	
	@Override
	public List<MDTInstance> getTargetInstanceAllOfDependency(String depType) {
		MDTTwinCompositionDescriptor twinComp = getMDTTwinCompositionDescriptor();
		Map<String, MDTCompositionItem> itemMap = FStream.from(twinComp.getCompositionItems())
														.tagKey(MDTCompositionItem::getId)
														.toMap();

		String myId = twinComp.getId();
		List<MDTInstance> dependents = Lists.newArrayList();
		for ( MDTCompositionDependency dep: twinComp.getCompositionDependencies() ) {
			if ( !dep.getType().equals(depType) || !dep.getSourceItem().equals(myId) ) {
				continue;
			}
			
			MDTCompositionItem depItem = itemMap.get(dep.getTargetItem());
			if ( depItem == null ) {
				continue;
			}
			
			try {
				MDTInstance depInst = m_manager.getInstanceByAasId(depItem.getReference());
				dependents.add(depInst);
			}
			catch ( Exception e ) {
				s_logger.warn("failed to get dependent MDTInstance: aasId={}", depItem.getReference(), e);
			}
		}
		
		return dependents;
	}

	@Override
	public List<MDTInstance> getSourceInstanceAllOfDependency(String depType) {
		MDTTwinCompositionDescriptor twinComp = getMDTTwinCompositionDescriptor();
		Map<String, MDTCompositionItem> itemMap = FStream.from(twinComp.getCompositionItems())
																	.tagKey(MDTCompositionItem::getId)
																	.toMap();

		String myId = twinComp.getId();
		return FStream.from(twinComp.getCompositionDependencies())
						.filter(dep -> dep.getType().equals(depType) && dep.getTargetItem().equals(myId))
						.flatMapNullable(dep -> itemMap.get(dep.getSourceItem()))
						.map(MDTCompositionItem::getReference)
						.flatMapTry(aasId -> Try.get(() -> m_manager.getInstanceByAasId(aasId)))
						.cast(MDTInstance.class)
						.toList();
	}
	
	@Override
	public String toString() {
		String submodelIdStr = FStream.from(getMDTSubmodelDescriptorAll())
										.map(MDTSubmodelDescriptor::getIdShort)
										.join(", ");
		return String.format("[%s] AAS=%s SubmodelIdShorts=(%s) status=%s",
								getId(), getAasId(), submodelIdStr, getStatus());
	}
	
	private InstanceDescriptor reloadInstanceDescriptor() {
		InstanceDescriptor desc = m_manager.getInstanceDescriptor(m_id);
		m_guard.run(() -> {
			m_desc = desc;
			m_updated = Instant.now();
		});
		
		return desc;
	}
	
    // @PutMapping({"instance-manager/instances/{id}/start"})
	private InstanceDescriptor sendStartRequest() {
		String url = String.format("%s/start", getEndpoint());
		return m_restfulClient.put(url, EMPTY_BODY, MDTModelSerDes.INSTANCE_DESC_RESP);
	}

    // @PutMapping({"instance-manager/instances/{id}/stop"})
	private InstanceDescriptor sendStopRequest() {
		String url = String.format("%s/stop", getEndpoint());
		return m_restfulClient.put(url, EMPTY_BODY, MDTModelSerDes.INSTANCE_DESC_RESP);
	}
	
	private SubmodelService toSubmodelService(MDTSubmodelDescriptor smDesc) {
		String baseEndpoint = getServiceEndpoint();
		if ( baseEndpoint == null ) {
			throw new InvalidResourceStatusException("MDTInstance", "id=" + getId(), getStatus());
		}
		
		String encodedSubmodelId = AASUtils.encodeBase64UrlSafe(smDesc.getId());
		String smSvcEndpoint = String.format("%s/submodels/%s", baseEndpoint, encodedSubmodelId);
		SubmodelService core = new HttpSubmodelServiceClient(getHttpClient(), smSvcEndpoint);
		return switch ( smDesc.getSemanticId() ) {
			case Simulation.SEMANTIC_ID -> new SimulationSubmodelService(core);
			case AI.SEMANTIC_ID -> new AISubmodelService(core);
			default -> core;
		};
	}

	public String getSubmodelServiceEndpoint(String submodelId) {
		String instanceServiceEndpoint = getServiceEndpoint();
		if ( instanceServiceEndpoint != null ) {
			String encodedSubmodelId = AASUtils.encodeBase64UrlSafe(submodelId);
			return String.format("%s/submodels/%s", instanceServiceEndpoint, encodedSubmodelId);
		}
		else {
			return null;
		}
	}
}
