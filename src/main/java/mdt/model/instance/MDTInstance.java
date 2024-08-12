package mdt.model.instance;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import com.google.common.base.Preconditions;

import utils.func.Funcs;

import mdt.model.registry.InvalidResourceStatusException;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstance {
	/**
	 * MDTInstance의 식별자를 반환한다.
	 * 
	 * @return	식별자.
	 */
	public default String getId() {
		return getInstanceDescriptor().getId();
	}
	
	/**
	 * MDTInstance의 상태를 반환한다.
	 * 
	 * @return	상태 정보
	 */
	public default MDTInstanceStatus getStatus() {
		return getInstanceDescriptor().getStatus();
	}
	
	/**
	 * MDTInstance에 부여된 endpoint를 반환한다.
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
	 */
	public default @Nullable String getEndpoint() {
		return getInstanceDescriptor().getEndpoint();
	}

	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 식별자를 반환한다.
	 * 
	 * @return	AAS 식별자.
	 */
	public default String getAasId() {
		return getInstanceDescriptor().getAasId();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 * 
	 * @return	idShort.
	 */
	public default @Nullable String getAasIdShort() {
		return getInstanceDescriptor().getAasIdShort();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId 를 반환한다.
	 * 
	 * @return	자산 식별자.
	 */
	public default @Nullable String getGlobalAssetId() {
		return getInstanceDescriptor().getGlobalAssetId();
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * 
	 * @return	자산 타입.
	 */
	public default @Nullable String getAssetType() {
		return getInstanceDescriptor().getAssetType();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 자산 종류를 반환한다.
	 * 
	 * @return	자산 종류.
	 */
	public default @Nullable AssetKind getAssetKind() {
		return getInstanceDescriptor().getAssetKind();
	}
	
	/**
	 * MDTInstance를 시작시킨다.
	 * 
	 * MDTInstance의 시작을 요청하고 실제로 성공적으로 동작할 때까지 대기한다.
	 * 시작 요청 후 동작할 때까지 내부적으로 주어진 {@code pollInterval} 간격으로
	 * 체크하는 작업을 반복한다. 만일 {@code pollInterval}이
	 * {@code null}인 경우는 대기 없이 시작 요청 후 바로 반환된다.
	 * 
	 * 제한시간 {@code timeout}이 {@code null}이 아닌 경우에는 이 제한시간 내로
	 * 시작되지 않는 경우에는 {@link TimeoutException}오류를 발생시킨다.
	 * 시작 대기 시간 중 쓰레드가 중단된 경우에는 {@link InterruptedException} 예외가 발생된다.
	 * 
	 * @param pollInterval	시작 완료 체크 시간 간격.
	 * @param timeout	최대 대기 시간. {@code null}인 경우에는 무한 대기를 의미함.
	 * @throws TimeoutException		대기 제한 시간을 경과하도록 시작이 완료되지 않은 경우.
	 * @throws InterruptedException	대기 중 쓰레드가 중단된 경우.
	 * @throws InvalidResourceStatusException	MDTInstance가 이미 동작 중인 경우.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 MDTInstance 시작이 실패한 경우.
	 */
	public void start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException, MDTInstanceManagerException;
	
	/**
	 * 동작 중인 MDTInstance를 종료시킨다.
	 * 
	 * MDTInstance의 종료를 요청하고 실제로 성공적으로 종료될 때까지 대기한다.
	 * 종료 요청 후 실제로 종료될 때까지 내부적으로 주어진 {@code pollInterval} 간격으로
	 * 체크하는 작업을 반복한다. 만일 {@code pollInterval}이
	 * {@code null}인 경우는 대기 없이 종료 요청 후 바로 반환된다.
	 * 
	 * 제한시간 {@code timeout}이 {@code null}이 아닌 경우에는 이 제한시간 내로
	 * 종료되지 않는 경우에는 {@link TimeoutException}오류를 발생시킨다.
	 * 종료 대기 시간 중 쓰레드가 중단된 경우에는 {@link InterruptedException} 예외가 발생된다.
	 * 
	 * @param pollInterval	종료 완료 체크 시간 간격.
	 * @param timeout	최대 대기 시간. {@code null}인 경우에는 무한 대기를 의미함.
	 * @throws TimeoutException		대기 제한 시간을 경과하도록 종료가 완료되지 않은 경우.
	 * @throws InterruptedException	대기 중 쓰레드가 중단된 경우.
	 * @throws InvalidResourceStatusException	MDTInstance가 이미 동작 중인 경우.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 MDTInstance 종료가 실패한 경우.
	 */
	public void stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException, MDTInstanceManagerException;
	
	public AssetAdministrationShellService getAssetAdministrationShellService()
		throws InvalidResourceStatusException, MDTInstanceManagerException;
	public List<SubmodelService> getAllSubmodelServices()
		throws InvalidResourceStatusException, MDTInstanceManagerException;
	public SubmodelService getSubmodelServiceById(String id);
	public SubmodelService getSubmodelServiceByIdShort(String idShort);
	
	public AssetAdministrationShellDescriptor getAASDescriptor();
	public List<SubmodelDescriptor> getAllSubmodelDescriptors();
	
	public default SubmodelDescriptor getSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodelId);
		
		return getAllSubmodelDescriptors().stream()
								.filter(desc -> desc.getId().equals(submodelId))
								.findAny()
								.orElseThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}
	public default SubmodelDescriptor getSubmodelDescriptorByIdShort(String submodelIdShort)
		throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodelIdShort);
		
		return getAllSubmodelDescriptors().stream()
								.filter(desc -> submodelIdShort.equals(desc.getIdShort()))
								.findAny()
								.orElseThrow(() -> new ResourceNotFoundException("Submodel",
																			"idShort=" + submodelIdShort));
	}
	
	public InstanceDescriptor getInstanceDescriptor();
	
	public default List<InstanceSubmodelDescriptor> getInstanceSubmodelDescriptors() {
		return getInstanceDescriptor().getInstanceSubmodelDescriptors();
	}
	public default InstanceSubmodelDescriptor getInstanceSubmodelDescriptorByIdShort(String idShort) {
		return Funcs.findFirst(getInstanceDescriptor().getInstanceSubmodelDescriptors(),
								isd -> isd.getIdShort().equals(idShort))
					.getOrThrow(() -> new ResourceNotFoundException("Submodel",
														String.format("instance[%s].%s", getId(), idShort)));
	}
	public default InstanceSubmodelDescriptor getInstanceSubmodelDescriptorById(String submodelId) {
		return Funcs.findFirst(getInstanceDescriptor().getInstanceSubmodelDescriptors(),
								isd -> isd.getId().equals(submodelId))
					.getOrThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}
	
	public default List<String> getSubmodelIdShorts() {
		return Funcs.map(getInstanceDescriptor().getInstanceSubmodelDescriptors(),
							InstanceSubmodelDescriptor::getIdShort);
	}
}
