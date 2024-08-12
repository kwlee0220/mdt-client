package mdt.ksx9101.simulation;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SimulationInput {
	public String getInputID();
	public void setInputID(String inputID);

	public String getInputName();
	public void setInputName(String inputName);

	public String getInputValue();
	public void setInputValue(String inputValue);

	public String getInputType();
	public void setInputType(String inputType);
}
