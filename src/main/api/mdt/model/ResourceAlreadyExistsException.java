package mdt.model;

/**
 * 생성 또는 등록하려는 자원과 동일한 식별자의 자원이 이미 존재하는 경우 발생되는 예외이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceAlreadyExistsException extends ResourceException {
    private static final long serialVersionUID = 1L;

    /**
     * 주어진 메시지로 예외 객체를 생성한다.
     *
     * @param msg	예외 메시지.
     */
    public ResourceAlreadyExistsException(String msg) {
        super(msg);
    }

    /**
     * 대상 자원의 종류와 식별자로 예외 객체를 생성한다.
     * <p>
     * 예외 메시지는 {@code "<resourceType>(<resourceId>)"} 형식으로 구성된다.
     *
     * @param resourceType	자원 종류.
     * @param resourceId	자원 식별자.
     */
    public ResourceAlreadyExistsException(String resourceType, String resourceId) {
        super(String.format("%s(%s)", resourceType, resourceId));
    }
}
