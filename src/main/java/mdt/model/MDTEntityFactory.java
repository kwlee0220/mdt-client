package mdt.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTEntityFactory {
	public MDTSubmodelElement newInstance(String id);
}
