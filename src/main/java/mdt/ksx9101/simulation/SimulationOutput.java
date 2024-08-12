package mdt.ksx9101.simulation;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationOutput {
	public String getOutputID();
	public void setOutputID(String outputID);

	public String getOutputName();
	public void setOutputName(String outputName);

	public String getOutputValue();
	public void setOutputValue(String outputValue);

	public String getOutputType();
	public void setOutputType(String outputType);
}
