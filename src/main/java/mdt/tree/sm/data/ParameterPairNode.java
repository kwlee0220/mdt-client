package mdt.tree.sm.data;

import java.util.Collections;
import java.util.Objects;

import org.barfuin.texttree.api.Node;

import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.ElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ParameterPairNode implements Node {
	private int m_ordinal;
	private Parameter m_param;
	private ParameterValue m_paramValue;
	
	public ParameterPairNode(int ordinal, Parameter prop, ParameterValue paramValue) {
		m_ordinal = ordinal;
		m_param = prop;
		m_paramValue = paramValue;
	}

	@Override
	public String getText() {
//		String idStr = FOption.getOrElse(m_param.getParameterName(), m_param::getParameterId);
		String idStr = m_param.getParameterId();
		String nameStr = FOption.mapOrElse(m_param.getParameterName(), n -> String.format("%s, ", n), "");
		String slStr = ( Objects.nonNull(m_param.getLSL()) || Objects.nonNull(m_param.getUSL()) )
					? String.format(", 공정범위: %s-%s", m_param.getLSL(), m_param.getUSL())
					: "";
		String tsStr = m_paramValue.getEventDateTime();
		tsStr = FOption.mapOrElse(tsStr, t -> String.format(" (%s)", t), "");
		
		String paramValue = FOption.mapOrElse(m_paramValue, this::toParameterValueString, "N/A");
		return String.format("[#%02d] %s (%s%s): %s%s%s", m_ordinal,
								idStr, nameStr, m_param.getParameterType(), paramValue, slStr, tsStr);
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
	
	private String toParameterValueString(ParameterValue param) {
		ElementValue smev = ElementValues.getValue(param.getParameterValue());
		return MDTModelSerDe.toJsonString(smev);
	}
}
