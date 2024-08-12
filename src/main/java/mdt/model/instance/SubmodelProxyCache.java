package mdt.model.instance;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmodelProxyCache {
	private String id;
	private String idShort;
	
	@Override
	public String toString() {
		return String.format("%s (%s)", this.id, this.idShort);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		SubmodelProxyCache other = (SubmodelProxyCache)obj;
		return Objects.equals(this.id, other.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}
}