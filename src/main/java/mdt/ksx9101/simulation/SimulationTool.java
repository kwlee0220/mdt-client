package mdt.ksx9101.simulation;

import java.time.Duration;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationTool {
	public String getSimToolName();
	public void setSimToolName(String simToolName);

	public String getSimulatorEndpoint();
	public void setSimulatorEndpoint(String simulatorEndpoint);

	public Duration getSimulationTimeout();
	public void setSimulationTimeout(Duration simulationTimeout);

	public Duration getSessionRetainTimeout();
	public void setSessionRetainTimeout(Duration sessionRetainTimeout);
}
