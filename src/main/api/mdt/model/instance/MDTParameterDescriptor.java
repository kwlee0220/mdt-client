package mdt.model.instance;

/**
 * MDT 파라미터의 인터페이스를 정의한다.
 * <p>
 * MDT 파라미터는 MDT 모델의 구성요소이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTParameterDescriptor {
	/**
	 * MDT 파라미터의 이름을 반환한다.
	 * 
	 * @return	파라미터 이름
	 */
	public String getName();
	
	/**
	 * MDT 파라미터의 타입을 반환한다.
	 *
	 * @return 파라미터 타입
	 */
	public String getValueType();
}
