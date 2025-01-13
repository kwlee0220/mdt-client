package mdt.model.instance;

import java.util.List;


/**
 * Interface representing the summary information of an MDT instance.
 * The summary information includes the ID, AAS ID, and AAS ID short name of the MDT instance.
 * It also includes the list of parameters and AI and Simulation operations for the MDT instance.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceInfo {
	/**
	 * Gets the ID of the MDT instance.
	 * 
	 * @return the ID of the MDT instance
	 */
	public String getId();

	/**
	 * Gets the AAS ID of the MDT instance.
	 * 
	 * @return the AAS ID of the MDT instance
	 */
	public String getAasId();

	/**
	 * Gets the AAS IdShort of the MDT instance.
	 * 
	 * @return the AAS IdShort of the MDT instance
	 */
	public String getAasIdShort();

	/**
	 * Gets the asset type of the MDT instance.
	 * <p>
	 * The asset type of the MDT instance can be one of the following:
	 * <ul>
	 * 	<li>Line</li>
	 * 	<li>Operation</li>
	 * 	<li>Equipment</li>
	 * </ul>
	 * 
	 * @return the asset type of the MDT instance
	 */
	public String getAssetType();
	
	/**
	 * Gets the status of the MDT instance.
	 * 
	 * @return the status of the MDT instance
	 */
	public MDTInstanceStatus getStatus();
	
	/**
	 * Gets the base endpoint of the MDT instance.
	 * <p>
	 * The base endpoint of the MDT instance is the prefix of the endpoint URL to connect to the MDT instance.
	 *
	 * @return	the base endpoint of the MDT instance.
	 */
	public String getBaseEndpoint();

	/**
	 * Gets the list of parameters of the MDT instance.
	 * 
	 * @return the list of parameters
	 */
	public List<ParameterInfo> getParameters();

	/**
	 * Gets the list of operations of the MDT instance.
	 * 
	 * @return the list of operations
	 */
	public List<OperationInfo> getOperations();
}
