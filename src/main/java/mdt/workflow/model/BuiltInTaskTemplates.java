package mdt.workflow.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.workflow.model.TaskDescriptor;


/**
 * 내장(built-in) task 템플릿 등록부.
 * <p>
 * 현재는 등록된 내장 템플릿이 없다(템플릿을 등록하던 코드는 주석 처리되어 있다). 따라서 조회 메소드는
 * 항상 빈 결과를 반환한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BuiltInTaskTemplates {
	private static final Logger s_logger = LoggerFactory.getLogger(BuiltInTaskTemplates.class);
	private static Map<String,TaskDescriptor> MAP = Map.of();
	
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
	
	/**
	 * 주어진 식별자의 내장 템플릿을 반환한다.
	 *
	 * @param id	템플릿 식별자.
	 * @return	내장 템플릿. 없으면 {@code null}.
	 */
	public static TaskDescriptor get(String id) {
		return MAP.get(id);
	}
	
	/**
	 * 주어진 식별자의 내장 템플릿이 있는지 확인한다.
	 *
	 * @param id	템플릿 식별자.
	 * @return	있으면 {@code true}.
	 */
	public static boolean existTemplate(String id) {
		return MAP.containsKey(id);
	}
	
	/**
	 * 모든 내장 템플릿의 식별자 집합을 반환한다.
	 *
	 * @return	식별자 집합 (변경할 수 없는 내부 맵의 key 뷰).
	 */
	public static Set<String> getIdSet() {
		return MAP.keySet();
	}
	
	/**
	 * 모든 내장 템플릿을 반환한다.
	 *
	 * @return	내장 템플릿 컬렉션 (변경할 수 없는 내부 맵의 value 뷰).
	 */
	public static Collection<TaskDescriptor> getAll() {
		return MAP.values();
	}
}
