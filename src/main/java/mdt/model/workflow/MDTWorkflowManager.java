package mdt.model.workflow;

import java.util.Collection;
import java.util.List;

import mdt.model.MDTService;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.model.workflow.descriptor.WorkflowDescriptor;

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
	
	public TaskTemplateDescriptor getBuiltInTaskTemplate(String id) throws ResourceNotFoundException;
	public Collection<TaskTemplateDescriptor> getBuiltInTaskTemplateAll();
}
