package mdt.model.sm.simulation;

import java.time.Duration;

/**
 * Simulation Submodel의 {@code SimulationInfo.Model} SubmodelElementCollection 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Model {
	/**
	 * 모델 파일 타입를 반환한다.
	 *
	 * @return	모델 파일 타입.
	 */
	public String getModelFileType();
	/**
	 * 모델 파일 타입를 설정한다.
	 *
	 * @param id	모델 파일 타입.
	 */
	public void setModelFileType(String id);
	
	/**
	 * 모델 파일 버전 정보를 반환한다.
	 *
	 * @return	모델 파일 버전 정보.
	 */
	public ModelFileVersion getModelFileVersion();
	/**
	 * 모델 파일 버전 정보를 설정한다.
	 *
	 * @param version	모델 파일 버전 정보.
	 */
	public void setModelFileVersion(ModelFileVersion version);
	
	/**
	 * 최근 실행 소요 시간을 반환한다.
	 *
	 * @return	최근 실행 소요 시간.
	 */
	public Duration getLastExecutionTime();
	/**
	 * 최근 실행 소요 시간을 설정한다.
	 *
	 * @param version	최근 실행 소요 시간.
	 */
	public void setLastExecutionTime(Duration version);
}
