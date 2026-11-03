package mdt.aas;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

import mdt.model.AssetAdministrationShellService;

/**
 * AAS Repository에 저장된 {@link AssetAdministrationShell}들을 조회/등록/갱신/삭제하는
 * 인터페이스를 정의한다.
 * <p>
 * 조회 결과는 각 AAS에 접근할 수 있는 {@link AssetAdministrationShellService} 형태로 반환된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AssetAdministrationShellRepository {
	/**
	 * Repository에 저장된 모든 AAS들의 서비스 객체를 반환한다.
	 *
	 * @return	{@link AssetAdministrationShellService} 리스트.
	 */
	public List<AssetAdministrationShellService> getAllAssetAdministrationShells();
	/**
	 * 주어진 식별자에 해당하는 AAS의 서비스 객체를 반환한다.
	 *
	 * @param aasId	AAS 식별자.
	 * @return	{@link AssetAdministrationShellService} 객체.
	 */
	public AssetAdministrationShellService getAssetAdministrationShellById(String aasId);
	/**
	 * 주어진 자산 식별자를 갖는 AAS들의 서비스 객체를 반환한다.
	 *
	 * @param key	자산 식별자.
	 * @return	{@link AssetAdministrationShellService} 리스트.
	 */
	public List<AssetAdministrationShellService> getAssetAdministrationShellByAssetId(String key);
	/**
	 * 주어진 idShort를 갖는 AAS들의 서비스 객체를 반환한다.
	 *
	 * @param idShort	AAS의 idShort.
	 * @return	{@link AssetAdministrationShellService} 리스트.
	 */
	public List<AssetAdministrationShellService> getAssetAdministrationShellByIdShort(String idShort);
	
	/**
	 * 주어진 AAS를 Repository에 새로 등록한다.
	 *
	 * @param aas	등록할 AAS 객체.
	 * @return	등록된 AAS의 {@link AssetAdministrationShellService} 객체.
	 */
	public AssetAdministrationShellService postAssetAdministrationShell(AssetAdministrationShell aas);
	/**
	 * Repository에 저장된 AAS를 주어진 것으로 갱신한다.
	 * <p>
	 * 갱신 대상은 {@code aas}의 식별자로 결정된다.
	 *
	 * @param aas	갱신할 AAS 객체.
	 * @return	갱신된 AAS의 {@link AssetAdministrationShellService} 객체.
	 */
	public AssetAdministrationShellService putAssetAdministrationShellById(AssetAdministrationShell aas);
	/**
	 * 주어진 식별자에 해당하는 AAS를 Repository에서 삭제한다.
	 *
	 * @param id	삭제할 AAS 식별자.
	 */
	public void deleteAssetAdministrationShellById(String id);
}
