package mdt.task;

import java.util.Arrays;

import utils.func.Funcs;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum PortType {
	INPUT_SME("insme."),
	INPUT_VALUE("inval."),
	OUTPUT_SME("outsme."),
	OUTPUT_VALUE("outval."),
	INOUT_SME("inoutsme."),
	INOUT_VALUE("inoutval.");
	
	private String m_argNamePrefix;
	
	private PortType(String argNamePrefix) {
		m_argNamePrefix = argNamePrefix;
	}
	
	public String getArgNamePrefix() {
		return m_argNamePrefix;
	}
	
	public static PortType fromArgName(String argName) {
		return Funcs.findFirst(Arrays.asList(PortType.values()),
								t -> argName.startsWith(t.getArgNamePrefix()))
					.getOrNull();
	}
}
