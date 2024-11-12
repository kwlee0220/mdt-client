package mdt.workflow.model;

import java.util.List;

import mdt.model.MDTService;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTWorkflowManager extends MDTService {
	public WorkflowDescriptor getWorkflowDescriptor(String id) throws ResourceNotFoundException;
	public List<WorkflowDescriptor> getWorkflowDescriptorAll();
	
	public String addWorkflowDescriptor(WorkflowDescriptor desc) throws ResourceAlreadyExistsException;
	public void removeWorkflowDescriptor(String id) throws ResourceNotFoundException;
	public void removeWorkflowDescriptorAll();
	
//	public TaskDescriptor getBuiltInTaskTemplate(String id) throws ResourceNotFoundException;
//	public Collection<TaskDescriptor> getBuiltInTaskTemplateAll();
	
	public String getArgoWorkflowDescriptor(String id, String clientImageName) throws ResourceNotFoundException;
}
