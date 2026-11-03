package mdt.model;

/**
 * AI/Simulation 모델 수행에 필요한 최소 컴퓨팅 자원 요구사항을 표현하는 인터페이스이다.
 * <p>
 * 모든 항목은 문자열로 표현된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MinResourceRequirements {
	/**
	 * 요구되는 GPU 사양을(를) 반환한다.
	 *
	 * @return	GPU 사양.
	 */
	public String getGPUSpec();
	/**
	 * 요구되는 GPU 사양을(를) 설정한다.
	 *
	 * @param spec	GPU 사양.
	 */
	public void setGPUSpec(String spec);
	
	/**
	 * 요구되는 GPU 개수을(를) 반환한다.
	 *
	 * @return	GPU 개수.
	 */
	public String getGPUNum();
	/**
	 * 요구되는 GPU 개수을(를) 설정한다.
	 *
	 * @param num	GPU 개수.
	 */
	public void setGPUNum(String num);
	
	/**
	 * 요구되는 CPU 사양을(를) 반환한다.
	 *
	 * @return	CPU 사양.
	 */
	public String getCPUSpec();
	/**
	 * 요구되는 CPU 사양을(를) 설정한다.
	 *
	 * @param num	CPU 사양.
	 */
	public void setCPUSpec(String num);
	
	/**
	 * 요구되는 메모리 크기을(를) 반환한다.
	 *
	 * @return	메모리 크기.
	 */
	public String getMemorySize();
	/**
	 * 요구되는 메모리 크기을(를) 설정한다.
	 *
	 * @param size	메모리 크기.
	 */
	public void setMemorySize(String size);
	
	/**
	 * 요구되는 메모리 속도을(를) 반환한다.
	 *
	 * @return	메모리 속도.
	 */
	public String getMemorySpeed();
	/**
	 * 요구되는 메모리 속도을(를) 설정한다.
	 *
	 * @param speed	메모리 속도.
	 */
	public void setMemorySpeed(String speed);
}
