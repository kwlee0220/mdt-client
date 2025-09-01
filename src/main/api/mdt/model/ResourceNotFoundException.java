package mdt.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceNotFoundException extends ResourceException {
    private static final long serialVersionUID = 1L;

    public static ResourceNotFoundException ofSemanticId(String target, String semanticId) {
        return new ResourceNotFoundException("Submodel", "semanticId=" + semanticId);
    }

    public ResourceNotFoundException(String msg) {
        super(msg);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s(%s)", resourceType, resourceId));
    }
}
