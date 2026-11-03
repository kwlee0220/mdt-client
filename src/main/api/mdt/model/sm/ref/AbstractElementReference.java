package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import utils.LoggerSettable;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 * {@link ElementReference} 구현체들이 공통으로 사용하는 기능을 제공하는 추상 기반 클래스이다.
 * <p>
 * 구체 구현체에 따라 달라지지 않는 갱신({@link #update(SubmodelElement)}), Json 직렬화
 * ({@link #toJsonString()}, {@link #toJsonNode()}), 로거 관리({@link LoggerSettable})를
 * 기본 구현으로 제공한다. 참조 대상에 실제로 접근하는 메소드({@code read}, {@code write} 등)는
 * 하위 클래스에서 구현해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractElementReference implements ElementReference, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractElementReference.class);
	private Logger m_logger = s_logger;

	/**
	 * 참조가 가리키는 SubmodelElement의 값 부분을 주어진 SubmodelElement의 값으로 갱신한다.
	 * <p>
	 * 기본 구현은 {@code sme}의 값({@link ElementValues#getValue(SubmodelElement)})으로
	 * {@link #updateValue(ElementValue)}를 호출한다. idShort·semanticId 등 메타데이터는 바뀌지 않는다.
	 * 요소 전체를 교체하려면 {@link #write(SubmodelElement)}를 쓴다.
	 *
	 * @param sme	갱신할 값을 포함한 SubmodelElement 객체.
	 * @throws	IOException	갱신 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void update(SubmodelElement sme) throws IOException {
		updateValue(ElementValues.getValue(sme));
	}

	/**
	 * 주어진 값 JSON 노드로 참조가 가리키는 SubmodelElement의 값 부분을 갱신한다.
	 * <p>
	 * 기본 구현은 현재 값({@link #readValue()})을 원형으로 삼아 {@code valueNode}를 {@link ElementValue}로
	 * 해석한 뒤 {@link #updateValue(ElementValue)}에 위임한다. 따라서 갱신 전에 대상을 한 번 읽는다.
	 *
	 * @param valueNode	값 JSON 노드.
	 * @throws	IOException	읽기·해석·갱신 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void updateFromValueJson(JsonNode valueNode) throws IOException {
		ElementValue proto = readValue();
		ElementValue newVal = ElementValues.parseJsonNode(valueNode, proto);
		updateValue(newVal);
	}

	/**
	 * 이 참조를 {@link MDTModelSerDe}를 이용하여 Json 문자열로 직렬화한다.
	 *
	 * @return	직렬화된 Json 문자열.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	@Override
	public String toJsonString() throws IOException {
		return MDTModelSerDe.toJsonString(this);
	}

	/**
	 * 이 참조를 {@link MDTModelSerDe}를 이용하여 {@link JsonNode}로 직렬화한다.
	 *
	 * @return	직렬화된 {@link JsonNode} 객체.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	@Override
	public JsonNode toJsonNode() throws IOException {
		return MDTModelSerDe.toJsonNode(this);
	}

	/**
	 * 이 객체에 설정된 로거를 반환한다.
	 *
	 * @return 현재 로거. 별도로 설정하지 않은 경우 기본 로거.
	 */
	@Override
	public Logger getLogger() {
		return m_logger;
	}

	/**
	 * 이 객체가 사용할 로거를 설정한다.
	 *
	 * @param logger 설정할 로거. {@code null}이면 기본 로거로 되돌린다.
	 */
	@Override
	public void setLogger(Logger logger) {
		m_logger = logger != null ? logger : s_logger;
	}
}
