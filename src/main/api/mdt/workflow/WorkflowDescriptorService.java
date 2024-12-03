package mdt.workflow;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.workflow.model.WorkflowDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowDescriptorService {
	@GetExchange("/descriptors")
	public List<WorkflowDescriptor> getWorkflowDescriptorAll();

	@GetExchange("/descriptors/{id}")
	public WorkflowDescriptor getWorkflowDescriptor(@PathVariable("id") String id) throws ResourceNotFoundException;
	
	@GetExchange("/descriptors/{id}/argo")
	public String getArgoWorkflowDescriptor(@PathVariable("id") String id,
											@RequestParam("client-image") String clientImageName)
		throws ResourceNotFoundException;

	public default String addWorkflowDescriptor(WorkflowDescriptor desc) throws ResourceAlreadyExistsException {
		return addOrUpdateWorkflowDescriptor(desc, false);
	}
	
	@PostExchange("/descriptors")
	public String addOrUpdateWorkflowDescriptor(@RequestBody WorkflowDescriptor desc,
												@RequestParam("updateIfExists") Boolean updateIfExists)
		throws ResourceAlreadyExistsException;

	@DeleteExchange("/descriptors/{id}")
	public void removeWorkflowDescriptor(@PathVariable("id") String id) throws ResourceNotFoundException;
	@DeleteExchange("/descriptors")
	public void removeWorkflowDescriptorAll();
}
