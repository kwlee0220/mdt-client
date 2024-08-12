package mdt.client.instance;

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
import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.DescriptorUtils;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.registry.InvalidResourceStatusException;
import mdt.model.service.SubmodelService;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClient extends HttpAASRESTfulClient implements MDTInstance {
	private static final RequestBody EMPTY_BODY = RequestBody.create("", null);
	
	private final HttpMDTInstanceManagerClient m_manager;
	private final String m_endpoint;
	private AtomicReference<InstanceDescriptor> m_desc;
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	public HttpMDTInstanceClient(HttpMDTInstanceManagerClient manager, InstanceDescriptor desc) {
		super(manager.getHttpClient());
		
		m_manager = manager;
		m_endpoint = String.format("%s/instances/%s", manager.getEndpoint(), desc.getId());
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
	public String getEndpoint() {
		return m_desc.get().getEndpoint();
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

	public HttpMDTInstanceClient reload() {
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
		String endpoint = DescriptorUtils.toAASServiceEndpointString(getEndpoint(), getAasId());
		if ( endpoint == null ) {
			throw new InvalidResourceStatusException("AssetAdministrationShell",
													String.format("mdt=%s, id=%s", getId(), getAasId()),
													getStatus());
		}
		return new HttpAASServiceClient(getHttpClient(), endpoint);
	}

	@Override
	public List<SubmodelService> getAllSubmodelServices() throws InvalidResourceStatusException {
		return FStream.from(getInstanceSubmodelDescriptors())
						.map(desc -> toSubmodelService(desc.getId()))
						.toList();
	}
	public SubmodelService getSubmodelServiceById(String id) {
		InstanceSubmodelDescriptor desc = getInstanceSubmodelDescriptorById(id);
		return toSubmodelService(desc.getId());
	}
	public SubmodelService getSubmodelServiceByIdShort(String idShort) {
		InstanceSubmodelDescriptor desc = getInstanceSubmodelDescriptorByIdShort(idShort);
		return toSubmodelService(desc.getId());
	}

	
	public List<InstanceSubmodelDescriptor> getInstanceSubmodelDescriptors() {
		return FStream.from(m_desc.get().getInstanceSubmodelDescriptors())
						.cast(InstanceSubmodelDescriptor.class)
						.toList();
	}
	

    // @GetMapping({"instances/{id}/aas_descriptor"})
	@Override
	public AssetAdministrationShellDescriptor getAASDescriptor() {
		String url = String.format("%s/aas_descriptor", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetAdministrationShellDescriptor.class);
	}

    // @GetMapping({"instances/{id}/submodel_descriptors"})
	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
		String url = String.format("%s/submodel_descriptors", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
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
		String submodelIdStr = FStream.from(getSubmodelIdShorts()).join(", ");
		return String.format("[%s] AAS=%s SubmodelIdShorts=(%s) status=%s",
								getId(), getAasId(), submodelIdStr, getStatus());
	}
	
    // @PutMapping({"instance-manager/instances/{id}/start"})
	private InstanceDescriptor sendStartRequest() {
		String url = String.format("%s/start", m_endpoint);
		Request req = new Request.Builder().url(url).put(EMPTY_BODY).build();
		
		String descJson = call(req, String.class);
		return m_serde.readInstanceDescriptor(descJson);
	}

    // @PutMapping({"instance-manager/instances/{id}/stop"})
	private InstanceDescriptor sendStopRequest() {
		String url = String.format("%s/stop", m_endpoint);
		Request req = new Request.Builder().url(url).put(EMPTY_BODY).build();
		
		String descJson = call(req, String.class);
		return  m_serde.readInstanceDescriptor(descJson);
	}
	
	private SubmodelService toSubmodelService(String id) {
		String baseEndpoint = getEndpoint();
		if ( baseEndpoint == null ) {
			throw new InvalidResourceStatusException("MDTInstance", "id=" + getId(), getStatus());
		}
		
		String smEp = DescriptorUtils.toSubmodelServiceEndpointString(baseEndpoint, id);
		return new HttpSubmodelServiceClient(getHttpClient(), smEp);
	}
}
