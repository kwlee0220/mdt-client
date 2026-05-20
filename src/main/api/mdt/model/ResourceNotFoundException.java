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
    
	public static ResourceNotFoundException ofIdShort(String type, String idShort) {
		return new ResourceNotFoundException(type, "idShort=" + idShort);
	}
    
	public static ResourceNotFoundException ofId(String type, String id) {
		return new ResourceNotFoundException(type, "id=" + id);
	}

    public ResourceNotFoundException(String msg) {
        super(msg);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s(%s)", resourceType, resourceId));
    }
}
