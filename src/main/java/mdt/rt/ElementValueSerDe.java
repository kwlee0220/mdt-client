package mdt.rt;

import java.io.IOException;

import mdt.model.sm.value.ElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ElementValueSerDe<T> {
	public ElementValue serializeToElementValue(T elem) throws IOException;
	public T deserializeFromElementValue(ElementValue elem) throws IOException;
}
