package mdt.workflow;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface WorkflowListener {
	public void onWorkflowStarting(String wfName);
	public void onWorkflowStarted(String wfName);

	public void onWorkflowCompleted(String wfName);
	public void onWorkflowFailed(String wfName);

}
