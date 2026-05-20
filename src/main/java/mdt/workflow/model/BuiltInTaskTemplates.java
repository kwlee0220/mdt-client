package mdt.workflow.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.workflow.model.TaskDescriptor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BuiltInTaskTemplates {
	private static final Logger s_logger = LoggerFactory.getLogger(BuiltInTaskTemplates.class);
	private static Map<String,TaskDescriptor> MAP;
	
	static {
		try {
//			MAP = FStream.from(List.of(
//									SetTask.getTemplateDescriptor(),
//									CopyTask.getTemplateDescriptor(),
//									ProgramTask.getTemplateDescriptor(),
//									HttpTask.getTemplateDescriptor(),
//									JsltTask.getTemplateDescriptor(),
//									AASOperationTask.getTemplateDescriptor()
//								))
//						.toMap(TaskDescriptor::getId);
		}
		catch ( Exception e ) {
			s_logger.error("Failed to build built-in TaskTemplates: cause=" + e);
		}
	}
	
	public static TaskDescriptor get(String id) {
		return MAP.get(id);
	}
	
	public static boolean existTemplate(String id) {
		return MAP.containsKey(id);
	}
	
	public static Set<String> getIdSet() {
		return MAP.keySet();
	}
	
	public static Collection<TaskDescriptor> getAll() {
		return MAP.values();
	}
}
