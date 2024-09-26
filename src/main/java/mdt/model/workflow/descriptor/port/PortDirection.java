package mdt.model.workflow.descriptor.port;

import java.util.Arrays;

import utils.func.Funcs;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum PortDirection {
	INPUT("in"),
	OUTPUT("out");
	
	private String m_typeStr;
	
	private PortDirection(String typeStr) {
		m_typeStr = typeStr;
	}
	
	public String getTypeString() {
		return m_typeStr;
	}
	
	public static PortDirection fromArgName(String argName) {
		return Funcs.findFirst(Arrays.asList(PortDirection.values()),
								t -> argName.startsWith(t.getTypeString() + "."))
					.getOrNull();
	}
}
