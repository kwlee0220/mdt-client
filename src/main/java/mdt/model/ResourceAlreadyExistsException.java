package mdt.model;

import mdt.model.registry.RegistryException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ResourceAlreadyExistsException extends RegistryException {
    private static final long serialVersionUID = 1L;

    public ResourceAlreadyExistsException(String msg) {
        super(msg);
    }

    public ResourceAlreadyExistsException(String resourceType, String resourceId) {
        super(String.format("%s(%s)", resourceType, resourceId));
    }
}
