package mdt.client;


/**
 * FA³ST 서버 응답 메시지의 종류.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum Fa3stMessageType {
    /** 정보 메시지. */
    INFO,
    /** 경고 메시지. */
    WARNING,
    /** 오류 메시지. */
    ERROR,
    /** 예외 메시지. */
    EXCEPTION
}
