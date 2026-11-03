package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;


/**
 * 비동기로 호출된 AAS Operation의 실행 핸들을 표현하는 {@link OperationHandle} 구현이다.
 * <p>
 * 대상 Operation의 idShortPath(원본 및 URL 인코딩된 형태)와, 비동기 호출 응답으로 받은
 * 실행 상태 조회 위치(location)를 보관한다. 핸들 식별자는 location의 마지막 경로 구성요소이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTOperationHandle implements OperationHandle {
	private String m_idShortPath;
	private String m_idShortPathEncoded;
	private String m_location;
	private String m_handleId;
	
	/**
	 * 주어진 Operation 경로와 상태 조회 위치로 핸들 객체를 생성한다.
	 * <p>
	 * 핸들 식별자({@link #getHandleId()})는 {@code location}을 {@code '/'}로 분리한 마지막 구성요소로
	 * 설정된다.
	 *
	 * @param idShortPath	대상 Operation SubmodelElement의 idShortPath.
	 * @param idShortPathEncoded	URL에 사용하기 위해 인코딩된 idShortPath.
	 * @param location	비동기 호출 응답으로 받은 실행 상태 조회 위치.
	 */
	public MDTOperationHandle(String idShortPath, String idShortPathEncoded, String location) {
		m_idShortPath = idShortPath;
		m_idShortPathEncoded = idShortPathEncoded;
		m_location = location;
		
		String[] parts = location.split("/");
		m_handleId = parts[parts.length-1];
	}
	
	/**
	 * 대상 Operation SubmodelElement의 idShortPath를 반환한다.
	 *
	 * @return	idShortPath.
	 */
	public String getIdShortPath() {
		return m_idShortPath;
	}
	
	/**
	 * URL에 사용하기 위해 인코딩된 idShortPath를 반환한다.
	 *
	 * @return	인코딩된 idShortPath.
	 */
	public String getIdShortPathEncoded() {
		return m_idShortPathEncoded;
	}
	
	/**
	 * 인코딩된 idShortPath를 설정한다.
	 *
	 * @param encoded	인코딩된 idShortPath.
	 */
	public void setIdShortPathEncoded(String encoded) {
		m_idShortPathEncoded = encoded;
	}
	
	/**
	 * Operation 실행 상태 조회 위치를 반환한다.
	 *
	 * @return	실행 상태 조회 위치 (비동기 호출 응답의 location).
	 */
	public String getStatusLocation() {
		return m_location;
	}

	@Override
	public String getHandleId() {
		return m_handleId;
	}

	@Override
	public void setHandleId(String handleId) {
		m_handleId = handleId;
	}

}
