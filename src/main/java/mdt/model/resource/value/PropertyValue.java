package mdt.model.resource.value;

import java.util.Objects;
import java.util.function.Supplier;

import mdt.model.DataType;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class PropertyValue<T> implements SubmodelElementValue, Supplier<T> {
	private final DataType<T> m_type;
	private T m_value;
	
	abstract public void setString(String str);

	protected PropertyValue(DataType<T> type) {
		this.m_type = type;
		this.m_value = null;
	}

	protected PropertyValue(DataType<T> type, T value) {
		this.m_type = type;
		this.m_value = value;
	}

	@Override
	public T get() {
		return m_value;
	}
	
	public void set(T value) {
		m_value = value;
	}

	@Override
	public Object toJsonObject() {
		return  m_type.toValueString(m_value);
	}
	
	@Override
	public String toString() {
		return String.format("(%s) %s", m_type.getName(), m_value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
			return false;
		}
		
		@SuppressWarnings("rawtypes")
		PropertyValue other = (PropertyValue)obj;
		return Objects.equals(m_value, other.m_value)
				&& Objects.equals(m_type, other.m_type);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_type, m_value);
	}
}
