package mdt.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTEntityFactory {
	public SubmodelElementEntity newInstance(String id);
}
