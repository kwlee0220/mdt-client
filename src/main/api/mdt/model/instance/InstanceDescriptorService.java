package mdt.model.instance;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import utils.InternalException;

import mdt.model.ResourceNotFoundException;


/**
 * {@link InstanceDescriptorService}는 MDTInstance들을 관리하는 관리자 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InstanceDescriptorService {
	/**
	 * MDT 관리자에 등록된 모든 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @return	등록된 {@link InstanceDescriptor}들의 리스트.
	 */
	@GetExchange("/instances")
	public List<InstanceDescriptor> getInstanceDescriptorAll();
    
	/**
	 * 주어진 식별자에 해당하는 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return		식별자에 해당하는 {@link InstanceDescriptor} 객체.
	 * 				만일 식별자에 해당하는 InstanceDescriptor가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 * 				식별자에 해당하는 {@link InstanceDescriptor}가 등록되어 있지 않은 경우.
	 */
	@GetExchange("/instances/{id}")
	public String getInstanceDescriptor(@PathVariable("id") String id) throws ResourceNotFoundException;
	
	/**
	 * 주어진 식별자에 해당하는 {@link InstanceDescriptor}의 등록 여부를 반환된다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return	InstanceDescriptor의 등록 여부.
	 */
	public default boolean existsInstanceDescriptor(String id) {
		try {
			getInstanceDescriptor(id);
			return true;
		}
		catch ( ResourceNotFoundException e ) {
			return false;
		}
	}
	
	/**
	 * AAS 식별자에 해당하는 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @param aasId	검색 대상 AAS 식별자.
	 * @return		식별자에 해당하는 {@link InstanceDescriptor}.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 * 				식별자에 해당하는 {@link InstanceDescriptor}가 등록되어 있지 않은 경우.
	 */
	public default InstanceDescriptor getInstanceDescriptorByAasId(String aasId) throws ResourceNotFoundException {
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<InstanceDescriptor> instList = getInstanceDescriptorAllByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("InstanceDescriptor", "aasId=" + aasId);
		}
		else {
			throw new InternalException("multiple InstanceDescriptor for aasId: " + aasId);
		}
	}
	
	/**
	 * AAS idShort에 해당하는 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @param idShort	검색 대상 AAS idShort.
	 * @return		식별자에 해당하는 {@link InstanceDescriptor}.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public default List<InstanceDescriptor> getAllInstanceAllByAasIdShort(String idShort) {
		String filter = String.format("instance.aasIdShort = '%s'", idShort);
		return getInstanceDescriptorAllByFilter(filter);
	}

	/**
	 * 자산 식별자에 해당하는 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @param assetId	검색 대상 자산 식별자.
	 * @return		자산 식별자에 해당하는 {@link InstanceDescriptor}.
	 * 				만일 자산 식별자에 해당하는 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public default List<InstanceDescriptor> getAllInstanceAllByAssetId(String assetId) {
		String filter = String.format("instance.globalAssetId = '%s'", assetId);
		return getInstanceDescriptorAllByFilter(filter);
	}

	/**
	 * 주어진 Submodel 식별자를 포함한 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @param submodelId	검색에 사용할 Submodel 식별자.
	 * @return		Submodel 식별자를 포함한 {@link InstanceDescriptor}.
	 * 				만일 Submodel 식별자를 포함한 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 * 				Submodel 식별자를 포함한 {@link InstanceDescriptor}가 등록되어 있지 않은 경우.
	 */
	public default InstanceDescriptor getInstanceBySubmodelId(String submodelId) throws ResourceNotFoundException {
		String filter = String.format("submodel.id = '%s'", submodelId);
		List<InstanceDescriptor> instList = getInstanceDescriptorAllByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("Submodel", "id=" + submodelId);
		}
		else {
			throw new InternalException("multiple MDTInstances exist of submodel-id: " + submodelId);
		}
	}

	/**
	 * 주어진 Submodel의 idShort를 포함한 모든 {@link InstanceDescriptor}를 반환한다.
	 * 
	 * @param 	submodelIdShort	검색에 사용할 Submodel의 idShort.
	 * @return		Submodel의 idShort를 포함한 {@link InstanceDescriptor}들의 리스트.
	 * 				만일 Submodel의 idShort를 포함한 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public default List<InstanceDescriptor> getAllInstancesBySubmodelIdShort(String submodelIdShort)
		throws ResourceNotFoundException {
		String filter = String.format("submodel.idShort = '%s'", submodelIdShort);
		return getInstanceDescriptorAllByFilter(filter);
	}

	/**
	 * 주어진 filter 조건을 만족하는 {@link InstanceDescriptor}를 반환한다.
	 * <p>
	 * Filter 조건에서 사용할 수 있는 속성 정보는 다음과 같다.
	 * <table border="1">
	 * 	<tr>
	 * 		<td>id</td>
	 * 		<td>InstanceDescriptor 고유 식별자</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>status</td>
	 * 		<td>동작 상태 정보. STOPPED, STARTING, RUNNING, FAILED 중 하나</td>
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
	 * 		<td>자산 타입. Line, Process, Machine 중 하나</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>assetKind</td>
	 * 		<td>자산 종류. Template, Instance 중 하나</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>submodel.id</td>
	 * 		<td>서브모델 식별자</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>submodel.idShort</td>
	 * 		<td>서브모델 idshort</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>submodel.semanticId</td>
	 * 		<td>서브 모델 의미 식별자</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param filterExpr	검색에 사용할 조건 표현식.
	 * @return		검색 조건에 해당하는 {@link InstanceDescriptor} 리스트.
	 * 				만일 검색 조건을 만족하는 InstanceDescriptor가 없는 경우는 empty 리스트가 반환된다.
	 */
	@GetExchange("/instances")
	public List<InstanceDescriptor> getInstanceDescriptorAllByFilter(@RequestParam(name="filter") String filterExpr);
	
    @PostExchange("/instances")
    public String addInstance(@RequestParam("id") String id, @RequestParam("port") int port,
								@RequestParam("zipFile") MultipartFile zipFile)
		throws IOException, InterruptedException;
	
	/**
	 * 등록된 MDT Instance을 해제시킨다.
	 * 
	 * @param id	해제시킬 MDT Instance의 식별자.
	 * @throws MDTInstanceManagerException	등록 해제가 실패한 경우.
	 */
	@DeleteExchange("/instances/{id}")
	public void removeInstance(@PathVariable("id") String id) throws ResourceNotFoundException;

	/**
	 * MDT 시스템에 등록된 모든 {@link MDTInstance}를 등록 해제시킨다.
	 * 
	 * @throws MDTInstanceManagerException	등록 해제가 실패한 경우.
	 */
	@DeleteExchange("/instances")
	public void removeAllInstances();
	
//	/**
//	 * 등록된 MDTInstance의 갯수를 반환한다.
//	 * 
//	 * @return	MDTInstance 갯수.
//	 */
//	public long countInstances();
}
