package mdt.model.sm.ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

import utils.Preconditions;
import utils.func.Lazy;
import utils.http.RESTfulRemoteException;

import mdt.model.MDTModelSerDe;
import mdt.model.SubmodelService;
import mdt.model.SubmodelService.Modifier;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;


/**
 * Submodel에 포함된 SubmodelElement를 참조하는 {@link MDTElementReference}의 기본 클래스.
 * <p>
 * SubmodelBasedElementReference는 대상 SubmodelElement를 참조하기 위해, 생성자로 전달받은
 * {@link MDTSubmodelReference}(참조 대상이 속한 Submodel)와 대상 SubmodelElement의
 * idShort path 문자열을 사용한다. 모든 읽기/쓰기 연산은 {@code MDTSubmodelReference}가 제공하는
 * {@link SubmodelService}를 통해 수행된다.
 * <p>
 * {@link #activate(MDTInstanceManager)} 호출 시점에 Submodel 참조가 활성화되고, 하위 클래스가
 * 제공하는 {@link #buildIdShortPath()}를 1번 호출하여 대상 SubmodelElement의 idShort path를 확정한다.
 * <p>
 * 이 클래스는 {@link #read()}, {@link #readValue()}, {@link #updateValue(ElementValue)},
 * {@link #write(SubmodelElement)}, 첨부파일 접근 등 SubmodelElement에 대한 접근 로직과
 * {@link #getInstance()}/{@link #getInstanceId()}, {@link #getSubmodelReference()},
 * {@link #getSubmodelService()}, {@link #getIdShortPathString()} 등의 정보 조회 메소드를
 * 모두 기본 구현으로 제공한다. 따라서 이 클래스를 상속하는 하위 클래스가 반드시 구현해야 하는
 * 메소드는 다음과 같다.
 * <ul>
 *   <li>{@link #buildIdShortPath()}: 참조 활성화 시점에 호출되어 대상 SubmodelElement의
 *       idShort path 문자열을 생성한다.</li>
 *   <li>{@link #toStringExpr()}: 이 참조를 사람이 읽을 수 있는 문자열로 표현한다.
 *       ({@code ElementReference}로부터 상속됨, {@link #toString()}에서 사용된다.)</li>
 * </ul>
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class SubmodelBasedElementReference extends AbstractElementReference
													implements MDTElementReference {
	private final MDTSubmodelReference m_submodelRef;	// 대상 Submodel 참조
	private String m_idShortPath;						// 대상 SubmodelElement의 idShort path 문자열
														// (활성화 시점에 초기화됨)
	
	private final Lazy<SubmodelElement> m_prototype = Lazy.of(this::loadPrototype);
	
	/**
	 * Submodel 내 대상 SubmodelElement의 idShort path를 생성한다.
	 * <p>
	 * 이 메소드는 참조가 활성화되는 시점에 1번 호출된다.
	 *
	 * @return	 대상 SubmodelElement의 idShort path.
	 */
	@NotNull abstract protected String buildIdShortPath();
	
	protected SubmodelBasedElementReference(MDTSubmodelReference submodelRef) {
		Preconditions.checkNotNullArgument(submodelRef, "submodelRef must not be null");
		
		m_submodelRef = submodelRef;
	}

	@Override
	public void activate(MDTInstanceManager manager) {
		Preconditions.checkNotNullArgument(manager, "manager must not be null");

		m_submodelRef.activate(manager);
		m_idShortPath = buildIdShortPath();
		if ( m_idShortPath == null ) {
			throw new IllegalStateException("buildIdShortPath() returned null");
		}
	}

	@Override
	public boolean isActivated() {
		return m_submodelRef.isActivated() && m_idShortPath != null;
	}

	@Override
	public String getInstanceId() {
		return m_submodelRef.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() {
		return m_submodelRef.getInstance();
	}

	/**
	 * 참조 대상 SubmodelElement가 속한 Submodel에 대한 {@link MDTSubmodelReference}를 반환한다.
	 *
	 * @return	대상 Submodel 참조.
	 */
	public MDTSubmodelReference getSubmodelReference() {
		return m_submodelRef;
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_submodelRef.get();
	}

	@Override
	public String getIdShortPathString() {
		Preconditions.checkState(m_idShortPath != null, "not activated");
		
		return m_idShortPath;
	}

	@Override
	public SubmodelElement getPrototype() {
		return m_prototype.get();
	}
	
	@Override
	public SubmodelElement read() throws IOException {
		SubmodelElement sme = getSubmodelService().getSubmodelElementByPath(getIdShortPathString());
		
		// (불필요한 prototype 로딩을 방지하기 위해) prototype이 아직 로드되지 않은 상태라면,
		// 읽어온 SubmodelElement를 prototype으로 설정한다.
		if ( !m_prototype.isLoaded() ) {
			m_prototype.set(sme);
		}
		
		return sme;
	}
	
	public SubmodelElement read(Modifier modifier) throws IOException {
		return getSubmodelService().getSubmodelElementByPath(getIdShortPathString(), modifier);
	}

	@Override
	public ElementValue readValue() throws IOException {
		return getSubmodelService().getSubmodelElementValueByPath(getIdShortPathString(), m_prototype.get());
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		updateValue(smev.toValueJsonString());
	}
	
	@Override
	public void updateValue(String valueJsonString) throws IOException {
		SubmodelService service = getSubmodelService();
		try {
			service.updateSubmodelElementValueByPath(getIdShortPathString(), valueJsonString);
		}
		catch ( RESTfulRemoteException e ) {
			// 현재 FAST의 구현에 버그가 있어 예외가 발생하기도 해서,
			// 이러한 경우 로컬 업데이트로 처리하도록 함.
			// TODO: FAST 서버의 버그가 수정되면 이 우회 코드를 제거할 것.
			//       에러 메시지 문자열("no type information found")에 의존하므로
			//       서버 측 문구가 변경되면 우회가 동작하지 않을 수 있음.
			String msg = e.getMessage();
			if ( msg != null && msg.contains("no type information found") ) {
				getLogger().warn("failed to update the value by path=" + getIdShortPathString()
									+ ", try to update it locally: valueJsonString=" + valueJsonString);
				// 일단 SubmodelElement 전체를 읽어와서 JSON 문자열로 업데이트한 뒤 다시 쓰는 방식으로 처리한다.
				updateValueLocally(valueJsonString);
			}
			else {
				throw e;
			}
		}
	}
	private void updateValueLocally(String valueJsonString) throws IOException {
		// 일단 SubmodelElement 전체를 읽어와서 JSON 문자열로 업데이트한 뒤 다시 쓰는 방식으로 처리한다.
		SubmodelElement buffer = read();
		ElementValues.updateWithValueJsonString(buffer, valueJsonString);
		write(buffer);
	}

	@Override
	public void write(SubmodelElement sme) throws IOException {
		getSubmodelService().setSubmodelElementByPath(getIdShortPathString(), sme);
	}

	@Override
	public void readAttachment(OutputStream out) throws IOException {
		getSubmodelService().getAttachmentByPath(getIdShortPathString(), out);
	}

	@Override
	public void updateAttachment(FileValue file, InputStream content) throws IOException {
		getSubmodelService().putAttachmentByPath(getIdShortPathString(), file, content);
	}

	@Override
	public void removeAttachment() throws IOException {
		getSubmodelService().deleteAttachmentByPath(getIdShortPathString());
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	/**
	 * 주어진 JSON 노드에서 특정 필드를 추출하여 반환한다.
	 * 만약 해당 필드가 존재하지 않으면 {@link IOException} 예외를 던진다.
	 * <p>
	 * 이 함수는 본 클래스를 상속받는 하위 클래스에서 JSON 입력을 처리할 때 유용하게 사용할 수 있다.
	 *
	 * @param jnode			조회 대상 JSON 노드 객체.
	 * @param fieldName		추출하려는 필드의 이름.
	 * @return	추출된 필드에 해당하는 {@link JsonNode} 객체.
	 * @throws IOException	해당 필드가 존재하지 않는 경우.
	 */
	protected static JsonNode checkJsonField(JsonNode jnode, String fieldName) throws IOException {
		JsonNode fieldNode = jnode.get(fieldName);
		if ( fieldNode == null ) {
			String json = MDTModelSerDe.toJsonString(jnode);
			throw new IOException("Missing required field '" + fieldName + "': ref=" + json);
		}
		
		return fieldNode;
	}
	
	private SubmodelElement loadPrototype() {
		return getSubmodelService().getSubmodelElementByPath(getIdShortPathString());
	}
}
