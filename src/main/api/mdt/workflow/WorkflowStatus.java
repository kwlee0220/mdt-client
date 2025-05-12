package mdt.workflow;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum WorkflowStatus {
	NOT_STARTED("N"),
	STARTING("S"),
	RUNNING("R"),
	COMPLETED("C"),
	FAILED("F"),
	UNKNOWN("?");
	
	private String m_shortName;
	
	WorkflowStatus(String shortName) {
		m_shortName = shortName;
	}
	
	public String getShortName() {
		return m_shortName;
	}
}
