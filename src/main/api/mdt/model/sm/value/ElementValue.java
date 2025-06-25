package mdt.model.sm.value;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * ElementValue는 AAS {@link SubmodelElement}에서 값에 해당하는 부분을 표현하는 인터페이스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ElementValue {
	// public static ElementValue parseJsonString(String json);	// ElementValues에서 정의됨.
	public String toJsonString() throws IOException;
	
	// public ElementValue parseValueJsonString(String json) throws IOException;	// ElementValues에서 정의됨.
	public String toValueJsonString();
	
	public String toValueString();
}
