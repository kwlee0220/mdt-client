package mdt.test;

import java.io.File;

import mdt.model.MDTModelSerDe;
import mdt.workflow.model.TaskDescriptor;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestTaskDescriptor {
	public static final void main(String... args) throws Exception {
		File descJsonFile;
		TaskDescriptor taskDesc;
		
		descJsonFile = new File("misc/test-workflows/test_set_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
		
		descJsonFile = new File("misc/test-workflows/test_copy_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
		
		descJsonFile = new File("misc/test-workflows/test_program_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
		
		descJsonFile = new File("misc/test-workflows/test_http_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
		
		descJsonFile = new File("misc/test-workflows/test_aas_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
		
		descJsonFile = new File("misc/test-workflows/test_jslt_task.json");
		taskDesc = MDTModelSerDe.readValue(descJsonFile, TaskDescriptor.class);
		System.out.println(taskDesc);
	}
}
