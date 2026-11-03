package mdt.model;

import mdt.aas.ShellRegistry;
import mdt.aas.SubmodelRegistry;
import mdt.aas.SubmodelRepository;


/**
 * 주어진 endpoint에 접속하는 AAS 관련 서비스(Registry, Repository, AAS/Submodel 서비스)의
 * 클라이언트 객체를 생성하는 팩토리 인터페이스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ServiceFactory {
	/**
	 * 주어진 endpoint에 접속하는 AAS Registry({@link ShellRegistry}) 객체를 반환한다.
	 *
	 * @param endpoint	서비스 endpoint.
	 * @return	서비스 객체.
	 */
	public ShellRegistry getAssetAdministrationShellRegistry(String endpoint);
	/**
	 * 주어진 endpoint에 접속하는 Submodel Registry({@link SubmodelRegistry}) 객체를 반환한다.
	 *
	 * @param endpoint	서비스 endpoint.
	 * @return	서비스 객체.
	 */
	public SubmodelRegistry getSubmodelRegistry(String endpoint);

	/**
	 * 주어진 endpoint에 접속하는 Submodel Repository({@link SubmodelRepository}) 객체를 반환한다.
	 *
	 * @param endpoint	서비스 endpoint.
	 * @return	서비스 객체.
	 */
	public SubmodelRepository getSubmodelRepository(String endpoint);

	/**
	 * 주어진 endpoint에 접속하는 AAS 서비스({@link AssetAdministrationShellService}) 객체를 반환한다.
	 *
	 * @param endpoint	서비스 endpoint.
	 * @return	서비스 객체.
	 */
	public AssetAdministrationShellService getAssetAdministrationShellService(String endpoint);
	/**
	 * 주어진 endpoint에 접속하는 Submodel 서비스({@link SubmodelService}) 객체를 반환한다.
	 *
	 * @param endpoint	서비스 endpoint.
	 * @return	서비스 객체.
	 */
	public SubmodelService getSubmodelService(String endpoint);
}
