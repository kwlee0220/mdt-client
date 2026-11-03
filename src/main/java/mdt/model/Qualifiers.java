package mdt.model;

import java.util.List;
import java.util.Optional;

import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;

import utils.stream.FStream;


/**
 * MDT에서 사용하는 AAS {@link Qualifier} 타입 상수들과 Qualifier 검색 유틸리티를 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class Qualifiers {
	private Qualifiers() {
		throw new AssertionError("Should not be called: class=" + Qualifiers.class.getName());
	}
	
	/** Operation 호출 방식을 지정하는 qualifier 타입. */
	public static final String QUALIFIER_OPERATION_METHOD = "http://mdt.etri.re.kr/quantifier/operation/method";
	/** Operation 서버 endpoint를 지정하는 qualifier 타입. */
	public static final String QUALIFIER_OPERATION_SERVER_ENDPOINT
														= "http://mdt.etri.re.kr/quantifier/operation/endpoint";
	/** Operation 식별자를 지정하는 qualifier 타입. */
	public static final String QUALIFIER_OPERATION_ID = "http://mdt.etri.re.kr/quantifier/operation/id";
	/** Operation 실행 제한 시간을 지정하는 qualifier 타입. */
	public static final String QUALIFIER_TIMEOUT = "http://mdt.etri.re.kr/quantifier/operation/timeout";
	/** Operation 실행 상태 polling 간격을 지정하는 qualifier 타입. */
	public static final String QUALIFIER_POLL_INTERVAL = "http://mdt.etri.re.kr/quantifier/operation/pollInterval";
	/** Operation 변수 갱신 여부를 지정하는 qualifier 타입. */
	public static final String QUALIFIER_UPDATE_OPVAR = "http://mdt.etri.re.kr/quantifier/operation/updateVariable";
	/** Operation 최종 실행 시각을 기록하는 qualifier 타입. */
	public static final String QUALIFIER_LAST_EXEC_TIME = "http://mdt.etri.re.kr/quantifier/operation/lastExecutionTime";
	
	/**
	 * 주어진 qualifier 리스트에서 타입이 일치하는 첫 번째 qualifier의 값을 반환한다.
	 *
	 * @param qualifiers	검색 대상 qualifier 리스트.
	 * @param type	검색할 qualifier 타입.
	 * @return	타입이 일치하는 qualifier의 값. 없는 경우는 {@link Optional#empty()}.
	 */
	public static Optional<String> findQualifierByType(List<Qualifier> qualifiers, String type) {
		return FStream.from(qualifiers)
						.findFirst(q -> type.equals(q.getType()))
						.map(Qualifier::getValue)
						.toOptional();
	}
}
