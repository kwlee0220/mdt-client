package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.ObjectMapper;

import utils.func.Lazy;
import utils.http.RESTfulRemoteException;

import mdt.model.SubmodelService;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.IdShortPath;


/**
 * Submodel에 포함된 SubmodelElement를 참조하는 ElementReference의 기본 클래스.
 * <p>
 * SubmodelBasedElementReference는 대상 SubmodelElement를 참조하기 위해 SubmodelService 객체와
 * SubmodelElement의 idShort path를 사용한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class SubmodelBasedElementReference extends AbstractElementReference
													implements MDTElementReference {
	private final Lazy<IdShortPath> m_idShortPath = Lazy.of(this::parseIdShortPath);
	private final ObjectMapper MAPPER = new ObjectMapper();
	
	abstract public MDTSubmodelReference getSubmodelReference();
	
	public IdShortPath getIdShortPath() {
		return m_idShortPath.get();
	}
	
	@Override
	public SubmodelElement read() throws IOException {
		return getSubmodelService().getSubmodelElementByPath(getIdShortPathString());
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		updateValue(smev.toValueJsonString());
	}
	
	@Override
	public void updateValue(String valueJsonString) throws IOException {
		SubmodelService service = getSubmodelService();
		try {
//			JsonNode jnode = MAPPER.readTree(valueJsonString);
//			if ( jnode.isObject() || jnode.isArray() ) {
//				String encoded = MAPPER.writeValueAsString(valueJsonString);
//				service.updateSubmodelElementByPath(getIdShortPathString(), encoded);
//			}
//			else {
				service.updateSubmodelElementByPath(getIdShortPathString(), valueJsonString);
//			}
		}
		catch ( RESTfulRemoteException e ) {
			String msg = e.getMessage();
			if ( msg != null && msg.startsWith("no type information found") ) {
				getLogger().warn("failed to update the value by path=" + getIdShortPathString()
									+ ", try to update it locally: valueJsonString=" + valueJsonString);
				updateValueLocally(valueJsonString);
			}
			else {
				throw e;
			}
		}
	}
	private void updateValueLocally(String valueJsonString) throws IOException {
		SubmodelElement buffer = read();
		ElementValues.updateWithValueJsonString(buffer, valueJsonString);
		write(buffer);
	}

	@Override
	public void write(SubmodelElement sme) throws IOException {
		getSubmodelService().setSubmodelElementByPath(getIdShortPathString(), sme);
	}
	
	private IdShortPath parseIdShortPath() {
		return IdShortPath.fromString(getIdShortPathString());
	}
}
