package mdt.workflow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.InternalException;
import utils.Preconditions;

import mdt.model.MDTModelSerDe;
import mdt.workflow.model.TaskDescriptor;


/**
 * 워크플로우 모델.
 * <p>
 * 워크플로우를 구성하는 task들의 기술자({@link TaskDescriptor}) 목록과 식별자, 이름, 설명, GUI 편집기용
 * 부가 정보를 갖는다. JSON으로 직렬화되며, {@code null}인 필드는 출력하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "description", "taskDescriptors", "gui"})
public class WorkflowModel {
	private String m_id;
	private @Nullable String m_name;
	private @Nullable String m_description;

	private List<TaskDescriptor> m_taskDescriptors = Lists.newArrayList();
	private Map<String, Object> m_gui = Maps.newHashMap();
	
	/**
	 * task 기술자를 목록 끝에 추가한다.
	 *
	 * @param taskDesc	추가할 task 기술자.
	 * @throws IllegalArgumentException	{@code taskDesc}가 {@code null}이거나, 동등한 기술자가 이미 있는 경우.
	 */
	public void addTaskDescriptor(TaskDescriptor taskDesc) {
		Preconditions.checkNotNullArgument(taskDesc, "TaskDescriptor must not be null");
		
		if ( m_taskDescriptors.contains(taskDesc) ) {
			throw new IllegalArgumentException("taskDesc already exists: " + taskDesc.getId());
		}
		
		m_taskDescriptors.add(taskDesc);
	}
	
	/**
	 * 워크플로우 모델 식별자를 반환한다.
	 *
	 * @return	모델 식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * 워크플로우 모델 식별자를 설정한다.
	 *
	 * @param id	모델 식별자.
	 */
	public void setId(String id) {
		m_id = id;
	}
	
	/**
	 * 워크플로우 모델 이름을 반환한다.
	 *
	 * @return	모델 이름. 지정되지 않은 경우 {@code null}.
	 */
	public @Nullable String getName() {
		return m_name;
	}
	
	/**
	 * 워크플로우 모델 이름을 설정한다.
	 *
	 * @param name	모델 이름. {@code null} 가능.
	 */
	public void setName(@Nullable String name) {
		m_name = name;
	}
	
	/**
	 * 워크플로우 모델 설명을 반환한다.
	 *
	 * @return	모델 설명. 지정되지 않은 경우 {@code null}.
	 */
	public @Nullable String getDescription() {
		return m_description;
	}
	
	/**
	 * 워크플로우 모델 설명을 설정한다.
	 *
	 * @param description	모델 설명. {@code null} 가능.
	 */
	public void setDescription(@Nullable String description) {
		m_description = description;
	}
	
	/**
	 * task 기술자 목록을 반환한다. 내부 리스트를 그대로 반환하므로 수정하면 모델에 반영된다.
	 *
	 * @return	task 기술자 목록.
	 */
	public List<TaskDescriptor> getTaskDescriptors() {
		return m_taskDescriptors;
	}
	
	/**
	 * task 기술자 목록을 설정한다. 주어진 리스트를 복사하지 않고 그대로 사용한다.
	 *
	 * @param taskDescs	task 기술자 목록.
	 */
	public void setTaskDescriptors(List<TaskDescriptor> taskDescs) {
		m_taskDescriptors = taskDescs;
	}
	
	/**
	 * GUI 편집기에서 사용하는 부가 정보를 반환한다.
	 *
	 * @return	GUI 정보 맵.
	 */
	public Map<String, Object> getGui() {
		return m_gui;
	}
	
	/**
	 * GUI 편집기에서 사용하는 부가 정보를 설정한다.
	 *
	 * @param gui	GUI 정보 맵.
	 */
	public void setGui(Map<String, Object> gui) {
		m_gui = gui;
	}
	
	/**
	 * 이 모델을 JSON 문자열로 변환한다.
	 *
	 * @return	JSON 문자열.
	 */
	public String toJsonString() {
		return MDTModelSerDe.toJsonString(this);
	}
	
	/**
	 * JSON 파일을 읽어 워크플로우 모델을 만든다.
	 *
	 * @param workflowDescFile	워크플로우 모델 JSON 파일.
	 * @return	워크플로우 모델.
	 * @throws StreamReadException	JSON 문법이 올바르지 않은 경우.
	 * @throws DatabindException	JSON 내용을 워크플로우 모델로 변환할 수 없는 경우.
	 * @throws IOException	파일을 읽는 중 오류가 발생한 경우.
	 */
	public static WorkflowModel parseJsonFile(File workflowDescFile)
		throws StreamReadException, DatabindException, IOException {
		return MDTModelSerDe.getJsonMapper().readValue(workflowDescFile, WorkflowModel.class);
	}
	
	/**
	 * JSON 문자열을 파싱하여 워크플로우 모델을 만든다.
	 *
	 * @param workflowDescJson	워크플로우 모델 JSON 문자열.
	 * @return	워크플로우 모델.
	 * @throws JsonProcessingException	JSON 문법이 올바르지 않거나 워크플로우 모델로 변환할 수 없는 경우.
	 */
	public static WorkflowModel parseJsonString(String workflowDescJson)
		throws JsonProcessingException {
		return MDTModelSerDe.getJsonMapper()
							.readValue(workflowDescJson, WorkflowModel.class);
	}
	
	/**
	 * 워크플로우 모델 JSON 파싱 실패를 알리는 {@link InternalException}을 만든다.
	 * <p>
	 * 메시지에 JSON 문자열과 원인을 포함하고, 원인 예외를 cause로 연결한다.
	 *
	 * @param jsonStr	파싱에 실패한 JSON 문자열.
	 * @param cause	파싱 실패 원인.
	 * @return	생성된 예외 객체.
	 */
	public static InternalException toInternalException(String jsonStr, Exception cause) {
		String msg = String.format("Failed parse WorkflowDescriptor: json=%s, cause=%s",
									jsonStr, cause);
		return new InternalException(msg, cause);
	}
	
	@Override
	public String toString() {
		return String.format("WorkflowModel(id=%s, name=%s, #tasks=%d)",
								m_id, m_name, m_taskDescriptors.size());
	}
}
