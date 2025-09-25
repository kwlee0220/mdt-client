package mdt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import utils.KeyValue;
import utils.Keyed;
import utils.Named;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class NameValue extends KeyValue<String,String> implements Named, Keyed<String> {
	public static NameValue of(String name, String value) {
		return new NameValue(name, value);
	}

	@JsonCreator
	public NameValue(@JsonProperty("name") String name,
					@JsonProperty("value") String value) {
		super(name, value);
	}
	
	@Override
	public String getName() {
		return key();
	}

	public String getValue() {
		return value();
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		NameValue other = (NameValue)obj;
		return Objects.equal(getName(), other.getValue());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getName());
	}
}