package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;

import lombok.experimental.UtilityClass;

import utils.func.FOption;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class Qualifiers {
	public static final String QUALIFIER_OPERATION_METHOD = "http://mdt.etri.re.kr/quantifier/operation/method";
	public static final String QUALIFIER_OPERATION_SERVER_ENDPOINT
														= "http://mdt.etri.re.kr/quantifier/operation/endpoint";
	public static final String QUALIFIER_OPERATION_ID = "http://mdt.etri.re.kr/quantifier/operation/id";
	public static final String QUALIFIER_TIMEOUT = "http://mdt.etri.re.kr/quantifier/operation/timeout";
	public static final String QUALIFIER_POLL_INTERVAL = "http://mdt.etri.re.kr/quantifier/operation/pollInterval";
	public static final String QUALIFIER_UPDATE_OPVAR = "http://mdt.etri.re.kr/quantifier/operation/updateVariable";
	public static final String QUALIFIER_LAST_EXEC_TIME = "http://mdt.etri.re.kr/quantifier/operation/lastExecutionTime";
	
	public static FOption<String> findQualifierByType(List<Qualifier> qualifiers, String type) {
		return FStream.from(qualifiers)
						.findFirst(q -> type.equals(q.getType()))
						.map(Qualifier::getValue);
	}
}
