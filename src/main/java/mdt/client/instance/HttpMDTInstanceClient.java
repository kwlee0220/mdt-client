package mdt.client.instance;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.InternalException;
import utils.Preconditions;
import utils.Throwables;
import utils.async.PeriodicPoller;
import utils.func.FOption;
import utils.func.Funcs;
import utils.func.TimedLazy;
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
import mdt.model.instance.MDTParameterService;
import mdt.model.instance.MDTParameterServiceCollection;
import mdt.model.instance.MDTSubmodelDescriptor;
import mdt.model.instance.MDTTwinCompositionDescriptor;
import mdt.model.instance.MDTTwinCompositionDescriptor.MDTCompositionDependency;
import mdt.model.instance.MDTTwinCompositionDescriptor.MDTCompositionItem;
import mdt.model.instance.StatusChangedListener;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ai.AISubmodelService;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DefaultDataInfo;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.simulation.SimulationSubmodelService;


/**
 * {@code HttpMDTInstanceClient}는 HTTP REST API를 통해 원격 MDT 매니저에 등록된
 * {@link MDTInstance}를 조작하기 위한 클라이언트 구현이다.
 * <p>
 * 본 클래스는 {@link HttpMDTInstanceManager}를 통해서만 생성되며, 직접 생성할 수 없다.
 * 대상 MDTInstance에 대한 모든 조회/제어 요청은 매니저의 {@code /instances/{id}} 엔드포인트로 전달된다.
 * <p>
 * 상태 조회 비용을 줄이기 위해 {@link InstanceDescriptor}를 내부적으로 {@link TimedLazy}로 캐싱한다.
 * 캐시는 최근 갱신 시점으로부터 3초 동안 유효하며, 그 이후 첫 조회 시
 * 원격 매니저로부터 다시 로드된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClient implements MDTInstance, HttpClientProxy {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpMDTInstanceClient.class);
	
	private static final RequestBody EMPTY_BODY = RequestBody.create(new byte[0], null);
	private static final Duration DESCRIPTOR_TTL = Duration.ofSeconds(3);

	private final String m_id;
	private final String m_registryEndpoint;
	private final HttpMDTInstanceManager m_manager;
	private final HttpRESTfulClient m_restfulClient;
	private final TimedLazy<InstanceDescriptor> m_lazyDesc;

	/**
	 * 주어진 매니저와 초기 {@link InstanceDescriptor}로 클라이언트를 생성한다.
	 * <p>
	 * 패키지 private 생성자로, 외부에서 직접 호출하지 않고 {@link HttpMDTInstanceManager}에 의해서만 생성된다.
	 * 전달된 {@code desc}는 TTL 캐시의 초기 값으로 적재되며, 이후 TTL 만료 시 매니저를 통해 다시 로드된다.
	 *
	 * @param manager	본 클라이언트가 속한 {@link HttpMDTInstanceManager}.
	 * @param desc		초기 {@link InstanceDescriptor}.
	 */
	HttpMDTInstanceClient(HttpMDTInstanceManager manager, InstanceDescriptor desc) {
		m_manager = manager;

		m_id = desc.getId();
		m_registryEndpoint = String.format("%s/instances/%s", manager.getEndpoint(), desc.getId());
		m_restfulClient = HttpRESTfulClient.builder()
										.httpClient(manager.getHttpClient())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER))
										.build();
		m_lazyDesc = TimedLazy.of(DESCRIPTOR_TTL, () -> m_manager.getInstanceDescriptor(m_id));
		m_lazyDesc.set(desc);
	}

	@Override
	public HttpMDTInstanceManager getInstanceManager() {
		return m_manager;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 캐시된 {@link InstanceDescriptor}의 마지막 갱신으로부터 3초가 경과한 경우에는
	 * 원격 매니저로부터 다시 로드하여 캐시를 갱신한 뒤 반환하고, 그렇지 않으면 캐시 값을 그대로 반환한다.
	 */
	@Override
	public InstanceDescriptor getInstanceDescriptor() {
		return m_lazyDesc.get();
	}
	
	@Override
	public String getId() {
		return m_id;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 다른 메소드와 달리 본 메소드는 캐시된 {@link InstanceDescriptor}의 상태를 반환하는 것이 아니라,
	 * 항상 원격 매니저로부터 다시 로드하여 최신 상태를 반환한다.
	 */
	@Override
	public MDTInstanceStatus getStatus() {
		return m_lazyDesc.reload().getStatus();
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * 원격 매니저에 시작을 요청한 뒤, {@code pollInterval}이 지정된 경우
	 * {@link MDTInstanceStatus#STARTING} 상태에서 벗어날 때까지 polling 한다.
	 * 상태 조회 도중 발생하는 예기치 못한 {@link ExecutionException}은
	 * {@link InternalException}으로 변환되어 던져진다.
	 */
	@Override
	public MDTInstanceStatus start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException {
		Instant due = null;
		if ( timeout != null ) {
			due = Instant.now().plus(timeout);
		}

		MDTInstanceStatus status = sendStartRequest().getStatus();
		if ( status != MDTInstanceStatus.STARTING || pollInterval == null ) {
			return status;
		}

		// 'STARTING' 상태가 바뀔 때까지 대기한다.
		try {
			status = waitWhileStatus(state -> state == MDTInstanceStatus.STARTING, pollInterval, due);
			return status;
		}
		catch ( ExecutionException e ) {
			// 상태 체크만 하던 과정에서 오류가 발생할 경우가 거의 없으므로
			// 이 경우는 InternalException을 발생시킨다.
			throw new InternalException(Throwables.unwrapThrowable(e));
		}
	}

	public MDTInstanceStatus start(@Nullable Duration pollInterval, @Nullable Duration timeout,
									@NotNull StatusChangedListener listener)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 원격 매니저에 종료를 요청한 뒤, 응답된 상태가
	 * {@link MDTInstanceStatus#STOPPING}이면서 {@code pollInterval}이 지정된 경우
	 * 해당 상태에서 벗어날 때까지 polling 한다. 응답된 상태가 종료 진행/완료 상태
	 * ({@link MDTInstanceStatus#STOPPED} 또는 {@link MDTInstanceStatus#STOPPING})가 아닌 경우에는
	 * {@link InvalidResourceStatusException}을 던진다.
	 */
	@Override
	public MDTInstanceStatus stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException {
		Instant due = null;
		if ( timeout != null ) {
			due = Instant.now().plus(timeout);
		}

		MDTInstanceStatus status = sendStopRequest().getStatus();
		switch ( status ) {
			case STOPPED:
				return status;
			case STOPPING:
				if ( pollInterval != null ) {
					try {
						return waitWhileStatus(state -> state == MDTInstanceStatus.STOPPING, pollInterval, due);
					}
					catch ( ExecutionException e ) {
						throw new InternalException(Throwables.unwrapThrowable(e));
					}
				}
				else {
					return status;
				}
			default:
				throw new InvalidResourceStatusException("MDTInstance", getId(), status);
		}
	}

	@Override
	public HttpAASServiceClient getAssetAdministrationShellService() {
		InstanceDescriptor desc = getInstanceDescriptor();
		String endpoint = DescriptorUtils.toAASServiceEndpointString(desc.getBaseEndpoint(), desc.getAasId());
		if ( endpoint == null ) {
			throw new InvalidResourceStatusException("AssetAdministrationShell",
													String.format("mdt=%s, id=%s", m_id, desc.getAasId()),
													desc.getStatus());
		}
		return new HttpAASServiceClient(getHttpClient(), endpoint);
	}
	
	@Override
	public List<MDTSubmodelDescriptor> getMDTSubmodelDescriptorAll() {
		String url = String.format("%s/model/submodels", getEndpoint());
		return m_restfulClient.get(url, MDTModelSerDes.MDT_SUBMODEL_LIST);
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
	public List<SubmodelService> getSubmodelServiceAll() {
		return FStream.from(getMDTSubmodelDescriptorAll())
						.map(this::toSubmodelService)
						.toList();
	}

	@Override
	public FOption<SubmodelService> getSubmodelServiceById(String submodelId) {
		var desc = Funcs.findFirst(getMDTSubmodelDescriptorAll(), isd -> isd.getId().equals(submodelId));
		return FOption.ofNullable(desc).map(this::toSubmodelService);
	}

	@Override
	public FOption<SubmodelService> getSubmodelServiceByIdShort(String submodelIdShort) {
		var desc = Funcs.findFirst(getMDTSubmodelDescriptorAll(), isd -> isd.getIdShort().equals(submodelIdShort));
		return FOption.ofNullable(desc).map(this::toSubmodelService);
	}

	@Override
	public List<SubmodelService> getSubmodelServiceAllBySemanticId(String semanticId) {
		return FStream.from(getMDTSubmodelDescriptorAll())
						.filter(isd -> isd.getSemanticId().equals(semanticId))
						.map(this::toSubmodelService)
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
	public List<MDTParameterService> getParameterServiceAll() {
		String url = String.format("%s/model/parameters", getEndpoint());
		List<MDTParameterDescriptor> paramDescList = m_restfulClient.get(url, MDTModelSerDes.MDT_PARAM_LIST);
		return new MDTParameterServiceCollection(this, paramDescList);
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
	
	/**
	 * 원격 매니저에서 최신 상태를 다시 로드하여, 주어진 술어({@code pred})를 만족하면 그 상태를,
	 * 그렇지 않으면 {@code null}을 반환한다.
	 * <p>
	 * 캐시 TTL과 무관하게 항상 매니저에서 다시 로드한 뒤 캐시도 같이 갱신한다.
	 * {@link PeriodicPoller}는 {@code null}이 아닌 값이 반환되는 시점에 polling을 종료하므로,
	 * 본 메서드는 polling 종료 조건을 표현하는 어댑터로 사용된다.
	 */
	private MDTInstanceStatus pollTargetState(Predicate<MDTInstanceStatus> pred) {
		MDTInstanceStatus state = m_lazyDesc.reload().getStatus();
		return pred.test(state) ? state : null;
	}
	
	/**
	 * MDTInstance 상태가 {@code waitCond}을 만족하는 동안 polling 대기한다.
	 * <p>
	 * Polling은 매 {@code pollInterval}마다 서버로부터 최신 상태를 조회하며,
	 * {@code waitCond}을 더 이상 만족하지 않게 되는 시점에 종료한다.
	 * 제한 시각 {@code due}까지 종료 조건이 충족되지 않으면 {@link TimeoutException}이 발생한다.
	 *
	 * @param waitCond     대기 조건. {@code true}인 동안 polling이 계속된다.
	 * @param pollInterval Polling 간격.
	 * @param due          제한 시각. {@code null}이면 무한 대기.
	 * @return 대기 종료 시점의 상태.
	 * @throws TimeoutException     {@code due} 시각까지 대기 조건이 해제되지 않은 경우.
	 * @throws InterruptedException 대기 중 쓰레드가 인터럽트된 경우.
	 * @throws ExecutionException   상태 조회 중 오류가 발생한 경우.
	 */
	public MDTInstanceStatus waitWhileStatus(Predicate<MDTInstanceStatus> waitCond, Duration pollInterval,
												Instant due)
		throws TimeoutException, InterruptedException, ExecutionException {
		return PeriodicPoller.poll(() -> pollTargetState(waitCond.negate()))
							.interval(pollInterval)
							.due(due)
							.build()
							.run();
	}

	/**
	 * MDTInstance 상태가 {@code waitCond}을 만족하는 동안 polling 대기한다.
	 * <p>
	 * 위 오버로드와 동일하지만 제한 시각 대신 제한 기간을 받는다.
	 *
	 * @param waitCond     대기 조건.
	 * @param pollInterval Polling 간격.
	 * @param timeout      제한 기간. {@code null}이면 무한 대기.
	 * @return 대기 종료 시점의 상태.
	 * @throws TimeoutException     {@code timeout} 경과 시까지 대기 조건이 해제되지 않은 경우.
	 * @throws InterruptedException 대기 중 쓰레드가 인터럽트된 경우.
	 * @throws ExecutionException   상태 조회 중 오류가 발생한 경우.
	 */
	public MDTInstanceStatus waitWhileStatus(Predicate<MDTInstanceStatus> waitCond, Duration pollInterval,
											Duration timeout)
		throws TimeoutException, InterruptedException, ExecutionException {
		return PeriodicPoller.poll(() -> pollTargetState(waitCond.negate()))
							.interval(pollInterval)
							.timeout(timeout)
							.build()
							.run();
	}

	/**
	 * 주어진 인스턴스들 중 {@code pred}를 만족하는 인스턴스가 모두 사라질 때까지 polling 대기한다.
	 * <p>
	 * 매 {@code pollInterval}마다 모든 인스턴스에 {@code pred}를 평가하여 만족하지 않는 인스턴스를 목록에서
	 * 제거하며, 모든 인스턴스가 제거되면 종료한다.
	 *
	 * @param instances    대기 대상 인스턴스 목록.
	 * @param pred         대기 조건. {@code true}인 인스턴스는 계속 polling 된다.
	 * @param pollInterval Polling 간격.
	 * @param timeout      제한 기간. {@code null}이면 무한 대기.
	 * @throws TimeoutException     {@code timeout} 경과 시까지 대기 조건이 해제되지 않은 경우.
	 * @throws InterruptedException 대기 중 쓰레드가 인터럽트된 경우.
	 * @throws ExecutionException   상태 조회 중 오류가 발생한 경우.
	 */
	public static void waitWhileStatus(List<HttpMDTInstanceClient> instances, Predicate<MDTInstance> pred,
										Duration pollInterval, Duration timeout)
		throws TimeoutException, InterruptedException, ExecutionException {
		List<HttpMDTInstanceClient> copiedInstances = Lists.newArrayList(instances);
		PeriodicPoller.poll(() -> {
							// predicate를 만족하지 않는 인스턴스는 목록에서 제거한다.
							Funcs.removeIf(copiedInstances, inst -> !pred.test(inst));
							// predicate를 만족하는 인스턴스가 하나도 없으면 Polling을 종료한다.
							// 여기서 'Boolean.TRUE'를 반환하는 것은 임의의 non-null 값을 반환하기 위한 것이며,
							// 반환된 값 자체는 사용되지 않는다.
							return !copiedInstances.isEmpty() ? null : Boolean.TRUE;
						})
						.interval(pollInterval)
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
		List<MDTInstance> sources = Lists.newArrayList();
		for ( MDTCompositionDependency dep: twinComp.getCompositionDependencies() ) {
			if ( !dep.getType().equals(depType) || !dep.getTargetItem().equals(myId) ) {
				continue;
			}

			MDTCompositionItem srcItem = itemMap.get(dep.getSourceItem());
			if ( srcItem == null ) {
				continue;
			}

			try {
				MDTInstance srcInst = m_manager.getInstanceByAasId(srcItem.getReference());
				sources.add(srcInst);
			}
			catch ( Exception e ) {
				s_logger.warn("failed to get source MDTInstance: aasId={}", srcItem.getReference(), e);
			}
		}

		return sources;
	}
	
	@Override
	public String toString() {
		InstanceDescriptor desc = m_lazyDesc.peekUnchecked();
		return ( desc != null )
				? String.format("[%s] AAS=%s status=%s", m_id, desc.getAasId(), desc.getStatus())
				: String.format("[%s] (unloaded)", m_id);
	}

	/**
	 * 원격 매니저의 {@code /start} 엔드포인트에 PUT 요청을 보내고 응답으로 받은
	 * {@link InstanceDescriptor}를 반환한다.
	 */
	private InstanceDescriptor sendStartRequest() {
		String url = String.format("%s/start", getEndpoint());
		return m_restfulClient.put(url, EMPTY_BODY, MDTModelSerDes.INSTANCE_DESC_RESP);
	}

	/**
	 * 원격 매니저의 {@code /stop} 엔드포인트에 PUT 요청을 보내고 응답으로 받은
	 * {@link InstanceDescriptor}를 반환한다.
	 */
	private InstanceDescriptor sendStopRequest() {
		String url = String.format("%s/stop", getEndpoint());
		return m_restfulClient.put(url, EMPTY_BODY, MDTModelSerDes.INSTANCE_DESC_RESP);
	}

	/**
	 * {@link MDTSubmodelDescriptor}로부터 적절한 {@link SubmodelService} 구현을 생성한다.
	 * <p>
	 * 기본적으로 {@link HttpSubmodelServiceClient}를 사용하지만,
	 * {@code semanticId}가 {@link Simulation#SEMANTIC_ID} 또는 {@link AI#SEMANTIC_ID}인 경우에는
	 * 각각 {@link SimulationSubmodelService}, {@link AISubmodelService}로 래핑하여 반환한다.
	 *
	 * @throws InvalidResourceStatusException	대상 MDTInstance가 동작 중이 아니라서 endpoint를 사용할 수 없는 경우.
	 */
	private SubmodelService toSubmodelService(MDTSubmodelDescriptor smDesc) {
		InstanceDescriptor desc = getInstanceDescriptor();
		String baseEndpoint = desc.getBaseEndpoint();
		if ( baseEndpoint == null ) {
			throw new InvalidResourceStatusException("MDTInstance", "id=" + m_id, desc.getStatus());
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

	/**
	 * 주어진 Submodel ID에 해당하는 Submodel 서비스의 endpoint URL을 반환한다.
	 *
	 * @param submodelId Submodel의 ID.
	 * @return Submodel 서비스의 endpoint URL.
	 *         이 인스턴스의 서비스 endpoint가 활성화되어 있지 않으면 {@code null}을 반환한다.
	 */
	@Nullable
	public String getSubmodelServiceEndpoint(String submodelId) {
		Preconditions.checkNotNullArgument(submodelId, "submodelId is null");

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
