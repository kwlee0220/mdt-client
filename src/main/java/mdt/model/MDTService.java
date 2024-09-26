package mdt.model;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.workflow.MDTWorkflowManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTService {
	public static final Class<MDTInstanceManager> INSTANCE_MANAGER = MDTInstanceManager.class;
	public static final Class<MDTWorkflowManager> WORKFLOW_MANAGER = MDTWorkflowManager.class;
}
