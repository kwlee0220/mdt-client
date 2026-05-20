package mdt.model.instance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import utils.Preconditions;

import mdt.model.InvalidResourceStatusException;
import mdt.model.ModelValidationException;
import mdt.model.ResourceNotFoundException;


/**
 * MDT 프레임워크가 관리하는 {@link MDTInstance}들의 등록/조회/해제를 담당하는 관리자 인터페이스이다.
 * <p>
 * 등록된 MDTInstance는 다양한 식별자 (MDTInstance id, AAS id/idShort, Asset id, Submodel id/idShort 등)로
 * 조회할 수 있으며, {@link #getInstanceAllByFilter(String)}를 통해 filter 표현식 기반의 일반 검색도 가능하다.
 * <p>
 * 별도로 명시되지 않는 한, 모든 메서드는 {@code null} 인자를 허용하지 않으며 {@code null}이 전달되면
 * {@link IllegalArgumentException}이 발생한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManager {
	public static final String MDT_INSTANCE_JAR_FILE_NAME = "mdt-instance-all.jar";
	public static final String MODEL_FILE_NAME = "model.json";
	public static final String MODEL_AASX_NAME = "model.aasx";
	public static final String CONF_FILE_NAME = "config.json";
	public static final String CERT_FILE_NAME = "mdt_cert.p12";
	public static final String GLOBAL_CONF_FILE_NAME = "mdt_global_config.json";
	
	/**
	 * 등록된 MDTInstance의 총 개수를 반환한다.
	 *
	 * @return	MDTInstance 개수.
	 */
	public long countInstances();

	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 *
	 * @param id	검색 대상 MDTInstance 식별자.
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 {@link MDTInstance}가 등록되어 있지 않은 경우.
	 */
	public MDTInstance getInstance(String id) throws ResourceNotFoundException;
	
	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance}의 등록 여부를 반환한다.
	 *
	 * @param id	검색 대상 MDTInstance 식별자.
	 * @return	등록되어 있으면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public default boolean existsInstance(String id) {
		Preconditions.checkNotNullArgument(id, "MDTInstance id is null");
		
		try {
			getInstance(id);
			return true;
		}
		catch ( ResourceNotFoundException e ) {
			return false;
		}
	}
	
	/**
	 * AAS 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 *
	 * @param aasId	검색 대상 AAS 식별자.
	 * @return		해당 AAS 식별자를 갖는 {@link MDTInstance} 객체.
	 * @throws ResourceNotFoundException	해당 AAS 식별자를 갖는 {@link MDTInstance}가 등록되어 있지 않은 경우.
	 * @throws MDTInstanceManagerException	동일 AAS 식별자를 갖는 {@link MDTInstance}가 두 개 이상 검색된 경우.
	 */
	public default MDTInstance getInstanceByAasId(String aasId) throws ResourceNotFoundException {
		Preconditions.checkNotNullArgument(aasId, "AAS id is null");
		
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<? extends MDTInstance> instList = getInstanceAllByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.isEmpty() ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			throw new MDTInstanceManagerException("multiple MDTInstances for aasId: " + aasId);
		}
	}
	
	/**
	 * 주어진 AAS idShort를 갖는 모든 {@link MDTInstance} 객체를 반환한다.
	 * <p>
	 * MDT 프레임워크에는 동일 AAS idShort를 가지는 MDTInstance가 여러 개 존재할 수 있기 때문에,
	 * 검색된 모든 MDTInstance의 리스트를 반환한다.
	 *
	 * @param idShort	검색 대상 AAS idShort.
	 * @return		해당 AAS idShort를 갖는 {@link MDTInstance} 객체 리스트.
	 * 				일치하는 MDTInstance가 없는 경우 빈 리스트를 반환한다.
	 */
	public default List<? extends MDTInstance> getInstanceAllByAasIdShort(String idShort) {
		Preconditions.checkNotNullArgument(idShort, "AAS idShort is null");
		
		String filter = String.format("instance.aasIdShort = '%s'", idShort);
		return getInstanceAllByFilter(filter);
	}

	/**
	 * 주어진 globalAssetId를 갖는 모든 {@link MDTInstance} 객체를 반환한다.
	 * <p>
	 * MDT 프레임워크에는 동일 자산 식별자를 가지는 MDTInstance가 여러 개 존재할 수 있기 때문에,
	 * 검색된 모든 MDTInstance의 리스트를 반환한다.
	 *
	 * @param assetId	검색 대상 globalAssetId.
	 * @return		해당 자산 식별자를 갖는 {@link MDTInstance} 객체 리스트.
	 * 				일치하는 MDTInstance가 없는 경우 빈 리스트를 반환한다.
	 */
	public default List<? extends MDTInstance> getInstanceAllByAssetId(String assetId) {
		Preconditions.checkNotNullArgument(assetId, "AssetId is null");
		
		String filter = String.format("instance.globalAssetId = '%s'", assetId);
		return getInstanceAllByFilter(filter);
	}

	/**
	 * 주어진 Submodel 식별자를 포함한 {@link MDTInstance} 객체를 반환한다.
	 *
	 * @param submodelId	검색에 사용할 Submodel 식별자.
	 * @return		해당 Submodel을 포함한 {@link MDTInstance} 객체.
	 * @throws ResourceNotFoundException	해당 Submodel을 포함한 {@link MDTInstance}가 등록되어 있지 않은 경우.
	 * @throws MDTInstanceManagerException	동일 Submodel 식별자를 포함한 {@link MDTInstance}가 두 개 이상 검색된 경우.
	 */
	public default MDTInstance getInstanceBySubmodelId(String submodelId) throws ResourceNotFoundException {
		Preconditions.checkNotNullArgument(submodelId, "Submodel id is null");

		String filter = String.format("submodel.id = '%s'", submodelId);
		List<? extends MDTInstance> instList = getInstanceAllByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.isEmpty() ) {
			throw new ResourceNotFoundException("Submodel", "id=" + submodelId);
		}
		else {
			throw new MDTInstanceManagerException("multiple MDTInstances exist of submodel-id: " + submodelId);
		}
	}

	/**
	 * 주어진 Submodel idShort를 포함한 모든 {@link MDTInstance} 객체를 반환한다.
	 * <p>
	 * MDT 프레임워크에는 동일 Submodel idShort를 가지는 MDTInstance가 여러 개 존재할 수 있기 때문에,
	 * 검색된 모든 MDTInstance의 리스트를 반환한다.
	 *
	 * @param submodelIdShort	검색에 사용할 Submodel의 idShort.
	 * @return		해당 Submodel idShort를 포함한 {@link MDTInstance} 객체 리스트.
	 * 				일치하는 MDTInstance가 없는 경우 빈 리스트를 반환한다.
	 */
	public default List<? extends MDTInstance> getInstanceAllBySubmodelIdShort(String submodelIdShort) {
		Preconditions.checkNotNullArgument(submodelIdShort, "Submodel idShort is null");
		
		String filter = String.format("submodel.idShort = '%s'", submodelIdShort);
		return getInstanceAllByFilter(filter);
	}
	
	/**
	 * 등록된 모든 {@link MDTInstance}를 반환한다.
	 *
	 * @return	등록된 {@link MDTInstance} 객체 리스트.
	 * 			등록된 인스턴스가 없는 경우 빈 리스트를 반환한다.
	 */
	public List<? extends MDTInstance> getInstanceAll();

	/**
	 * 주어진 filter 조건을 만족하는 {@link MDTInstance} 객체를 반환한다.
	 * <p>
	 * Filter 조건에서 사용할 수 있는 속성 정보는 다음과 같다.
	 * <table border="1">
	 * 	<caption>사용 가능한 MDTInstance 속성 정보</caption>
	 * 	<tr>
	 * 		<td>id</td>
	 * 		<td>MDTInstance 고유 식별자</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>status</td>
	 * 		<td>동작 상태. ADDING, STOPPED, STARTING, RUNNING, STOPPING, FAILED, REMOVED 중 하나</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>baseEndpoint</td>
	 * 		<td>기본 접속 정보</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>aasId</td>
	 * 		<td>AssetAdministrationShell의 식별자</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>aasIdShort</td>
	 * 		<td>AssetAdministrationShell의 idShort</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>globalAssetId</td>
	 * 		<td>자산 식별자</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>assetType</td>
	 * 		<td>자산 타입. Machine, Process, Line, Factory 중 하나</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>submodel.id</td>
	 * 		<td>서브모델 식별자</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>submodel.idShort</td>
	 * 		<td>서브모델 idShort</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>submodel.semanticId</td>
	 * 		<td>서브모델 의미 식별자</td>
	 * 	</tr>
	 * </table>
	 * <p>
	 * 표현식은 {@code <속성> <비교연산자> <리터럴>} 형식의 단일 조건이며,
	 * {@code =} 와 {@code !=} 비교 연산자를 지원한다. 문자열 리터럴은 작은따옴표(')로 감싼다.
	 * <p>예시:
	 * <pre>{@code
	 * "instance.aasId = 'urn:example:aas:001'"
	 * "instance.status = 'RUNNING'"
	 * "instance.status != 'RUNNING'"
	 * "submodel.semanticId = 'https://example.com/Simulation'"
	 * }</pre>
	 *
	 * @param filterExpr	검색에 사용할 조건 표현식.
	 * @return		검색 조건에 해당하는 {@link MDTInstance} 객체 리스트.
	 * 				일치하는 MDTInstance가 없는 경우 빈 리스트를 반환한다.
	 */
	public List<? extends MDTInstance> getInstanceAllByFilter(String filterExpr);
	
	/**
	 * 새로운 MDTInstance를 등록한다.
	 * <p>
	 * 구현체가 디렉터리인 경우 zip으로 압축된 뒤 등록된다.
	 *
	 * @param id           등록할 MDTInstance의 식별자.
	 * @param port         사용할 포트 번호. {@code -1}이면 자동 할당.
	 * @param instanceFile 인스턴스 구현체 경로 (디렉터리 또는 zip 파일).
	 * @return 등록된 {@link MDTInstance} 객체.
	 * @throws ModelValidationException		인스턴스 구현체에 포함된 모델의 검증에 실패한 경우.
	 * @throws IOException					인스턴스 구현체 파일 읽기/압축 과정에서 입출력 오류가 발생한 경우.
	 * @throws MDTInstanceManagerException	등록 과정에서 기타 오류가 발생한 경우.
	 */
	public MDTInstance addInstance(String id, int port, File instanceFile)
		throws ModelValidationException, IOException, MDTInstanceManagerException;

	/**
	 * 새로운 MDTInstance를 자동 할당 포트로 등록한다.
	 * <p>
	 * {@link #addInstance(String, int, File)}에 포트 번호 {@code -1}(자동 할당)을 전달하는 것과 같다.
	 *
	 * @param id           등록할 MDTInstance의 식별자.
	 * @param instanceFile 인스턴스 구현체 경로 (디렉터리 또는 zip 파일).
	 * @return 등록된 {@link MDTInstance} 객체.
	 * @throws ModelValidationException		인스턴스 구현체에 포함된 모델의 검증에 실패한 경우.
	 * @throws IOException					인스턴스 구현체 파일 읽기/압축 과정에서 입출력 오류가 발생한 경우.
	 * @throws MDTInstanceManagerException	등록 과정에서 기타 오류가 발생한 경우.
	 */
	public default MDTInstance addInstance(String id, File instanceFile)
		throws ModelValidationException, IOException, MDTInstanceManagerException {
		return addInstance(id, -1, instanceFile);
	}

	/**
	 * 등록된 MDTInstance를 해제한다.
	 * <p>
	 * 주어진 식별자에 해당하는 MDTInstance가 존재하지 않는 경우는 호출이 무시된다.
	 *
	 * @param id	해제할 MDTInstance의 식별자.
	 * @throws InvalidResourceStatusException	해제 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}인 경우.
	 * @throws MDTInstanceManagerException		등록 해제 과정에서 기타 오류가 발생한 경우.
	 */
	public void removeInstance(String id) throws InvalidResourceStatusException, MDTInstanceManagerException;

	/**
	 * 등록된 모든 {@link MDTInstance}를 해제한다.
	 *
	 * @throws MDTInstanceManagerException	등록 해제 과정에서 오류가 발생한 경우.
	 */
	public void removeInstanceAll() throws MDTInstanceManagerException;
}
