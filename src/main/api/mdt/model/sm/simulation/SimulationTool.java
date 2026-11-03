package mdt.model.sm.simulation;

import java.time.Duration;

/**
 * Simulation Submodel의 {@code SimulationInfo.SimulationTool} SubmodelElementCollection 인터페이스.
 * <p>
 * 시뮬레이션 도구의 이름, 운영체제, 라이선스, 시뮬레이터 endpoint와 제한 시간 정보를 갖는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationTool {
	/**
	 * 시뮬레이션 도구 이름을 반환한다.
	 *
	 * @return	시뮬레이션 도구 이름.
	 */
	public String getSimToolName();
	/**
	 * 시뮬레이션 도구 이름을 설정한다.
	 *
	 * @param name	시뮬레이션 도구 이름.
	 */
	public void setSimToolName(String name);
	
	/**
	 * 시뮬레이션 도구가 동작하는 운영체제를 반환한다.
	 *
	 * @return	시뮬레이션 도구가 동작하는 운영체제.
	 */
	public String getOperatingSystem();
	/**
	 * 시뮬레이션 도구가 동작하는 운영체제를 설정한다.
	 *
	 * @param os	시뮬레이션 도구가 동작하는 운영체제.
	 */
	public void setOperatingSystem(String os);
	
	/**
	 * 시뮬레이션 도구의 라이선스 모델을 반환한다.
	 *
	 * @return	시뮬레이션 도구의 라이선스 모델.
	 */
	public String getLicenseModel();
	/**
	 * 시뮬레이션 도구의 라이선스 모델을 설정한다.
	 *
	 * @param model	시뮬레이션 도구의 라이선스 모델.
	 */
	public void setLicenseModel(String model);

	/**
	 * 시뮬레이터 endpoint를 반환한다.
	 *
	 * @return	시뮬레이터 endpoint.
	 */
	public String getSimulatorEndpoint();
	/**
	 * 시뮬레이터 endpoint를 설정한다.
	 *
	 * @param simulatorEndpoint	시뮬레이터 endpoint.
	 */
	public void setSimulatorEndpoint(String simulatorEndpoint);

	/**
	 * 시뮬레이션 제한 시간을 반환한다.
	 *
	 * @return	시뮬레이션 제한 시간.
	 */
	public Duration getSimulationTimeout();
	/**
	 * 시뮬레이션 제한 시간을 설정한다.
	 *
	 * @param simulationTimeout	시뮬레이션 제한 시간.
	 */
	public void setSimulationTimeout(Duration simulationTimeout);

	/**
	 * 시뮬레이션 종료 후 세션 유지 시간을 반환한다.
	 *
	 * @return	시뮬레이션 종료 후 세션 유지 시간.
	 */
	public Duration getSessionRetainTimeout();
	/**
	 * 시뮬레이션 종료 후 세션 유지 시간을 설정한다.
	 *
	 * @param sessionRetainTimeout	시뮬레이션 종료 후 세션 유지 시간.
	 */
	public void setSessionRetainTimeout(Duration sessionRetainTimeout);
}
