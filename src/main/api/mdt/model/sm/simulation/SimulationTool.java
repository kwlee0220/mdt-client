package mdt.model.sm.simulation;

import java.time.Duration;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationTool {
	public String getSimToolName();
	public void setSimToolName(String name);
	
	public String getOperatingSystem();
	public void setOperatingSystem(String os);
	
	public String getLicenseModel();
	public void setLicenseModel(String model);

	public String getSimulatorEndpoint();
	public void setSimulatorEndpoint(String simulatorEndpoint);

	public Duration getSimulationTimeout();
	public void setSimulationTimeout(Duration simulationTimeout);

	public Duration getSessionRetainTimeout();
	public void setSessionRetainTimeout(Duration sessionRetainTimeout);
}
