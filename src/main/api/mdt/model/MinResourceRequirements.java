package mdt.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MinResourceRequirements {
	public String getGPUSpec();
	public void setGPUSpec(String spec);
	
	public String getGPUNum();
	public void setGPUNum(String num);
	
	public String getCPUSpec();
	public void setCPUSpec(String num);
	
	public String getMemorySize();
	public void setMemorySize(String size);
	
	public String getMemorySpeed();
	public void setMemorySpeed(String speed);
}
