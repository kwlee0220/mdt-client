package mdt.cli.tree.ksx9101;

import java.util.Collections;
import java.util.Objects;

import org.barfuin.texttree.api.Node;

import utils.func.FOption;

import mdt.ksx9101.model.Parameter;
import mdt.ksx9101.model.ParameterValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ParameterNode implements Node {
	private Parameter m_param;
	private ParameterValue m_paramValue;
	
	public ParameterNode(Parameter prop, ParameterValue paramValue) {
		m_param = prop;
		m_paramValue = paramValue;
	}

	@Override
	public String getText() {
		String idStr = FOption.getOrElse(m_param.getParameterName(), m_param::getParameterId);
		String slStr = ( Objects.nonNull(m_param.getLSL()) || Objects.nonNull(m_param.getUSL()) )
					? String.format(", 공정범위: %s-%s", m_param.getLSL(), m_param.getUSL())
					: "";
		String paramValue = FOption.map(m_paramValue, ParameterValue::getParameterValue, "N/A");
		return String.format("%s (%s): %s%s", idStr, m_param.getParameterType(), paramValue, slStr);
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
