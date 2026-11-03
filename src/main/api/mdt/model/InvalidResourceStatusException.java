package mdt.model;

import mdt.model.instance.MDTInstanceStatus;

/**
 * 자원(예: MDTInstance)이 요청된 작업을 수행할 수 없는 상태에 있는 경우 발생되는 예외이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InvalidResourceStatusException extends ResourceException {
    private static final long serialVersionUID = 1L;

    /**
     * 주어진 메시지로 예외 객체를 생성한다.
     *
     * @param msg	예외 메시지.
     */
    public InvalidResourceStatusException(String msg) {
        super(msg);
    }

    /**
     * 대상 자원과 그 상태 정보로 예외 객체를 생성한다.
     * <p>
     * 예외 메시지는 {@code "<resourceType>(<resourceId>): <status>"} 형식으로 구성된다.
     *
     * @param resourceType	자원 종류 (예: {@code "MDTInstance"}).
     * @param resourceId	자원 식별자.
     * @param status	자원의 현재 상태.
     */
    public InvalidResourceStatusException(String resourceType, String resourceId, MDTInstanceStatus status) {
        super(String.format("%s(%s): %s", resourceType, resourceId, status));
    }
}
