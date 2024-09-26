package mdt.model.workflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.model.workflow.descriptor.TaskTemplateDescriptor;
import mdt.task.builtin.AASOperationTask;
import mdt.task.builtin.CopyTask;
import mdt.task.builtin.HttpTask;
import mdt.task.builtin.JavaTask;
import mdt.task.builtin.JsltTask;
import mdt.task.builtin.ProgramTask;
import mdt.task.builtin.SetTask;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BuiltInTaskTemplates {
	private static final Logger s_logger = LoggerFactory.getLogger(BuiltInTaskTemplates.class);
	private static Map<String,TaskTemplateDescriptor> MAP;
	
	static {
		try {
			MAP = FStream.from(List.of(
									SetTask.getTemplateDescriptor(),
									CopyTask.getTemplateDescriptor(),
									JavaTask.getTemplateDescriptor(),
									ProgramTask.getTemplateDescriptor(),
									HttpTask.getTemplateDescriptor(),
									JsltTask.getTemplateDescriptor(),
									AASOperationTask.getTemplateDescriptor()
								))
						.toMap(TaskTemplateDescriptor::getId);
		}
		catch ( Exception e ) {
			s_logger.error("Failed to build built-in TaskTemplates: cause=" + e);
		}
	}
	
	public static TaskTemplateDescriptor get(String id) {
		return MAP.get(id);
	}
	
	public static boolean existTemplate(String id) {
		return MAP.containsKey(id);
	}
	
	public static Set<String> getIdSet() {
		return MAP.keySet();
	}
	
	public static Collection<TaskTemplateDescriptor> getAll() {
		return MAP.values();
	}
}
