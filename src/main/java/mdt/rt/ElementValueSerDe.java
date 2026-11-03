package mdt.rt;

import java.io.IOException;

import mdt.model.sm.value.ElementValue;


/**
 * 객체와 SubmodelElement 값({@link ElementValue}) 사이의 양방향 변환기를 정의한다.
 *
 * @param <T>	변환 대상 객체의 타입.
 * @author Kang-Woo Lee (ETRI)
 */
public interface ElementValueSerDe<T> {
	/**
	 * 주어진 객체를 {@link ElementValue}로 변환한다.
	 *
	 * @param elem	변환할 객체.
	 * @return	변환된 {@link ElementValue}.
	 * @throws IOException	변환에 실패한 경우.
	 */
	public ElementValue serializeToElementValue(T elem) throws IOException;

	/**
	 * 주어진 {@link ElementValue}를 객체로 변환한다.
	 *
	 * @param elem	변환할 {@link ElementValue}.
	 * @return	변환된 객체.
	 * @throws IOException	{@code elem}의 구성이 대상 타입과 맞지 않는 등 변환에 실패한 경우.
	 */
	public T deserializeFromElementValue(ElementValue elem) throws IOException;
}
