package mdt.model.registry;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AASRegistry {
	/**
	 * 등록된 모든 Asset administration shell descriptor 들을 반환한다.
	 * 
	 * @return	Asset administration shell descriptor 리스트.
	 * @throws RegistryException	Descriptor 획득 과정 중 오류가 발생한 경우.
	 */
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors()
		throws RegistryException;
	
    public List<AssetAdministrationShellDescriptor>
    getAllAssetAdministrationShellDescriptorsByIdShort(String idShort) throws RegistryException;
	
	/**
	 * 주어진 식별자에 해당하는 {@link AssetAdministrationShellDescriptor}를 반환한다.
	 * 
	 * @param aasId		AssetAdministrationShell 식별자.
	 * @return AssetAdministrationShellDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 AssetAdministrationShellDescriptor가 없는 경우.
	 * @throws RegistryException		Descriptor 획득 과정 중 오류가 발생한 경우.
	 */
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String aasId)
		throws ResourceNotFoundException, RegistryException;
	
	/**
	 * 주어진 전역 자산 식별자에 해당하는 {@link AssetAdministrationShellDescriptor}를 반환한다.
	 * 
	 * @param assetId		자산 식별자.
	 * @return AssetAdministrationShellDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 AssetAdministrationShellDescriptor가 없는 경우.
	 * @throws RegistryException		Descriptor 획득 과정 중 오류가 발생한 경우.
	 */
	public List<AssetAdministrationShellDescriptor>
	getAssetAdministrationShellDescriptorByGlobalAssetId(String assetId) throws ResourceNotFoundException,
																				RegistryException;
	
	/**
	 * 주어진 AssetAdministrationShellDescriptor를 등록시킨다.
	 * 
	 * @param descriptor	AssetAdministrationShellDescriptor 객체.
	 * @return				등록된 AssetAdministrationShellDescriptor 객체.
	 * @throws ResourceAlreadyExistsException	동일 식별자에 해당하는 AssetAdministrationShellDescriptor가
	 * 									이미 존재하는 경우
	 * @throws RegistryException		Descriptor 등록 과정 중 오류가 발생한 경우.
	 */
	public AssetAdministrationShellDescriptor postAssetAdministrationShellDescriptor(
																AssetAdministrationShellDescriptor descriptor)
		throws ResourceAlreadyExistsException, RegistryException;
	
	/**
	 * 기존에 등록된  AssetAdministrationShellDescriptor를 주어진 것으로 갱신시킨다.
	 * 
	 * @param descriptor	변경시킬 AssetAdministrationShellDescriptor 객체.
	 * @return	갱신된 AssetAdministrationShellDescriptor 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 AssetAdministrationShellDescriptor가 없는 경우
	 * @throws RegistryException		Descriptor 갱신 과정 중 오류가 발생한 경우.
	 */
	public AssetAdministrationShellDescriptor putAssetAdministrationShellDescriptorById(
														AssetAdministrationShellDescriptor descriptor)
		throws ResourceNotFoundException, RegistryException;
	
	/**
	 * 주어진 식별자에 해당하는 등록 AssetAdministrationShellDescriptor를 해제시킨다.
	 * 
	 * @param aasId		해제시킬 AssetAdministrationShellDescriptor의 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 AssetAdministrationShellDescriptor가 존재하지 않는 경우.
	 * @throws RegistryException	Descriptor 삭제 과정 중 오류가 발생한 경우.
	 */
	public void deleteAssetAdministrationShellDescriptorById(String aasId)
		throws ResourceNotFoundException, RegistryException;
}
