package mdt.model.sm.value;

import java.time.Instant;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.func.FOption;

import mdt.aas.DataTypes;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ParameterValue extends ElementCollectionValue {
	public static final String SERIALIZATION_TYPE = "mdt:value:parameter";

	private ParameterValue(Map<String,ElementValue> elements) {
		super(elements);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	@SuppressWarnings("unchecked")
	public Instant getEventDateTime() {
		return findField("EventDateTime")
				.map(ev -> ((PropertyValue<Instant>)ev).get())
				.getOrNull();
	}
	
	public ElementValue getParameterValue() {
		return findField("ParameterValue").getOrNull();
	}
	
	@Override
	public String toString() {
		Instant ts = getEventDateTime();
		String tsStr = (ts != null) ? String.format(" (ts=%s)", DataTypes.DATE_TIME.toValueString(ts)) : "";

		ElementValue value = getParameterValue();
		if ( value == null ) {
			return String.format("(unknown): null %s", tsStr);
		}
		else if ( value instanceof PropertyValue prop ) {
			String valueStr = FOption.mapOrElse(prop, PropertyValue::toDisplayString, "null");
			return String.format("[Property] %s%s", valueStr, tsStr);
		}
		else if ( value instanceof org.eclipse.digitaltwin.aas4j.v3.model.File file ) {
			return String.format("[File] %s%s%s", file.getContentType(), file.getValue(), tsStr);
		}
		else if ( value instanceof SubmodelElementCollection ) {
			return String.format("[SMC] %s", tsStr);
		}
		else if ( value instanceof SubmodelElementList ) {
			return String.format("[SML] %s", tsStr);
		}
		else {
			return String.format("unknown");
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private final Map<String, ElementValue> m_fields;

		public Builder() {
			m_fields = new java.util.LinkedHashMap<>();
		}
		
		public Builder eventDateTime(Instant ts) {
			m_fields.put("EventDateTime", PropertyValue.DATE_TIME(ts));
			return this;
		}

		public Builder value(ElementValue value) {
			m_fields.put("ParameterValue", value);
			return this;
		}

		public ParameterValue build() {
			return new ParameterValue(m_fields);
		}
	}
}
