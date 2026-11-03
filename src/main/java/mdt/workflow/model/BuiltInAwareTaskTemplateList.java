package mdt.workflow.model;

import java.util.List;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.workflow.model.TaskDescriptor;

/**
 * 내장(built-in) task 템플릿을 항상 포함하는 task 템플릿 목록.
 * <p>
 * 생성 시 {@link BuiltInTaskTemplates}의 모든 템플릿을 미리 담으며, 템플릿 식별자를 키로 사용한다.
 * 내장 템플릿과 같은 식별자의 템플릿은 추가할 수 없고, 내장 템플릿은 삭제할 수 없다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BuiltInAwareTaskTemplateList extends KeyedValueList<String, TaskDescriptor> {
	BuiltInAwareTaskTemplateList() {
		super(TaskDescriptor::getId);
		
		for ( TaskDescriptor builtin: BuiltInTaskTemplates.getAll() ) {
			super.add(builtin);
		}
	}
	
	/**
	 * 주어진 식별자가 내장 템플릿의 식별자인지 확인한다.
	 *
	 * @param tmpltId	템플릿 식별자.
	 * @return	내장 템플릿이면 {@code true}.
	 */
	public boolean isBuiltInTaskTemplate(String tmpltId) {
		return BuiltInTaskTemplates.getIdSet().contains(tmpltId);
	}
	
	/**
	 * 목록에 있는 내장 템플릿들을 반환한다.
	 *
	 * @return	내장 템플릿 목록 (새 리스트).
	 */
	public List<TaskDescriptor> getBuiltInTaskTemplateAll() {
		return FStream.from(this).filter(tmplt -> isBuiltInTaskTemplate(tmplt.getId())).toList();
	}
	
	/**
	 * 목록에 있는 사용자 정의 템플릿(내장 템플릿이 아닌 것)들을 반환한다.
	 *
	 * @return	사용자 정의 템플릿 목록 (새 리스트).
	 */
	public List<TaskDescriptor> getUserDefinedTaskTemplateAll() {
		return FStream.from(this).filter(tmplt -> !isBuiltInTaskTemplate(tmplt.getId())).toList();
	}

	/**
	 * 템플릿을 목록 끝에 추가한다.
	 *
	 * @param desc	추가할 템플릿.
	 * @return	추가 여부 ({@link KeyedValueList#add(Object)}의 결과).
	 * @throws IllegalArgumentException	내장 템플릿과 같은 식별자를 갖는 경우.
	 */
	@Override
	public boolean add(TaskDescriptor desc) {
		if ( BuiltInTaskTemplates.getIdSet().contains(desc.getId()) ) {
			throw new IllegalArgumentException("Builtin TaskTemplate exists: id=" + desc.getId());
		}
		
		return super.add(desc);
	}

	/**
	 * 템플릿을 주어진 위치에 추가한다.
	 *
	 * @param index	추가할 위치.
	 * @param desc	추가할 템플릿.
	 * @throws IllegalArgumentException	내장 템플릿과 같은 식별자를 갖는 경우.
	 */
	@Override
	public void add(int index, TaskDescriptor desc) {
		assertValidIndex(index);
		if ( BuiltInTaskTemplates.getIdSet().contains(desc.getId()) ) {
			throw new IllegalArgumentException("Builtin TaskTemplate exists: id=" + desc.getId());
		}
		
		super.add(index, desc);
	}
	
	/**
	 * 주어진 위치의 템플릿을 삭제한다.
	 *
	 * @param index	삭제할 템플릿의 위치.
	 * @return	삭제된 템플릿.
	 * @throws IllegalArgumentException	해당 위치의 템플릿이 내장 템플릿인 경우.
	 */
	@Override
	public TaskDescriptor remove(int index) {
		TaskDescriptor tmplt = get(index);
		if ( BuiltInTaskTemplates.getIdSet().contains(tmplt.getId()) ) {
			throw new IllegalArgumentException("Cannot remove builtin TaskTemplate: id=" + tmplt.getId());
		}
		
		return super.remove(index);
	}

	/**
	 * 주어진 식별자의 템플릿을 삭제한다.
	 *
	 * @param key	삭제할 템플릿의 식별자.
	 * @return	삭제된 템플릿 ({@link KeyedValueList#removeOfKey(Object)}의 결과).
	 * @throws IllegalArgumentException	내장 템플릿의 식별자인 경우.
	 */
	@Override
	public TaskDescriptor removeOfKey(String key) {
		if ( BuiltInTaskTemplates.getIdSet().contains(key) ) {
			throw new IllegalArgumentException("Cannot remove builtin TaskTemplate: id=" + key);
		}
		
		return super.removeOfKey(key);
	}
}
