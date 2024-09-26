package mdt.task;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.LoggerSettable;
import utils.func.FOption;

import mdt.model.workflow.descriptor.port.PortDirection;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractPort implements Port, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(Port.class);
	
	protected final String m_name;
	protected final PortDirection m_direction;
	protected final boolean m_valueOnly;
	private Logger m_logger;
	
	protected AbstractPort(String name, PortDirection type, boolean valueOnly) {
		m_name = name;
		m_direction = type;
		m_valueOnly = valueOnly;
		
		setLogger(s_logger);
	}

	@Override
	public String getName() {
		return m_name;
	}
	
	@Override
	public PortDirection getDirection() {
		return m_direction;
	}

	@Override
	public boolean isValuePort() {
		return m_valueOnly;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
	
	@Override
	public String toString() {
		return String.format("[%s(%s)]", m_name, m_direction.getTypeString());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}
		
		AbstractPort other = (AbstractPort)obj;
		return Objects.equals(m_name, other.m_name)
			&& Objects.equals(m_direction, other.m_direction);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_name, m_direction);
	}
}