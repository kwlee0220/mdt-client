package mdt.aas;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ShellRegistry {
	/**
	 * 등록된 모든 Asset administration shell descriptor 들을 반환한다.
	 * 
	 * @return	Asset administration shell descriptor 리스트.
	 */
	@GetExchange("/shell-descriptors")
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors();
	
	/**
	 * 주어진 식별자에 해당하는 {@link AssetAdministrationShellDescriptor}를 반환한다.
	 * 
	 * @param shellId		AssetAdministrationShell 식별자.
	 * @return AssetAdministrationShellDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 AssetAdministrationShellDescriptor가 없는 경우.
	 */
	@GetExchange("/shell-descriptors/{shellId}")
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(
																		@PathVariable(name="shellId") String shellId)
		throws ResourceNotFoundException;

	@GetExchange("/shell-descriptors/idShort/{idShort}")
    public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptorsByIdShort(
    																	@PathVariable(name="idShort") String idShort);
	
	/**
	 * 주어진 전역 자산 식별자에 해당하는 {@link AssetAdministrationShellDescriptor}를 반환한다.
	 * 
	 * @param assetId		자산 식별자.
	 * @return AssetAdministrationShellDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 AssetAdministrationShellDescriptor가 없는 경우.
	 */
	@GetExchange("/shell-descriptors/asset/{assetId}")
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptorByAssetId(
																		@PathVariable(name="assetId") String assetId);
	
	/**
	 * 주어진 AssetAdministrationShellDescriptor를 등록시킨다.
	 * 
	 * @param descriptor	AssetAdministrationShellDescriptor 객체.
	 * @return				등록된 AssetAdministrationShellDescriptor 객체.
	 * @throws ResourceAlreadyExistsException	동일 식별자에 해당하는 AssetAdministrationShellDescriptor가
	 * 									이미 존재하는 경우
	 */
	@PostExchange("/shell-descriptors")
	public AssetAdministrationShellDescriptor addAssetAdministrationShellDescriptor(
														@RequestBody AssetAdministrationShellDescriptor descriptor)
		throws ResourceAlreadyExistsException;
	
	/**
	 * 기존에 등록된  AssetAdministrationShellDescriptor를 주어진 것으로 갱신시킨다.
	 * 
	 * @param descriptor	변경시킬 AssetAdministrationShellDescriptor 객체.
	 * @return	갱신된 AssetAdministrationShellDescriptor 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 AssetAdministrationShellDescriptor가 없는 경우
	 */
	@PutExchange("/shell-descriptors")
	public AssetAdministrationShellDescriptor updateAssetAdministrationShellDescriptor(
														@RequestBody AssetAdministrationShellDescriptor descriptor)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 식별자에 해당하는 등록 AssetAdministrationShellDescriptor를 해제시킨다.
	 * 
	 * @param shellId		해제시킬 AssetAdministrationShellDescriptor의 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 AssetAdministrationShellDescriptor가 존재하지 않는 경우.
	 */
	@DeleteExchange("/shell-descriptors/{shellId}")
	public void removeAssetAdministrationShellDescriptorById(@PathVariable(name="shellId") String shellId)
		throws ResourceNotFoundException;
}
