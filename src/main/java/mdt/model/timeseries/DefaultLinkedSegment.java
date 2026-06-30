package mdt.model.timeseries;

import mdt.model.sm.entity.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultLinkedSegment extends DefaultSegment implements LinkedSegment {
	@PropertyField(idShort="Endpoint") private String endpoint;
	@PropertyField(idShort="Query") private String query;
	
	public DefaultLinkedSegment() {
		setSemanticId(LinkedSegment.SEMANTIC_ID);
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	public String toString() {
		return "LinkedSegment[id=" + getIdShort() + ", endpoint=" + getEndpoint() + ", query=" + getQuery() + "]";
	}
}
