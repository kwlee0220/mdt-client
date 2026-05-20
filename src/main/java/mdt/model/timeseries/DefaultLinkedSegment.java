package mdt.model.timeseries;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultLinkedSegment extends DefaultSegment implements LinkedSegment {
	@PropertyField(idShort="Endpoint") private String endpoint;
	@PropertyField(idShort="Query") private String query;
	
	public DefaultLinkedSegment() {
		setSemanticId(LinkedSegment.SEMANTIC_ID);
	}
	
	@Override
	public String toString() {
		return "LinkedSegment[id=" + getIdShort() + ", endpoint=" + getEndpoint() + ", query=" + getQuery() + "]";
	}
}
