package mdt.model.sm.ai;

import java.time.Duration;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Model {
	public String getLearningMethod();
	public void setLearningMethod(String method);
	
	public String getAIModelType();
	public void setAIModelType(String type);
	
	public String getAIFramework();
	public void setAIFramework(String framework);
	
	public String getModelDescription();
	public void setModelDescription(String desc);
	
	public Duration getLastExecutionTime();
	public void setLastExecutionTime(Duration version);
}
