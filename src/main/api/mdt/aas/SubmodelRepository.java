package mdt.aas;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.SubmodelService;

/**
 * Submodel Repository에 저장된 {@link Submodel}들을 조회/등록/갱신/삭제하는 인터페이스를 정의한다.
 * <p>
 * 조회 결과는 각 Submodel에 접근할 수 있는 {@link SubmodelService} 형태로 반환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelRepository {
	/**
	 * Repository에 저장된 모든 Submodel들의 서비스 객체를 반환한다.
	 *
	 * @return	{@link SubmodelService} 리스트.
	 */
	public List<SubmodelService> getAllSubmodels();
	/**
	 * 주어진 식별자에 해당하는 Submodel의 서비스 객체를 반환한다.
	 *
	 * @param id	Submodel 식별자.
	 * @return	{@link SubmodelService} 객체.
	 */
	public SubmodelService getSubmodelById(String id);
	/**
	 * 주어진 semantic id를 갖는 Submodel들의 서비스 객체를 반환한다.
	 *
	 * @param semanticId	Submodel의 semantic id.
	 * @return	{@link SubmodelService} 리스트.
	 */
	public List<SubmodelService> getAllSubmodelBySemanticId(String semanticId);
	/**
	 * 주어진 idShort를 갖는 Submodel들의 서비스 객체를 반환한다.
	 *
	 * @param idShort	Submodel의 idShort.
	 * @return	{@link SubmodelService} 리스트.
	 */
	public List<SubmodelService> getAllSubmodelsByIdShort(String idShort);
	
	/**
	 * 주어진 Submodel을 Repository에 새로 등록한다.
	 *
	 * @param submodel	등록할 Submodel 객체.
	 * @return	등록된 Submodel의 {@link SubmodelService} 객체.
	 */
	public SubmodelService postSubmodel(Submodel submodel);
	/**
	 * Repository에 저장된 Submodel을 주어진 것으로 갱신한다.
	 * <p>
	 * 갱신 대상은 {@code submodel}의 식별자로 결정된다.
	 *
	 * @param submodel	갱신할 Submodel 객체.
	 * @return	갱신된 Submodel의 {@link SubmodelService} 객체.
	 */
	public SubmodelService putSubmodelById(Submodel submodel);
	/**
	 * 주어진 식별자에 해당하는 Submodel을 Repository에서 삭제한다.
	 *
	 * @param id	삭제할 Submodel 식별자.
	 */
	public void deleteSubmodelById(String id);
	
}
