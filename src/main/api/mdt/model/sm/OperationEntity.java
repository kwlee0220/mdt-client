package mdt.model.sm;

import java.util.List;

import mdt.model.Input;
import mdt.model.Output;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface OperationEntity {
	public List<Input> getInputs();
	public void setInputs(List<Input> inputs);
	
	public List<Output> getOutputs();
	public void setOutputs(List<Output> outputs);
}
