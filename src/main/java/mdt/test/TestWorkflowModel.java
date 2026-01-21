package mdt.test;

import java.io.File;

import utils.io.FileUtils;

import mdt.workflow.WorkflowModel;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestWorkflowModel {
	public static final void main(String... args) throws Exception {
		File wfModelFile;
		WorkflowModel wfModel;
		
		File homeDir = FileUtils.path(System.getenv("MDT_HOME"), "models");
		
		wfModelFile = FileUtils.path(homeDir, "test", "wf-test.json");
		wfModel = WorkflowModel.parseJsonFile(wfModelFile);
		System.out.println(wfModel);
		System.out.println(wfModel.toJsonString());
		
		wfModelFile = FileUtils.path(homeDir, "test", "wf-test2.json");
		wfModel = WorkflowModel.parseJsonFile(wfModelFile);
		System.out.println(wfModel);
		System.out.println(wfModel.toJsonString());
		
		wfModelFile = FileUtils.path(homeDir, "innercase", "inspector", "wf_inspector_simulation.json");
		wfModel = WorkflowModel.parseJsonFile(wfModelFile);
		System.out.println(wfModel);
		System.out.println(wfModel.toJsonString());
		
		wfModelFile = FileUtils.path(homeDir, "innercase", "inspector", "wf-innercase-optimization.json");
		wfModel = WorkflowModel.parseJsonFile(wfModelFile);
		System.out.println(wfModel);
		System.out.println(wfModel.toJsonString());
	}
}
