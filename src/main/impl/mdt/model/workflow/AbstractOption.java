package mdt.model.workflow;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NoArgsConstructor;

import mdt.workflow.model.Option;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@NoArgsConstructor
@Getter
public abstract class AbstractOption implements Option {
	private String name;
	
	protected AbstractOption(String name) {
		Preconditions.checkArgument(name != null);
		
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		AbstractOption other = (AbstractOption)obj;
		return Objects.equal(name, other.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
}
