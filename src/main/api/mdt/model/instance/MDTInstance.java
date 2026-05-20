package mdt.model.instance;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import utils.func.FOption;

import mdt.aas.AssetAdministrationShellRegistry;
import mdt.model.AssetAdministrationShellService;
import mdt.model.InvalidResourceStatusException;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;


/**
 * {@code MDTInstance}는 MDT 프레임워크에 의해 관리/운영되는 MDT 인스턴스를
 * 다루기 위한 인터페이스를 정의한다.
 * <p>
 * {@code MDTInstance}에 의해 관리되는 속성은 {@link InstanceDescriptor}에 의해 정의되고,
 * 이는 {@link #getInstanceDescriptor()}를 통해 접근할 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstance {
	/**
	 * {@link MDTInstanceManager} 객체를 반환한다.
	 * 
	 * @return	{@link MDTInstanceManager} 객체.
	 */
	@NotNull public MDTInstanceManager getInstanceManager();
	
	/**
	 * MDTInstance의 식별자를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getId()}와 동일하다.
	 * 
	 * @return	식별자.
	 * @see #getInstanceDescriptor()
	 */
	@NotNull public default String getId() {
		return getInstanceDescriptor().getId();
	}
	
	/**
	 * MDTInstance의 상태를 반환한다.
	 *
	 * @return	상태 정보.
	 */
	@NotNull public MDTInstanceStatus getStatus();

	/**
	 * MDTInstance가 동작 중인지 여부를 반환한다.
	 *
	 * @return	동작 중이면 ({@link MDTInstanceStatus#RUNNING}) {@code true},
	 * 			그렇지 않으면 {@code false}.
	 */
	public default boolean isRunning() {
		return getStatus() == MDTInstanceStatus.RUNNING;
	}
	
	/**
	 * MDTInstance에 부여된 endpoint를 반환한다.
	 * <p>
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getBaseEndpoint()}와 동일하다.
	 * 
	 * @return	Endpoint 정보.
	 * @see #getInstanceDescriptor()
	 */
	public @Nullable String getServiceEndpoint();

	/**
	 * MDTInstance가 포함한 AssetAdministrationShell (AAS)의 식별자를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAasId()}와 동일하다.
	 * 
	 * @return	AAS 식별자.
	 * @see #getInstanceDescriptor()
	 */
	@NotNull public default String getAasId() {
		return getInstanceDescriptor().getAasId();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAasIdShort()}와 동일하다.
	 * 
	 * @return	idShort.
	 * @see #getInstanceDescriptor()
	 */
	@Nullable public default String getAasIdShort() {
		return getInstanceDescriptor().getAasIdShort();
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId 를 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getGlobalAssetId()}와 동일하다.
	 * 
	 * @return	자산 식별자.
	 * @see #getInstanceDescriptor()
	 */
	@Nullable public default String getGlobalAssetId() {
		return getInstanceDescriptor().getGlobalAssetId();
	}
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * <p>
	 * 내부적으로는 {@code getInstanceDescriptor().getAssetType()}와 동일하다.
	 * 
	 * @return	자산 타입.
	 * @see #getInstanceDescriptor()
	 */
	@Nullable public default MDTAssetType getAssetType() {
		return getInstanceDescriptor().getAssetType();
	}
	
	/**
	 * MDTInstance를 시작시킨다.
	 * <p>
	 * MDTInstance의 시작을 요청하고 실제로 성공적으로 동작할 때까지 대기한다.
	 * 시작 요청 후 동작할 때까지 내부적으로 주어진 {@code pollInterval} 간격으로
	 * 체크하는 작업을 반복한다. 만일 {@code pollInterval}이
	 * {@code null}인 경우는 대기 없이 시작 요청 후 바로 반환된다.
	 * <p>
	 * 제한시간 {@code timeout}이 {@code null}이 아닌 경우에는 이 제한시간 내로
	 * 시작되지 않는 경우에는 {@link TimeoutException} 오류를 발생시킨다.
	 * 시작 대기 시간 중 쓰레드가 중단된 경우에는 {@link InterruptedException} 예외가 발생된다.
	 * 
	 * @param pollInterval	시작 완료 체크 시간 간격.
	 * @param timeout	최대 대기 시간. {@code null}인 경우에는 무한 대기를 의미함.
	 * @return	메소드 반환시의 MDTInstance 상태.
	 * 			{@code pollInterval}이 {@code null}인 경우에는 시작 요청 후 바로 반환되므로,
	 * 			이 때의 상태는 {@link MDTInstanceStatus#STARTING}일 수 있다.
	 * 			{@code pollInterval}이 {@code null}이 아닌 경우에는 시작이 완료된 시점에서 반환되므로,
	 * 			대부분의 경우 이 때의 상태는 {@link MDTInstanceStatus#RUNNING}일 것이다.
	 * 			만일 시작 직후 다른 메소드 호출로 다른 상태가 반환될 수도 있다.
	 * @throws TimeoutException		대기 제한 시간을 경과하도록 시작이 완료되지 않은 경우.
	 * @throws InterruptedException	대기 중 쓰레드가 중단된 경우.
	 * @throws InvalidResourceStatusException	MDTInstance가 이미 동작 중인 경우.
	 * @throws ExecutionException 		MDTInstance 시작 과정에서 다른 이유로 실패한 경우.
	 */
	@NotNull public MDTInstanceStatus start(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException, ExecutionException;
	
	/**
	 * 동작 중인 MDTInstance를 중지시킨다.
	 * <p>
	 * MDTInstance의 종료를 요청하고 실제로 성공적으로 종료될 때까지 대기한다.
	 * 종료 요청 후 실제로 종료될 때까지 내부적으로 주어진 {@code pollInterval} 간격으로
	 * 체크하는 작업을 반복한다. 만일 {@code pollInterval}이
	 * {@code null}인 경우는 대기 없이 종료 요청 후 바로 반환된다.
	 * <p>
	 * 제한시간 {@code timeout}이 {@code null}이 아닌 경우에는 이 제한시간 내로
	 * 종료되지 않는 경우에는 {@link TimeoutException} 오류를 발생시킨다.
	 * 종료 대기 시간 중 쓰레드가 중단된 경우에는 {@link InterruptedException} 예외가 발생된다.
	 *
	 * @param pollInterval	종료 완료 체크 시간 간격.
	 * @param timeout	최대 대기 시간. {@code null}인 경우에는 무한 대기를 의미함.
	 * @return	메소드 반환 순간의 MDTInstance 상태.
	 * 			{@code pollInterval}이 {@code null}인 경우에는 중지 요청 후 바로 반환되므로,
	 * 			이 때의 상태는 {@link MDTInstanceStatus#STOPPING}일 수 있다.
	 * 			{@code pollInterval}이 {@code null}이 아닌 경우에는 완전히 종료된 시점에서 반환되므로,
	 * 			대부분의 경우 이 때의 상태는 {@link MDTInstanceStatus#STOPPED}일 것이다.
	 * 			만일 종료 직후 다른 메소드 호출로 다른 상태가 반환될 수도 있다.
	 * @throws TimeoutException		대기 제한 시간을 경과하도록 종료가 완료되지 않은 경우.
	 * @throws InterruptedException	대기 중 쓰레드가 중단된 경우.
	 * @throws InvalidResourceStatusException	MDTInstance가 동작 중이 아닌 경우.
	 * @throws ExecutionException		기타 다른 이유로 MDTInstance 종료가 실패한 경우.
	 */
	@NotNull public MDTInstanceStatus stop(@Nullable Duration pollInterval, @Nullable Duration timeout)
		throws TimeoutException, InterruptedException, InvalidResourceStatusException, ExecutionException;
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell (AAS)의 서비스 객체
	 * {@link AssetAdministrationShellService}를 반환한다.
	 * <p>
	 * {@link AssetAdministrationShellService}를 활용하여 원격 AssetAdministrationShell을 조작할 수 있다.
	 * 
	 * @return	{@link AssetAdministrationShellService} 객체.
	 * @throws InvalidResourceStatusException	MDTInstance가 동작 중이지 않은 경우.
	 * @throws MDTInstanceManagerException		기타 다른 이유로 AssetAdministrationShell 서비스 객체 획득에 실패한 경우.
	 *
	 * @see	AssetAdministrationShellService
	 */
	@NotNull public AssetAdministrationShellService getAssetAdministrationShellService()
		throws InvalidResourceStatusException, MDTInstanceManagerException;
	
	/**
	 * MDTInstance에 포함된 모든 하위 모델 (Submodel)들의 서비스 객체 ({@link SubmodelService})
	 * 리스트를 반환한다.
	 * <p>
	 * {@link SubmodelService}를 활용하여 하위 모델을 조작할 수 있다.
	 * 
	 * @return	{@link SubmodelService} 객체 리스트.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 하위 모델 검색에 실패한 경우.
	 *
	 * @see	SubmodelService
	 */
	@NotNull public List<? extends SubmodelService> getSubmodelServiceAll()
		throws MDTInstanceManagerException;
	
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
	 * @return	{@link SubmodelService} 객체를 담은 {@link FOption}.
	 * 			주어진 식별자의 하위 모델이 존재하지 않는 경우에는 {@link FOption#empty()}.
	 *
	 * @see	SubmodelService
	 */
	public FOption<SubmodelService> getSubmodelServiceById(String submodelId);
	
	/**
	 * MDTInstance에 포함된 하위 모델 (Submodel)들 중에서 주어진 idShort 식별자를 가진 하위 모델의
	 * 서비스 객체 ({@link SubmodelService})를 반환한다.
	 * <p>
	 * 한 MDTInstance에는 동일 idShort 식별자를 갖는 하위 모델이 복수개 존재할 수 없다.
	 * 그러나 MDTInstanceManager 전역에는 동일 idShort 식별자를 갖는 하위 모델이 여러개 존재할 수 있다.
	 * 그러므로 MDTInstanceManager 전역에서 MDTInstance 식별자와 하위 모델 idShort 식별자를 사용하면
	 * 유일한 하위 모델을 접근할 수 있다.
	 *
	 * {@link SubmodelService}를 활용하여 하위 모델을 조작할 수 있다.
	 *
	 * @param submodelIdShort	검색 대상 하위 모델의 idShort 식별자.
	 * @return	{@link SubmodelService} 객체를 담은 {@link FOption}.
	 * 			주어진 idShort 식별자의 하위 모델이 존재하지 않는 경우에는 {@link FOption#empty()}.
	 *
	 * @see	SubmodelService
	 */
	public FOption<SubmodelService> getSubmodelServiceByIdShort(String submodelIdShort);
	
	/**
	 * MDTInstance에 포함된 하위 모델 (Submodel)들 중에서 주어진 semanticId를 가진 하위 모델의
	 * 서비스 객체 ({@link SubmodelService}) 리스트를 반환한다.
	 * <p>
	 * 한 MDTInstance 내에 동일 semanticId를 갖는 하위 모델이 복수개 존재할 수 있으므로
	 * 결과는 리스트로 반환된다. 일치하는 하위 모델이 없는 경우 빈 리스트가 반환된다.
	 *
	 * @param semanticId	검색에 사용할 semanticId.
	 * @return	{@link SubmodelService} 객체 리스트.
	 *
	 * @see	SubmodelService
	 */
	@NotNull public List<SubmodelService> getSubmodelServiceAllBySemanticId(String semanticId);
	
	/**
	 * MDTInstance에 등록된 파라미터 서비스 목록을 반환한다.
	 *
	 * @return	MDTInstance에 등록된 {@link MDTParameterService} 객체 리스트.
	 * @throws ResourceNotFoundException	MDTInstance에 Data 서브모델이 존재하지 않는 경우.
	 */
	@NotNull public List<MDTParameterService> getParameterServiceAll() throws ResourceNotFoundException;
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell (AAS)의 기술자를 반환한다.
	 * <p>
	 * {@link AssetAdministrationShellDescriptor}를 활용하여 AssetAdministrationShell의
	 * 등록 정보를 접근할 수 있다.
	 * {@link AssetAdministrationShellRegistry} 인터페이스를 활용하면 AAS의 식별자를 통해
	 * 동일 정보를 접근할 수 있다.
	 * 
	 * @return	{@link AssetAdministrationShellDescriptor} 객체.
	 *
	 * @see	AssetAdministrationShellDescriptor
	 * @see	AssetAdministrationShellRegistry
	 */
	@NotNull public AssetAdministrationShellDescriptor getAASShellDescriptor();
	
	/**
	 * MDTInstance에 포함된 모든 하위 모델 (Submodel)들의 기술자 리스트를 반환한다.
	 *
	 * @return	{@link SubmodelDescriptor} 객체 리스트.
	 *
	 * @see	SubmodelDescriptor
	 */
	@NotNull public List<SubmodelDescriptor> getAASSubmodelDescriptorAll();
	
	/**
	 * MDTInstance에 등록된 파라미터 목록 객체를 반환한다.
	 *
	 * @return MDTInstance에 등록된 파라미터 목록 ({@link ParameterCollection}) 객체.
	 * @throws ResourceNotFoundException MDTInstance에 Data 서브모델이 존재하지 않는 경우.
	 */
	@NotNull public ParameterCollection getParameterCollection() throws ResourceNotFoundException;
	
	/**
	 * MDTInstance의 기술자를 반환한다.
	 * 
	 * @return	MDTInstance의 기술자.
	 */
	@NotNull public InstanceDescriptor getInstanceDescriptor();
	
	/**
	 * MDTInstance에 포함된 모든 MDT 하위 모델 기술자들을 반환한다.
	 *
	 * @return	{@link MDTSubmodelDescriptor} 객체 리스트.
	 */
	@NotNull public List<MDTSubmodelDescriptor> getMDTSubmodelDescriptorAll();
	
	/**
	 * MDTInstance에 포함된 모든 MDTOperation 기술자({@link MDTOperationDescriptor})들을 반환한다.
	 * 
	 * @return MDTOperation 기술자 목록.
	 */
	@NotNull public List<MDTOperationDescriptor> getMDTOperationDescriptorAll();
	
	/**
	 * MDTInstance에 포함된 TwinComposition 기술자({@link MDTTwinCompositionDescriptor})를 반환한다.
	 *
	 * @return	{@link MDTTwinCompositionDescriptor} 객체. TwinComposition 서브모델이 존재하지 않는
	 * 			경우에는 {@code null}.
	 */
	@Nullable public MDTTwinCompositionDescriptor getMDTTwinCompositionDescriptor();

	/**
	 * 본 MDTInstance가 포함(contain)하는 컴포넌트 MDTInstance 목록을 반환한다.
	 * <p>
	 * 내부적으로 {@code getTargetInstanceAllOfDependency("contain")}와 동일하다.
	 *
	 * @return	컴포넌트 {@link MDTInstance} 리스트.
	 */
	public default List<MDTInstance> getComponentInstanceAll() {
		return getTargetInstanceAllOfDependency("contain");
	}

	/**
	 * 본 MDTInstance를 의존성의 출처(source)로 하여 주어진 의존성 타입({@code depType})으로 연결된
	 * 대상(target) MDTInstance 목록을 반환한다.
	 *
	 * @param depType	의존성 타입.
	 * @return	대상 {@link MDTInstance} 리스트.
	 */
	public List<MDTInstance> getTargetInstanceAllOfDependency(String depType);

	/**
	 * 본 MDTInstance를 의존성의 대상(target)으로 하여 주어진 의존성 타입({@code depType})으로 연결된
	 * 출처(source) MDTInstance 목록을 반환한다.
	 *
	 * @param depType	의존성 타입.
	 * @return	출처 {@link MDTInstance} 리스트.
	 */
	public List<MDTInstance> getSourceInstanceAllOfDependency(String depType);

	/**
	 * MDTInstance의 출력 로그를 반환한다.
	 *
	 * @return	출력 로그 문자열.
	 * @throws IOException	로그 조회 과정에서 입출력 오류가 발생한 경우.
	 */
	public String getOutputLog() throws IOException;
}
