package mdt.model.sm.ref;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;


/**
 * MDT 프레임워크에서 관리되는 Submodel을 참조하는 인터페이스이다.
 * <p>
 * 참조는 생성(또는 역직렬화) 직후 비활성 상태이며, {@link #activate(MDTInstanceManager)}로
 * 활성화한 뒤에 {@link #getInstance()}/{@link #get()} 등을 통해 대상 Submodel에 접근할 수 있다.
 * Json 직렬화/역직렬화는 {@link SubmodelUtils.Serializer}/{@link SubmodelUtils.Deserializer}가
 * 담당한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = SubmodelUtils.Serializer.class)
@JsonDeserialize(using = SubmodelUtils.Deserializer.class)
public interface MDTSubmodelReference extends SubmodelReference {
	/**
	 * 본 참조가 활성화되어 있는지 여부를 반환한다.
	 * 
	 * @return 활성화 여부.
	 */
	public boolean isActivated();
	
	/**
	 * 본 참조를 활성화시킨다.
	 *
	 * @param manager	객체 활성화에 사용될 {@link MDTInstanceManager} 객체.
	 */
	public void activate(MDTInstanceManager manager);
	
	/**
	 * 주어진 {@link MDTInstance}가 소속된 {@link MDTInstanceManager}를 이용하여 본 참조를 활성화시킨다.
	 *
	 * @param instance	활성화에 사용할 MDTInstance 객체.
	 */
	default public void activate(MDTInstance instance) {
		activate(instance.getInstanceManager());
	}
	
	/**
	 * Submodel을 포함한 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return		MDTInstance 식별자.
	 */
	public String getInstanceId();
	
	/**
	 * 참조가 가리키는 Submodel의 식별자를 반환한다.
	 * <p>
	 * Submodel 식별자가 활성화 시점에 결정되는 구현(idShort 기반 참조 등)에서는
	 * 활성화 전에 호출하면 {@link IllegalStateException}이 발생할 수 있다.
	 *
	 * @return	Submodel 식별자.
	 */
	public String getSubmodelId();

	/**
	 * Submodel을 포함한 MDTInstance를 반환한다.
	 * <p>
	 * Reference가 {@link #activate(mdt.model.instance.MDTInstanceManager)}에 의해 activate되지 않은
	 * 경우에는 {@link IllegalStateException} 예외가 발생한다.
	 * 
	 * @return		MDTInstance
	 * @throws	IllegalStateException	reference가 activate되지 않은 경우.
	 * @see	#activate(mdt.model.instance.MDTInstanceManager)
	 */
	public MDTInstance getInstance();
	
	/**
	 * 이 참조를 주어진 {@link JsonGenerator}를 통해 Json 객체로 직렬화한다.
	 *
	 * @param gen	직렬화에 사용할 JsonGenerator.
	 * @throws IOException	직렬화 과정에서 예외가 발생한 경우.
	 */
	public void serialize(JsonGenerator gen) throws IOException;

	/**
	 * 이 참조의 MdtExpr 표현식 문자열을 반환한다.
	 *
	 * @return 표현식 문자열.
	 */
	public String toStringExpr();
}
