package mdt.model.instance;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;

import utils.InternalException;

import mdt.model.registry.ResourceNotFoundException;


/**
 * {@link MDTInstanceManager}는 MDTInstance들을 관리하는 관리자 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManager {
	public static final String CANONICAL_FA3ST_JAR_FILE = "faaast.starter-all.jar";
	public static final String CANONICAL_MODEL_FILE = "model.json";
	public static final String CANONICAL_CONF_FILE = "conf.json";
	public static final String CANONICAL_CERT_FILE = "mdt_cert.p12";
	
	/**
	 * 등록된 MDTInstance의 갯수를 반환한다.
	 * 
	 * @return	MDTInstance 갯수.
	 */
	public long countInstances();
    
	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 * 				식별자에 해당하는 {@link MDTInstance} 객체가 등록되어 있지 않은 경우.
	 */
	public MDTInstance getInstance(String id) throws ResourceNotFoundException;
	
	/**
	 * 주어진 식별자에 해당하는 {@link MDTInstance} 객체의 등록 여부를 반환된다.
	 * 
	 * @param id	검색 대상 식별자.
	 * @return	MDTInstance의 등록 여부.
	 */
	public default boolean existsInstance(String id) {
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
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 * 				식별자에 해당하는 {@link MDTInstance} 객체가 등록되어 있지 않은 경우.
	 */
	public default MDTInstance getInstanceByAasId(String aasId) throws ResourceNotFoundException {
		String filter = String.format("instance.aasId = '%s'", aasId);
		List<MDTInstance> instList = getAllInstancesByFilter(filter);
		if ( instList.size() == 1 ) {
			return instList.get(0);
		}
		else if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			throw new InternalException("multiple MDTInstances for aasId: " + aasId);
		}
	}
	
	/**
	 * AAS idShort에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param idShort	검색 대상 AAS idShort.
	 * @return		식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 식별자에 해당하는 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public default List<MDTInstance> getAllInstancesByAasIdShort(String idShort) {
		String filter = String.format("instance.aasIdShort = '%s'", idShort);
		return getAllInstancesByFilter(filter);
	}

	/**
	 * 자산 식별자에 해당하는 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param assetId	검색 대상 자산 식별자.
	 * @return		자산 식별자에 해당하는 {@link MDTInstance} 객체.
	 * 				만일 자산 식별자에 해당하는 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public default List<MDTInstance> getAllInstancesByAssetId(String assetId) {
		String filter = String.format("instance.globalAssetId = '%s'", assetId);
		return getAllInstancesByFilter(filter);
	}

	/**
	 * 주어진 Submodel 식별자를 포함한 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param submodelId	검색에 사용할 Submodel 식별자.
	 * @return		Submodel 식별자를 포함한 {@link MDTInstance} 객체.
	 * 				만일 Submodel 식별자를 포함한 MDT instance가 없는 경우는
	 * 				{@link ResourceNotFoundException} 예외가 발생된다.
	 * @throws ResourceNotFoundException
	 * 				Submodel 식별자를 포함한 {@link MDTInstance} 객체가 등록되어 있지 않은 경우.
	 */
	public default MDTInstance getInstanceBySubmodelId(String submodelId) throws ResourceNotFoundException {
		String filter = String.format("submodel.id = '%s'", submodelId);
		List<MDTInstance> instList = getAllInstancesByFilter(filter);
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
	 * 주어진 Submodel의 idShort를 포함한 모든 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param 	submodelIdShort	검색에 사용할 Submodel의 idShort.
	 * @return		Submodel의 idShort를 포함한 {@link MDTInstance} 객체들의 리스트.
	 * 				만일 Submodel의 idShort를 포함한 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public default List<MDTInstance> getAllInstancesBySubmodelIdShort(String submodelIdShort)
		throws ResourceNotFoundException {
		String filter = String.format("submodel.idShort = '%s'", submodelIdShort);
		return getAllInstancesByFilter(filter);
	}
	
	/**
	 * MDT 관리자에 등록된 모든 {@link MDTInstance}를 반환한다.
	 * 
	 * @return	등록된 {@link MDTInstance}들의 리스트.
	 */
	public List<MDTInstance> getAllInstances();

	/**
	 * 주어진 filter 조건을 만족하는 {@link MDTInstance} 객체를 반환한다.
	 * 
	 * @param filterExpr	검색에 사용할 조건 표현식.
	 * @return		검색 조건에 해당하는 {@link MDTInstance} 객체 리스트.
	 * 				만일 검색 조건을 만족하는 MDT instance가 없는 경우는 empty 리스트가 반환된다.
	 */
	public List<MDTInstance> getAllInstancesByFilter(String filterExpr);

	/**
	 * Docker에 저장된 image를 MDT instace로 등록시킨다.
	 * 
	 * @param id			저장할 MDTInstance의 식별자.
	 * @param aasFile		등록할 asset의 AAS Environment 정보 파일 경로.
	 * @param arguments 	Instance 구동에 필요한 argument 정보.
	 * 						Json으로 serializable해야 함.
	 * @return 등록된 MDT Instace 객체.
	 * @throws MDTInstanceManagerException	기타 다른 이유로 MDTInstance 등록에 실패한 경우.
	 */
	public MDTInstance addInstance(String id, File aasFile, String arguments)
		throws MDTInstanceManagerException;
	
	/**
	 * 등록된 MDT Instance을 해제시킨다.
	 * 
	 * @param id	해제시킬 MDT Instance의 식별자.
	 * @throws MDTInstanceManagerException	등록 해제가 실패한 경우.
	 */
	public void removeInstance(String id) throws MDTInstanceManagerException;

	/**
	 * MDT 시스템에 등록된 모든 {@link MDTInstance}를 등록 해제시킨다.
	 * 
	 * @throws MDTInstanceManagerException	등록 해제가 실패한 경우.
	 */
	public void removeAllInstances() throws MDTInstanceManagerException;
}
