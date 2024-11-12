package mdt.model.service;

import java.util.List;

import utils.KeyValue;
import utils.func.Funcs;

import mdt.model.ResourceNotFoundException;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ParameterCollection {
	/**
	 * 파라미터 번호에 해당하는 {@link Parameter} 객체를 반환한다.
	 * 
	 * @param paramIdx	파라미터 번호
	 * @return	{@link Parameter} 객체.
	 * @throws IllegalArgumentException	파라미터 번호가 잘못된 경우
	 * @throws ResourceNotFoundException	번호에 해당하는 파라미터가 존재하지 않는 경우.
	 */
	public default Parameter getParameter(int paramIdx) throws ResourceNotFoundException {
		return getParameterList().get(paramIdx);
	}
	
	/**
	 * 파라미터 번호에 해당하는 {@link ParameterValue} 객체를 반환한다.
	 * 
	 * @param paramIdx	파라미터 번호
	 * @return	{@link ParameterValue} 객체.
	 * @throws IllegalArgumentException	파라미터 번호가 잘못된 경우
	 * @throws ResourceNotFoundException	번호에 해당하는 파라미터가 존재하지 않는 경우.
	 */
	public default ParameterValue getParameterValue(int paramIdx) throws ResourceNotFoundException {
		return getParameterValueList().get(paramIdx);
	}
	
	/**
	 * 파라미터 식별자에 해당하는 {@link Parameter} 객체를 반환한다.
	 * 
	 * @param paramId	파라미터 식별자
	 * @return	{@link Parameter} 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 파라미터가 존재하지 않는 경우.
	 */
	public default Parameter getParameter(String paramId) throws ResourceNotFoundException {
		return Funcs.findFirst(this.getParameterList(), param -> param.getParameterId().equals(paramId))
					.getOrThrow(() -> new ResourceNotFoundException("Parameter", "id=" + paramId));
	}
	public default int getParameterIndex(String paramId) throws ResourceNotFoundException {
		return Funcs.findFirstIndexed(this.getParameterList(), param -> param.getParameterId().equals(paramId))
						.getOrThrow(() -> new ResourceNotFoundException("Parameter", "id=" + paramId))
						.index();
	}
//	public SubmodelElement getParameterAsSubmodelElement(String paramId) throws ResourceNotFoundException;
//	public String getIdShortPathOfParameter(String paramId);
	
	/**
	 * 파라미터 식별자에 해당하는 {@link ParameterValue} 객체를 반환한다.
	 * 
	 * @param paramId	파라미터 식별자
	 * @return	{@link ParameterValue} 객체.
	 * @throws ResourceNotFoundException	식별자에 해당하는 파라미터가 존재하지 않는 경우.
	 */
	public default ParameterValue getParameterValue(String paramId) throws ResourceNotFoundException {
		return Funcs.findFirst(this.getParameterValueList(), param -> param.getParameterId().equals(paramId))
				.getOrThrow(() -> new ResourceNotFoundException("ParameterValue", "id=" + paramId));
	}
	public default int getParameterValueIndex(String paramId) throws ResourceNotFoundException {
		return Funcs.findFirstIndexed(this.getParameterValueList(), param -> param.getParameterId().equals(paramId))
						.getOrThrow(() -> new ResourceNotFoundException("ParameterValue", "id=" + paramId))
						.index();
	}
//	public SubmodelElement getParameterValueAsSubmodelElement(String paramId) throws ResourceNotFoundException;
//	public String getIdShortPathOfParameterValue(String paramId);
	
	/**
	 * 파라미터 식별자에 해당하는 {@link Parameter}와 {@link ParameterValue} 객체 쌍을 반환한다.
	 * 
	 * @param paramId
	 * @return	{@link Parameter} 객체와 {@link ParameterValue} 객체 쌍.
	 * @throws ResourceNotFoundException	식별자에 해당하는 P파라미터가 존재하지 않는 경우.
	 */
	public default KeyValue<Parameter,ParameterValue> getParameterKeyValue(String paramId)
		throws ResourceNotFoundException {
		return KeyValue.of(getParameter(paramId), getParameterValue(paramId));
	}

	/**
	 * 등록된 모든 파라미터 목록을 반환한다.
	 * 
	 * @return	{@link Parameter} 객체 목록.
	 */
	public List<Parameter> getParameterList();
	
	/**
	 * 등록된 모든 파라미터 값 목록을 반환한다.
	 * 
	 * @return	{@link ParameterValue} 객체 목록.
	 */
	public List<ParameterValue> getParameterValueList();
}
