package mdt.model.registry;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InvalidIdShortPathException extends RegistryException {
    private static final long serialVersionUID = 1L;

    public InvalidIdShortPathException(String idShortPath, String details) {
        super(String.format("%s (path=%s)", details, idShortPath));
    }
}
