package mdt.workflow.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
public class ValueReferenceDescriptor {
	@JsonProperty("twinId") private String twinId;
	@JsonProperty("submodelIdShort") private String submodelIdShort;
	@JsonProperty("idShortPath") private String idShortPath;
	
	public static ValueReferenceDescriptor parseString(String refExpr) {
		String[] parts = refExpr.split("/");
		if ( parts.length != 3 ) {
			throw new IllegalArgumentException("invalid ValueReferenceDescriptor: " + refExpr);
		}
		return new ValueReferenceDescriptor(parts[0], parts[1], parts[2]);
	}
	
	public String toStringExpr() {
		return String.format("%s/%s/%s", this.twinId, this.submodelIdShort, this.idShortPath);
	}

	@Override
	public String toString() {
		return toStringExpr();
	}
}
