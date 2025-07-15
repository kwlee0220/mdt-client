package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.func.Lazy;

import mdt.model.SubmodelService;
import mdt.model.sm.value.ElementValue;
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
		SubmodelService service = getSubmodelService();
		service.updateSubmodelElementValueByPath(getIdShortPathString(), smev);
	}

	@Override
	public void write(SubmodelElement sme) throws IOException {
		getSubmodelService().setSubmodelElementByPath(getIdShortPathString(), sme);
	}
	
	private IdShortPath parseIdShortPath() {
		return IdShortPath.fromString(getIdShortPathString());
	}
}
