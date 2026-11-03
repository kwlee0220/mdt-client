package mdt.aas;


/**
 * AAS 관련 서비스 endpoint의 인터페이스 종류를 정의한다.
 * <p>
 * 각 상수는 AAS descriptor의 endpoint {@code interface} 필드에 기록되는 이름
 * ({@link #getName()})을 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum EndpointInterface {
	/** AAS 서비스 인터페이스. */
	AAS("AAS"),
	/** Submodel 서비스 인터페이스. */
	SUBMODEL("SUBMODEL"),
	/** AAS 직렬화 서비스 인터페이스. */
	AAS_SERIALIZE("AAS-SERIALIZE"),
	/** AASX 파일 서비스 인터페이스. */
	AASX_FILE("AASX-FILE"),
	/** AAS Registry 서비스 인터페이스. */
	AAS_REGISTRY("AAS-REGISTRY"),
	/** Submodel Registry 서비스 인터페이스. */
	SUBMODEL_REGISTRY("SUBMODEL-REGISTRY"),
	/** AAS Repository 서비스 인터페이스. */
	AAS_REPOSITORY("AAS-REPOSITORY"),
	/** Submodel Repository 서비스 인터페이스. */
	SUBMODEL_REPOSITORY("SUBMODEL-REPOSITORY"),
	/** ConceptDescription Repository 서비스 인터페이스. */
	CD_REPOSITORY("CD-REPOSITORY"),
	/** AAS Discovery 서비스 인터페이스. */
	AAS_DISCOVERY("AAS-DISCOVERY");
	
	private final String m_name;
	
	EndpointInterface(String name) {
		m_name = name;
	}
	
	/**
	 * Endpoint descriptor에 기록되는 인터페이스 이름을 반환한다.
	 *
	 * @return	인터페이스 이름 (예: {@code "AAS-REGISTRY"}).
	 */
	public String getName() {
		return m_name;
	}
}