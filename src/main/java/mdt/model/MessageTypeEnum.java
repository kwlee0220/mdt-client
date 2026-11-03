package mdt.model;


/**
 * FA³ST 등 AAS 서버의 오류/결과 메시지({@code Message})의 종류를 정의한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum MessageTypeEnum {
	/** 정보 메시지. */
	Info,
	/** 경고 메시지. */
	Warning,
	/** 오류 메시지. */
	Error,
	/** 예외 발생 메시지. */
	Exception
}
