package mdt.aas;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
public interface SubmodelRegistry {
	/**
	 * 등록된 모든 SubmodelDescriptor 들을 반환한다.
	 * 
	 * @return	SubmodelDescriptor 리스트.
	 */
	@GetExchange("/submodel-descriptors")
	public List<SubmodelDescriptor> getAllSubmodelDescriptors();
	
	/**
	 * 주어진 식별자에 해당하는 {@link SubmodelDescriptor}를 반환한다.
	 * 
	 * @param submodelId	SubmodelDescriptor 식별자.
	 * @return	SubmodelDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 SubmodelDescriptor가 없는 경우
	 */
	@GetExchange("/submodel-descriptors/{submodelId}")
	public SubmodelDescriptor getSubmodelDescriptorById(@PathVariable("submodelId") String submodelId)
		throws ResourceNotFoundException;

	@GetExchange("/submodel-descriptors")
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsByIdShort(
											@Nullable @RequestParam(name="idShort", required=false) String idShort);
	
	/**
	 * 주어진 SubmodelDescriptor를 등록시킨다.
	 * 
	 * @param submodelDescriptor	SubmodelDescriptor 객체.
	 * @return				등록된 SubmodelDescriptor 객체.
	 * @throws ResourceAlreadyExistsException	동일 식별자에 해당하는 SubmodelDescriptor가
	 * 									이미 존재하는 경우
	 */
	@PostExchange("/submodel-descriptors")
	public SubmodelDescriptor postSubmodelDescriptor(@RequestBody SubmodelDescriptor submodelDescriptor)
		throws ResourceAlreadyExistsException;
	
	/**
	 * 기존에 등록된  SubmodelDescriptor를 주어진 것으로 갱신시킨다.
	 * 
	 * @param submodelDescriptor	변경시킬 SubmodelDescriptor 객체.
	 * @return	갱신된 SubmodelDescriptor 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 SubmodelDescriptor가 없는 경우
	 */
	@PutExchange("/submodel-descriptors")
	public SubmodelDescriptor putSubmodelDescriptorById(@RequestBody SubmodelDescriptor submodelDescriptor)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 식별자에 해당하는 등록 SubmodelDescriptor를 해제시킨다.
	 * 
	 * @param submodelId		해제시킬 SubmodelDescriptor의 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 SubmodelDescriptor가 존재하지 않는 경우.
	 */
	@DeleteExchange("/submodel-descriptors/{submodelId}")
	public void deleteSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException;
}
