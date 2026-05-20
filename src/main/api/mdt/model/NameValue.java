package mdt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import utils.KeyValue;
import utils.Named;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class NameValue extends KeyValue<String,String> implements Named {
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
}