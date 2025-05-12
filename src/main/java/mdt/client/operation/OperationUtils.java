package mdt.client.operation;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.UtilityClass;

import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.variable.Variable;
import mdt.model.sm.variable.Variables;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class OperationUtils {
	private static final Logger s_logger = LoggerFactory.getLogger(OperationUtils.class);
	
	public static Variable toTaskPort(OperationVariable opv) {
		SubmodelElement sme = opv.getValue();
		return Variables.newInstance(sme.getIdShort(), "", sme);
	}
	
	public static String toExternalString(SubmodelElement sme) throws IOException {
		ElementValue smev = ElementValues.getValue(sme);
		if ( smev != null ) {
			return ( smev instanceof PropertyValue propv )
					? FOption.getOrElse(propv.get(), "")
					: MDTModelSerDe.toJsonString(smev);
		}
		else {
			return null;
		}
	}
}
