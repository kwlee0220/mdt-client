package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTOperationHandle implements OperationHandle {
	private String m_idShortPathEncoded;
	private String m_location;
	private String m_handleId;
	
	public MDTOperationHandle(String idShortPathEncoded, String location) {
		m_idShortPathEncoded = idShortPathEncoded;
		m_location = location;
		
		String[] parts = location.split("/");
		m_handleId = parts[parts.length-1];
	}
	
	public String getIdShortPathEncoded() {
		return m_idShortPathEncoded;
	}
	
	public void setIdShortPathEncoded(String encoded) {
		m_idShortPathEncoded = encoded;
	}
	
	public String getStatusLocation() {
		return m_location;
	}

	@Override
	public String getHandleId() {
		return m_handleId;
	}

	@Override
	public void setHandleId(String handleId) {
		m_handleId = handleId;
	}

}
