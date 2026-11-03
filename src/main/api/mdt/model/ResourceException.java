package mdt.model;

/**
 * MDT 자원(MDTInstance, AAS, Submodel 등)과 관련된 예외들의 기반 클래스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceException extends MDTException {
    private static final long serialVersionUID = 1L;

    /**
     * 주어진 메시지로 예외 객체를 생성한다.
     *
     * @param msg	예외 메시지.
     */
    public ResourceException(String msg) {
        super(msg);
    }
}
