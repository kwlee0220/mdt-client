package mdt.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;

import com.fasterxml.jackson.databind.JsonNode;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface DataType<T> {
	public String getId();
	public DataTypeDefXsd getTypeDefXsd();
	public Class<T> getJavaClass();
	
	public default String getName() {
		return getTypeDefXsd().name();
	}
	
	public String toValueString(Object value);
	public T parseValueString(String str);
	
	public Object toJsonObject(T value);
	public T fromJsonNode(JsonNode jnode);
	
	public Object toJdbcObject(T value);
	public T fromJdbcObject(Object jdbcObj);
}
