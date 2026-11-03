package mdt.test;

import java.io.File;

import mdt.model.MDTModelSerDe;
import mdt.workflow.model.TaskDescriptor;

/**
 * JSON 파일로 기술된 {@code TaskDescriptor}를 {@code MDTModelSerDe}로 읽어 출력하는 시험 프로그램.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestTaskDescriptor {
	/**
	 * 프로그램 진입점.
	 *
	 * @param args	사용하지 않음.
	 * @throws Exception	실행 중 발생한 모든 예외.
	 */
	public static final void main(String... args) throws Exception {
		File descJsonFile;
		TaskDescriptor taskDesc;
		
//		descJsonFile = new File("misc/test-workflows/test_set_task.json");
//		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
//		System.out.println(taskDesc);
//		
//		descJsonFile = new File("misc/test-workflows/test_copy_task.json");
//		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
//		System.out.println(taskDesc);
		
//		descJsonFile = new File("misc/test-workflows/test_program_task.json");
//		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
//		System.out.println(taskDesc);
//		
//		descJsonFile = new File("misc/test-workflows/test_http_task.json");
//		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
//		System.out.println(taskDesc);
		
		descJsonFile = new File("misc/test-workflows/test_aas_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
		
//		descJsonFile = new File("misc/test-workflows/test_jslt_task.json");
//		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
//		System.out.println(taskDesc);
	}
}
