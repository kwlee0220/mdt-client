package mdt.client.operation;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import lombok.experimental.UtilityClass;

import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class OperationUtils {
	public static Variable toTaskPort(OperationVariable opv) {
		SubmodelElement sme = opv.getValue();
		return Variables.newInstance(sme.getIdShort(), "", sme);
	}
	
	public static String unquote(String str) {
		if (str == null) return null;
		str = str.trim();
		if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length() - 1);
		}
		return str;
	}
}
