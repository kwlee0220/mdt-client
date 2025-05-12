package mdt.model.sm.ref;

import com.google.common.base.Preconditions;


/**
 * MDT 연산에 사용되는 입/출력 인자의 종류를 정의하는 열거형이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum MDTArgumentKind {
	INPUT, OUTPUT;
	
	public static MDTArgumentKind fromString(String kindStr) {
		try {
			int ordinal = Integer.parseInt(kindStr);
			Preconditions.checkArgument(ordinal >= 0 && ordinal < 2,
										"OperationArgument's ordinal should be between 0 and 1, but {}", kindStr);
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