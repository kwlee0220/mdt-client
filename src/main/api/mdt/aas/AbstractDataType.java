package mdt.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.databind.JsonNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractDataType<T> implements DataType<T> {
	private final String m_id;
	private final DataTypeDefXsd m_xsdType;
	private final Class<T> m_javaClass;
	
	protected AbstractDataType(String id, DataTypeDefXsd xsdType, Class<T> javaClass) {
		m_id = id;
		m_xsdType = xsdType;
		m_javaClass = javaClass;
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public DataTypeDefXsd getTypeDefXsd() {
		return m_xsdType;
	}

	@Override
	public Class<T> getJavaClass() {
		return m_javaClass;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public Object toJsonObject(T value) {
		return toValueString(value);
	}

	@Override
	public T fromJsonNode(JsonNode jnode) {
		if ( jnode != null && !jnode.isNull() ) {
			return parseValueString(jnode.asText());
		}
		else {
			return null;
		}
	}

	@Override
	public Object toJdbcObject(T value) {
		return toValueString(value);
	}

	@Override
	public T fromJdbcObject(Object jdbcObj) {
		return (jdbcObj != null) ? parseValueString(jdbcObj.toString()) : null;
	}
}
