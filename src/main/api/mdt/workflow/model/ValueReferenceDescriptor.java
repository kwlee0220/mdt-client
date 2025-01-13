package mdt.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
public class ValueReferenceDescriptor {
	@JsonProperty("twinId") private String m_twinId;
	@JsonProperty("submodelIdShort") private String m_submodelIdShort;
	@JsonProperty("idShortPath") private String m_idShortPath;
	
	public static ValueReferenceDescriptor parseString(String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException("invalid ValueReferenceDescriptor: " + refExpr);
		}
		return new ValueReferenceDescriptor(parts[0], parts[1], parts[2]);
	}

	public String getTwinId() {
		return m_twinId;
	}

	public void setTwinId(String twinId) {
		this.m_twinId = twinId;
	}

	public String getSubmodelIdShort() {
		return m_submodelIdShort;
	}

	public void setSubmodelIdShort(String submodelIdShort) {
		this.m_submodelIdShort = submodelIdShort;
	}

	public String getIdShortPath() {
		return m_idShortPath;
	}

	public void setIdShortPath(String idShortPath) {
		this.m_idShortPath = idShortPath;
	}
	
	public String toStringExpr() {
		return String.format("%s/%s/%s", m_twinId, m_submodelIdShort, m_idShortPath);
	}

	@Override
	public String toString() {
		return toStringExpr();
	}
}
