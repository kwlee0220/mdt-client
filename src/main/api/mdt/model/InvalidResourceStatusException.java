package mdt.model;

import mdt.model.instance.MDTInstanceStatus;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InvalidResourceStatusException extends MDTException {
    private static final long serialVersionUID = 1L;

    public InvalidResourceStatusException(String msg) {
        super(msg);
    }

    public InvalidResourceStatusException(String resourceType, String resourceId, MDTInstanceStatus status) {
        super(String.format("%s(%s): %s", resourceType, resourceId, status));
    }
}
