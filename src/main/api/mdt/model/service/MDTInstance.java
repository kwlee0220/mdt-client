package mdt.model.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import com.google.common.base.Preconditions;

import utils.func.Funcs;

import mdt.aas.AssetAdministrationShellRegistry;
import mdt.model.InvalidResourceStatusException;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstanceInfo;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.sm.data.Data;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.Simulation;


/**
 * {@code MDTInstance}는 MDT 프레임워크에 의해 관리/운영되는 MDT 트윈을
 * 다루기 위한 인터페이스를 정의한다.
 * <p>
 * {@code MDTInstance}에 의해 관리되는 속성은 {@link InstanceDescriptor}에 의해 정의되고,
 * {@link #getInstanceDescriptor()}를 통해 접근할 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstance {
	/**
	 * MDTInstance의 식별자를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getId()}와 동일하다.
	 * 
	 * @return	식별자.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default String getId() {
		return getInstanceDescriptor().getId();
	}
	
	/**
	 * MDTInstance의 상태를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getStatus()}와 동일하다.
	 * 
	 * @return	상태 정보
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default MDTInstanceStatus getStatus() {
		return getInstanceDescriptor().getStatus();
	}
	
	/**
	 * MDTInstance에 부여된 endpoint를 반환한다.
	 * <p>
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getEndpoint()}와 동일하다.
	 * 
	 * @return	Endpoint 정보.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default @Nullable String getBaseEndpoint() {
		return getInstanceDescriptor().getBaseEndpoint();
	}

	/**
	 * MDTInstance가 포함한 AssetAdministrationShell (AAS)의 식별자를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAasId()}와 동일하다.
	 * 
	 * @return	AAS 식별자.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default String getAasId() {
		return getInstanceDescriptor().getAasId();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAasIdShort()}와 동일하다.
	 * 
	 * @return	idShort.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default @Nullable String getAasIdShort() {
		return getInstanceDescriptor().getAasIdShort();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId 를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getGlobalAssetId()}와 동일하다.
	 * 
	 * @return	자산 식별자.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default @Nullable String getGlobalAssetId() {
		return getInstanceDescriptor().getGlobalAssetId();
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAssetType()}와 동일하다.
	 * 
	 * @return	자산 타입.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default @Nullable String getAssetType() {
		return getInstanceDescriptor().getAssetType();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 자산 종류를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAssetKind()}와 동일하다.
	 * 
	 * @return	자산 종류.
	 * @see MDTInstance#getInstanceDescriptor()
	 */
	public default @Nullable AssetKind getAssetKind() {
		return getInstanceDescriptor().getAssetKind();
	}
	
	public MDTInstanceInfo getInfo();
	
	/**
	 * MDTInstance를 시작시킨다.
	 * <p>
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
	 * @throws ExecutionException 		MDTInstance 시작 과정에서 다른 이유로 실패한 경우.
	 */
	public void start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException,
				ExecutionException;
	
	/**
	 * 동작 중인 MDTInstance를 종료시킨다.
	 * <p>
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
	 * @throws ExecutionException		기타 다른 이유로 MDTInstance 종료가 실패한 경우.
	 */
	public void stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException,
				ExecutionException;
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell (AAS)의 서비스 객체
	 * {@link AssetAdministrationShellService}를 반환한다.
	 * <p>
	 * {@link AssetAdministrationShellService}를 활용하여 원격 AssetAdministrationShell을 조작할 수 있다.
	 * 
	 * @return	{@link AssetAdministrationShellService} 객체.
	 * @throws InvalidResourceStatusException	MDTInstance가 동작 중이지 않은 경우.
	 * @throws MDTInstanceManagerException		기타 다른 이유로 MDTInstance 종료가 실패한 경우.
	 * 
	 * @see	AssetAdministrationShellService
	 */
	public AssetAdministrationShellService getAssetAdministrationShellService()
		throws InvalidResourceStatusException, MDTInstanceManagerException;
	
	/**
	 * MDTInstance에 포함된 모든 하위 모델 (Submodel)들의 서비스 객체 ({@link SubmodelService})
	 * 리스트를 반환한다.
	 * <p>
	 * {@link SubmodelService}를 활용하여 하위 모델을 조작할 수 있다.
	 * 
	 * @return	{@link SubmodelService} 객체 리스트.
	 * @throws InvalidResourceStatusException	MDTInstance가 동작 중이지 않은 경우.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 하위 모델 검색에 실패한 경우.
	 * 
	 * @see	SubmodelService
	 */
	public List<SubmodelService> getAllSubmodelServices()
		throws ResourceNotFoundException, MDTInstanceManagerException;
	
	/**
	 * MDTInstance에 포함된 하위 모델 (Submodel)들 중에서 주어진 식별자를 가진 하위 모델의
	 * 서비스 객체 ({@link SubmodelService})를 반환한다.
	 * <p>
	 * MDTInstanceManager 전역에 거쳐 하나의 하위 모델 식별자를 갖는 오직 한 개의 하위 모델만 존재한다.
	 * 그러므로 MDTInstance에는 동일 하위 모델 식별자를 갖는 하위 모델이 여러개 존재할 수 없다.
	 * 
	 * {@link SubmodelService}를 활용하여 하위 모델을 조작할 수 있다.
	 * 
	 * @param submodelId	검색 대상 하위 모델의 식별자.
	 * @return	{@link SubmodelService} 객체.
	 * @throws ResourceNotFoundException	주어진 식별자의 하위 모델이 등록되어 있지 않은 경우.
	 * @throws MDTInstanceManagerException		기타 다른 이유로 MDTInstance 종료가 실패한 경우.
	 * 
	 * @see	SubmodelService
	 */
	public SubmodelService getSubmodelServiceById(String submodelId) throws ResourceNotFoundException;
	
	/**
	 * MDTInstance에 포함된 하위 모델 (Submodel)들 중에서 주어진 idShort 식별자를 가진 하위 모델의
	 * 서비스 객체 ({@link SubmodelService})를 반환한다.
	 * <p>
	 * 한 MDTInstance에는 동일 idShort 식별자를 갖는 하위 모델이 복수개 존재할 수 없다.
	 * 그러나 MDTInstanceManager 전역에는 동일 idShort 식별자를 갖는 하위 모델이 여러개 존재할 수 있다.
	 * 그러므로  MDTInstanceManager 전역에서 MDTInstance 식별자와 하위 모델 idShort 식별자를 사용하면
	 * 유일한 하위 모델을 접근할 수 있다.
	 * 
	 * {@link SubmodelService}를 활용하여 하위 모델을 조작할 수 있다.
	 * 
	 * @param submodelIdShort	검색 대상 하위 모델의 idShort 식별자.
	 * @return	{@link SubmodelService} 객체.
	 * @throws ResourceNotFoundException	주어진 식별자의 하위 모델이 등록되어 있지 않은 경우.
	 * @throws MDTInstanceManagerException		기타 다른 이유로 MDTInstance 종료가 실패한 경우.
	 * 
	 * @see	SubmodelService
	 */
	public SubmodelService getSubmodelServiceByIdShort(String submodelIdShort) throws ResourceNotFoundException;
	
	public List<SubmodelService> getAllSubmodelServiceBySemanticId(String semanticId);
	public default SubmodelService getInformationModelSubmodel() throws ResourceNotFoundException {
		List<SubmodelService> found = getAllSubmodelServiceBySemanticId(InformationModel.SEMANTIC_ID);
		return Funcs.getFirst(found)
					.getOrThrow(() -> new ResourceNotFoundException("SubmodelService",
																	"semanticId=" + InformationModel.SEMANTIC_ID));
	}
	public default SubmodelService getDataSubmodel() throws ResourceNotFoundException {
		List<SubmodelService> found = getAllSubmodelServiceBySemanticId(Data.SEMANTIC_ID);
		if ( found.size() == 0 ) {
			throw new ResourceNotFoundException("SubmodelService", "semanticId=" + Data.SEMANTIC_ID);
		}
		
		return found.get(0);
	}

	public InformationModel getInformationModel() throws ResourceNotFoundException;
	public Data getData() throws ResourceNotFoundException;
	public List<Simulation> getAllSimulations();
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell (AAS)의 기술자를 반환한다.
	 * <p>
	 * {@link AssetAdministrationShellDescriptor}를 활용하여 AssetAdministrationShell의
	 * 등록 정보를 접근할 수 있다.
	 * {@link AssetAdministrationShellRegistry} 인터페이스를 활용하면 AAS의 식별자를 통해
	 * 동일 정보를 접근할 수 있다.
	 * 
	 * @return	{@link AssetAdministrationShellDescriptor} 객체.
	 * @throws MDTInstanceManagerException		AssetAdministrationShell 기술자 접근에 실패한 경우.
	 * 
	 * @see	AssetAdministrationShellDescriptor
	 * @see	AssetAdministrationShellRegistry
	 */
	public AssetAdministrationShellDescriptor getAASDescriptor();
	
	/**
	 * MDTInstance에 포함된 모든 하위 모델 (Submodel)들의 기술자 리스트를 반환한다.
	 * <p>
	 * {@link SubmodelService}를 활용하여 하위 모델을 조작할 수 있다.
	 * 
	 * @return	{@link SubmodelService} 객체 리스트.
	 * @throws InvalidResourceStatusException	MDTInstance가 동작 중이지 않은 경우.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 하위 모델 검색에 실패한 경우.
	 * 
	 * @see	SubmodelService
	 */
	public List<SubmodelDescriptor> getAllSubmodelDescriptors();
	
	/**
	 * 하위 모델 식별에 해당하는 기술자를 반환한다.
	 * 
	 * @param submodelId	하위 모델 식별자
	 * @return	하위 모델 식별자
	 * @throws ResourceNotFoundException	식별자에 해당하는 하위 모델 기술자가 없는 경우.
	 */
	public default SubmodelDescriptor getSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodelId);
		
		return getAllSubmodelDescriptors().stream()
								.filter(desc -> desc.getId().equals(submodelId))
								.findAny()
								.orElseThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}
	
	/**
	 * idShort에 해당하는 하위 모델의 기술자를 반환한다.
	 * 
	 * @param submodelIdShort	검색에 사용할 하위 모델의 idShort.
	 * @return	하위 모델 식별자
	 * @throws ResourceNotFoundException	식별자에 해당하는 하위 모델 기술자가 없는 경우.
	 */
	public default List<SubmodelDescriptor> getAllSubmodelDescriptorByIdShort(String submodelIdShort)
		throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodelIdShort);
		
		return getAllSubmodelDescriptors().stream()
								.filter(desc -> submodelIdShort.equals(desc.getIdShort()))
								.toList();
	}
	
	public default List<SubmodelDescriptor> getAllSubmodelDescriptorBySemanticId(String semanticId) {
		Preconditions.checkNotNull(semanticId);
		
		return getAllSubmodelDescriptors().stream()
										.filter(desc -> {
											Reference ref = desc.getSemanticId();
											if ( ref != null ) {
												return semanticId.equals(ref.getKeys().get(0).getValue());
											}
											else {
												return false;
											}
										})
										.toList();
	}
	
	/**
	 * MDTInstance의 기술자를 반환한다.
	 * 
	 * @return	MDTInstance의 기술자.
	 */
	public InstanceDescriptor getInstanceDescriptor();
	
	/**
	 * MDTInstance에 포함된 모든 MDTInstance 하위 모델 기술자들을 반환한다.
	 * 
	 *  @return		MDTInstance 하위 모델 기술자 리스트.
	 */
	public default List<InstanceSubmodelDescriptor> getAllInstanceSubmodelDescriptors() {
		return getInstanceDescriptor().getInstanceSubmodelDescriptors();
	}
	
	/**
	 * 하위 모델 식별자에 해당하는 MDTInstance 하위 모델 기술자를 반환한다.
	 * 
	 * @param submodelId	대상 하위 모델의 식별자.
	 * @return	MDTInstance 하위 모델 기술자
	 * @throws ResourceNotFoundException	idShort에 해당하는 하위 모델 기술자가 없는 경우.
	 */
	public default InstanceSubmodelDescriptor getInstanceSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException {
		return Funcs.findFirst(getInstanceDescriptor().getInstanceSubmodelDescriptors(),
								isd -> isd.getId().equals(submodelId))
					.getOrThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}
	
	/**
	 * 주어진 idShort에 해당하는 MDTInstance 하위 모델 기술자를 반환한다.
	 * 
	 * @param idShort	대상 하위 모델의 idShort
	 * @return	MDTInstance 하위 모델 기술자
	 * @throws ResourceNotFoundException	idShort에 해당하는 하위 모델 기술자가 없는 경우.
	 */
	public default InstanceSubmodelDescriptor getInstanceSubmodelDescriptorByIdShort(String idShort)
		throws ResourceNotFoundException {
		return Funcs.findFirst(getInstanceDescriptor().getInstanceSubmodelDescriptors(),
								isd -> isd.getIdShort().equals(idShort))
					.getOrThrow(() -> new ResourceNotFoundException("Submodel",
														String.format("instance[%s].%s", getId(), idShort)));
	}

	public default List<InstanceSubmodelDescriptor> getAllInstanceSubmodelDescriptorBySemanticId(String semanticId) {
		return Funcs.filter(getInstanceDescriptor().getInstanceSubmodelDescriptors(),
							isd -> isd.getSemanticId() != null &&  isd.getSemanticId().equals(semanticId));
	}
	
	public String getOutputLog() throws IOException;
}
