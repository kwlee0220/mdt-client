package mdt.model.sm.simulation;

import java.time.Duration;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Model {
	public String getModelFileType();
	public void setModelFileType(String id);
	
	public ModelFileVersion getModelFileVersion();
	public void setModelFileVersion(ModelFileVersion version);
	
	public Duration getLastExecutionTime();
	public void setLastExecutionTime(Duration version);
}
