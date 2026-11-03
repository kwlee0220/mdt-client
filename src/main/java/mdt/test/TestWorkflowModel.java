package mdt.test;

import java.io.File;

import utils.io.FileUtils;

import mdt.workflow.WorkflowModel;

/**
 * {@code $MDT_HOME/models} 아래의 워크플로우 모델 JSON 파일들을 {@code WorkflowModel}로 파싱한 뒤
 * 다시 JSON으로 출력하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestWorkflowModel {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
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
