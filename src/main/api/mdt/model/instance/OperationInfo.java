package mdt.model.instance;

import java.util.List;


/**
 * Interface representing the operation information of an MDT instance. Provides
 * methods to access the ID, kind, input arguments, and output arguments of the
 * operation.
 */
public interface OperationInfo {
	/**
	 * Gets the ID of the operation.
	 * 
	 * @return the ID of the operation
	 */
	public String getId();

	/**
	 * Gets the kind of the operation.
	 * The kind of the operation can be one of the following:
	 * <ul>
	 * <li>"AI"</li>
	 * <li>"Simulation"</li>
	 * </ul>.
	 * 
	 * @return the kind of the operation
	 */
	public String getType();

	/**
	 * Gets the list of input arguments of the operation.
	 * 
	 * @return the list of input arguments
	 */
	public List<ParameterInfo> getInputArguments();

	/**
	 * Gets the list of output arguments of the operation.
	 * 
	 * @return the list of output arguments
	 */
	public List<ParameterInfo> getOutputArguments();
}