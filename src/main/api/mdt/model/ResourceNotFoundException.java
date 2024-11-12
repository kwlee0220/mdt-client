package mdt.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceNotFoundException extends MDTException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String msg) {
        super(msg);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s(%s)", resourceType, resourceId));
    }
}
