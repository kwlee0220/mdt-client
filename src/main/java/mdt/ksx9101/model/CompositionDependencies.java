package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface CompositionDependencies {
	public default void update(String idShortPath, Object value) {
		throw new UnsupportedOperationException(getClass().getName() + ".update(idShort, value)");
	}
}
