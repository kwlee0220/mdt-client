package mdt.model.sm.ref;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import mdt.model.service.MDTInstance;
import mdt.model.sm.SubmodelUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using = SubmodelUtils.Serializer.class)
@JsonDeserialize(using = SubmodelUtils.Deserializer.class)
public interface MDTSubmodelReference extends SubmodelReference, MDTInstanceManagerAwareReference {
	/**
	 * Submodel을 포함한 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return		MDTInstance 식별자.
	 */
	public String getInstanceId();
	
	/**
	 * 대상 Submodel의 idShort를 반환한다.
	 * 
	 * @return		Submodel의 idShort.
	 */
	public String getSubmodelIdShort();

	/**
	 * 대상 Submodel의 식별자(id)를 반환한다.
	 * 
	 * @return		Submodel의 식별자.
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
	
	public void serialize(JsonGenerator gen) throws IOException;
}
