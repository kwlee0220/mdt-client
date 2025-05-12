package mdt.workflow.model;

import com.google.common.base.Preconditions;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractOption<T> implements Option<T> {
	private final String m_name;
	private final T m_value;

	public AbstractOption(String name, T value) {
		Preconditions.checkArgument(name != null, "name is null");
		
		m_name = name;
		m_value = value;
	}

	@Override
	public String getName() {
		return m_name;
	}
	
	@Override
	public T getValue() {
		return m_value;
	}
	
	@Override
	public String toString() {
		return String.format("Option[%s]: %s", getName(), getValue());
	}
}
