package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;


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
}
