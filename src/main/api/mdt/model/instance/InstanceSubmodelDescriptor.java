package mdt.model.instance;


/**
 * Represents a descriptor of an instance submodel.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InstanceSubmodelDescriptor {
	/**
	 * Gets the unique identifier of the instance submodel descriptor.
	 *
	 * @return the unique identifier as a String.
	 */
	public String getId();

	/**
	 * Gets the idShort of the instance submodel descriptor.
	 *
	 * @return the idShort as a String.
	 */
	public String getIdShort();

	/**
	 * Gets the semantic identifier of the instance submodel descriptor.
	 *
	 * @return the semantic identifier as a String.
	 */
	public String getSemanticId();

}