package mdt.model.sm.ref;

import utils.Preconditions;

/**
 * MDT 연산에 사용되는 입/출력 인자의 종류를 정의하는 열거형이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum MDTArgumentKind {
	/** 입력 인자. */
	INPUT,
	/** 출력 인자. */
	OUTPUT;

	/**
	 * 문자열을 {@link MDTArgumentKind}로 변환한다.
	 * <p>
	 * 순번(0=INPUT, 1=OUTPUT) 또는 {@code "in"}/{@code "out"}으로 시작하는 이름을 허용하며,
	 * {@code "*"}는 종류 미지정을 의미하는 {@code null}로 변환된다.
	 *
	 * @param kindStr	변환할 문자열.
	 * @return 대응하는 {@link MDTArgumentKind}. {@code "*"}인 경우 {@code null}.
	 * @throws IllegalArgumentException	인식할 수 없는 문자열인 경우.
	 */
	public static MDTArgumentKind fromString(String kindStr) {
		try {
			int ordinal = Integer.parseInt(kindStr);
			Preconditions.checkArgument(ordinal >= 0 && ordinal < 2,
										"OperationArgument's ordinal should be between 0 and 1, but %s", kindStr);
			return MDTArgumentKind.values()[ordinal];
		}
		catch ( NumberFormatException expected ) {
			kindStr = kindStr.trim().toLowerCase();
			if ( kindStr.startsWith("in") ) {
				return INPUT;
			}
			else if ( kindStr.startsWith("out") ) {
                return OUTPUT;
            }
			else if ( kindStr.equals("*") ) {
				return null;
			}
			else {
				throw new IllegalArgumentException("Invalid OperationArgument's kind: " + kindStr);
			}
		}
	}
}