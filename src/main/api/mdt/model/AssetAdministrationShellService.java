package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Resource;


/**
 * 하나의 AssetAdministrationShell(AAS)에 대한 조회/갱신 서비스를 정의하는 인터페이스이다.
 * <p>
 * AAS 자체와 그에 포함된 서브모델 참조, 자산 정보, thumbnail에 대한 접근 메소드를 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AssetAdministrationShellService {
	/**
	 * 본 서비스에 해당하는 {@link AssetAdministrationShell} 객체를 반환한다.
	 *
	 * @return	AssetAdministrationShell 객체.
	 */
	public AssetAdministrationShell getAssetAdministrationShell();
	
	/**
	 * 주어진 {@link AssetAdministrationShell} 객체를 서비스를 통해 설정한다.
	 *
	 * @param aas 설정할 AssetAdministrationShell 객체.
	 * @return	설정된 AssetAdministrationShell 객체.
	 */
	public AssetAdministrationShell putAssetAdministrationShell(AssetAdministrationShell aas);

	/**
	 * AssetAdministrationShell 객체에 포함된 모든 서브모델들의 참조 목록을 반환한다.
	 *
	 * @return 서브모델 참조 목록.
	 */
	public List<Reference> getAllSubmodelReferences();
	
	/**
	 * 주어진 서브모델 {@link Reference}를 AssetAdministrationShell 객체에 추가한다.
	 *
	 * @param ref 추가할 서브모델 {@link Reference} 객체.
	 * @return 추가된 서브모델 {@link Reference} 객체.
	 */
	public Reference postSubmodelReference(Reference ref);
	
	/**
	 * 주어진 Submodel 식별자에 해당하는 SubmodelReference를 삭제한다.
	 *
	 * @param submodelId	삭제할 서브모델 식별자.
	 */
	public void deleteSubmodelReference(String submodelId);
	
	/**
	 * {@link AssetAdministrationShell} 객체에 포함된 {@link AssetInformation} 객체를 반환한다.
	 *
	 * @return	AssetInformation 객체.
	 */
	public AssetInformation getAssetInformation();
	
	/**
     * 주어진 {@link AssetInformation} 객체를 서비스를 통해 {@link AssetAdministrationShell}에 설정한다.
     *
     * @param assetInfo 설정할 AssetInformation 객체.
     * @return 설정된 AssetInformation 객체
     */
	public AssetInformation putAssetInformation(AssetInformation assetInfo);
	
	/**
	 * AssetAdministrationShell 객체에 포함된 thumbnail의 {@link Resource} 객체를 반환한다.
	 *
	 * @return	thumbnail Resource
	 */
	public Resource getThumbnail();
	
	/**
	 * AssetAdministrationShell 객체에 포함된 thumbnail의 {@link Resource} 객체를 설정한다.
	 *
	 * @param thumbnail	설정할 thumbnail
	 * @return	설정된 thumbnail
	 */
	public Resource putThumbnail(Resource thumbnail);
	
	/**
	 * AssetAdministrationShell 객체에 포함된 thumbnail을 삭제한다.
	 */
	public void deleteThumbnail();
}
