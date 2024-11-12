package mdt.tree.sm.data;

import java.util.Collections;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.data.DefaultEquipmentParameterValue;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.value.ElementValues;
import mdt.tree.CustomNodeTransform;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ParameterValueNode implements Node {
	private String m_prefix;
	private ParameterValue m_paramValue;
	
	public static class Transform implements CustomNodeTransform {
		@Override
		public Node toNode(String prefix, SubmodelElement sme) {
			DefaultEquipmentParameterValue pv = new DefaultEquipmentParameterValue();
			pv.updateFromAasModel(sme);
			return new ParameterValueNode(prefix, pv);
		}
	}
	
	public ParameterValueNode(String prefix, ParameterValue paramValue) {
		m_prefix = prefix;
		m_paramValue = paramValue;
	}

	@Override
	public String getText() {
		String idStr = m_paramValue.getParameterId();
		SubmodelElement valueSme = m_paramValue.getParameterValue();
		String valueStr = MDTModelSerDe.toJsonString(ElementValues.getValue(valueSme));
		String tsStr = m_paramValue.getEventDateTime();
		tsStr = FOption.mapOrElse(tsStr, t -> String.format(" (%s)", t), "");
		
		if ( valueSme instanceof Property prop ) {
			valueStr = FOption.getOrElse(valueStr, "N/A");
			return String.format("%s%s (%s): %s%s", m_prefix, idStr, prop.getValueType(), valueStr, tsStr);
		}
		else if ( valueSme instanceof org.eclipse.digitaltwin.aas4j.v3.model.File file ) {
			return String.format("%s%s (%s): %s%s", m_prefix, idStr, file.getContentType(), file.getValue(), tsStr);
		}
		else if ( valueSme instanceof SubmodelElementCollection ) {
			return String.format("%s%s (SMC):%s", m_prefix, idStr, tsStr);
		}
		else if ( valueSme instanceof SubmodelElementList ) {
			return String.format("%s%s (SML):%s", m_prefix, idStr, tsStr);
		}
		else {
			return m_prefix + "unknown";
		}
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
