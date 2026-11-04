package mdt.aas;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;


/**
 * Submodel Registry에 등록된 {@link SubmodelDescriptor}들을 조회/등록/갱신/해제하는 인터페이스를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelRegistry {
	/**
	 * 등록된 모든 SubmodelDescriptor 들을 반환한다.
	 * 
	 * @return	SubmodelDescriptor 리스트.
	 */
	public List<SubmodelDescriptor> getAllSubmodelDescriptors();
	
	/**
	 * 주어진 식별자에 해당하는 {@link SubmodelDescriptor}를 반환한다.
	 * 
	 * @param submodelId	SubmodelDescriptor 식별자.
	 * @return	SubmodelDescriptor 객체
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 SubmodelDescriptor가 없는 경우
	 */
	public SubmodelDescriptor getSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException;

	/**
	 * 주어진 idShort를 갖는 모든 {@link SubmodelDescriptor}들을 반환한다.
	 *
	 * @param idShort	검색할 Submodel의 idShort.
	 * @return	idShort가 일치하는 SubmodelDescriptor 리스트.
	 */
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsByIdShort(String idShort);

	/**
	 * 주어진 semantic id를 갖는 모든 {@link SubmodelDescriptor}들을 반환한다.
	 *
	 * @param semanticId	검색할 Submodel의 semantic id.
	 * @return	semantic id가 일치하는 SubmodelDescriptor 리스트.
	 */
	public List<SubmodelDescriptor> getAllSubmodelDescriptorsBySemanticId(String semanticId);
	
	/**
	 * 주어진 SubmodelDescriptor를 등록시킨다.
	 * 
	 * @param submodelDescriptor	SubmodelDescriptor 객체.
	 * @return				등록된 SubmodelDescriptor 객체.
	 * @throws ResourceAlreadyExistsException	동일 식별자에 해당하는 SubmodelDescriptor가
	 * 									이미 존재하는 경우
	 */
	public SubmodelDescriptor postSubmodelDescriptor(SubmodelDescriptor submodelDescriptor)
		throws ResourceAlreadyExistsException;
	
	/**
	 * 기존에 등록된  SubmodelDescriptor를 주어진 것으로 갱신시킨다.
	 * 
	 * @param submodelDescriptor	변경시킬 SubmodelDescriptor 객체.
	 * @return	갱신된 SubmodelDescriptor 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 등록 SubmodelDescriptor가 없는 경우
	 */
	public SubmodelDescriptor putSubmodelDescriptor(SubmodelDescriptor submodelDescriptor)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 식별자에 해당하는 등록 SubmodelDescriptor를 해제시킨다.
	 * 
	 * @param submodelId		해제시킬 SubmodelDescriptor의 식별자.
	 * @throws ResourceNotFoundException	식별자에 해당하는 SubmodelDescriptor가 존재하지 않는 경우.
	 */
	public void deleteSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException;
}
