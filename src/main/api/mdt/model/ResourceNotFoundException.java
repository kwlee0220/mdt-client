package mdt.model;

/**
 * 요청한 자원이 존재하지 않는 경우 발생되는 예외이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceNotFoundException extends ResourceException {
    private static final long serialVersionUID = 1L;

    /**
     * 주어진 semantic id에 해당하는 자원이 없음을 알리는 예외 객체를 생성한다.
     *
     * @param target	자원 종류 (예: {@code "Submodel"}).
     * @param semanticId	검색에 사용한 semantic id.
     * @return	생성된 예외 객체.
     */
    public static ResourceNotFoundException ofSemanticId(String target, String semanticId) {
        return new ResourceNotFoundException(target, "semanticId=" + semanticId);
    }
    
	/**
	 * 주어진 idShort에 해당하는 자원이 없음을 알리는 예외 객체를 생성한다.
	 *
	 * @param type	자원 종류.
	 * @param idShort	검색에 사용한 idShort.
	 * @return	생성된 예외 객체.
	 */
	public static ResourceNotFoundException ofIdShort(String type, String idShort) {
		return new ResourceNotFoundException(type, "idShort=" + idShort);
	}
    
	/**
	 * 주어진 식별자에 해당하는 자원이 없음을 알리는 예외 객체를 생성한다.
	 *
	 * @param type	자원 종류.
	 * @param id	검색에 사용한 식별자.
	 * @return	생성된 예외 객체.
	 */
	public static ResourceNotFoundException ofId(String type, String id) {
		return new ResourceNotFoundException(type, "id=" + id);
	}

    /**
     * 주어진 메시지로 예외 객체를 생성한다.
     *
     * @param msg	예외 메시지.
     */
    public ResourceNotFoundException(String msg) {
        super(msg);
    }

    /**
     * 대상 자원의 종류와 식별자로 예외 객체를 생성한다.
     * <p>
     * 예외 메시지는 {@code "<resourceType>(<resourceId>)"} 형식으로 구성된다.
     *
     * @param resourceType	자원 종류.
     * @param resourceId	자원 식별자 (또는 {@code "idShort=..."}와 같은 검색 조건).
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s(%s)", resourceType, resourceId));
    }
}
