package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

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
			service.updateSubmodelElementByPath(getIdShortPathString(), valueJsonString);
		}
		catch ( RESTfulRemoteException e ) {
			// 현재 FAST의 구현에 버그가 있어 예외가 발생하기도 해서,
			// 이러한 경우 로컬 업데이트로 처리하도록 함.
			String msg = e.getMessage();
			if ( msg != null && msg.contains("no type information found") ) {
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
