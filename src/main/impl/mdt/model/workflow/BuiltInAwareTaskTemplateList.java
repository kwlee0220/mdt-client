package mdt.model.workflow;

import java.util.List;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.workflow.model.TaskDescriptor;

/**
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
	
	public boolean isBuiltInTaskTemplate(String tmpltId) {
		return BuiltInTaskTemplates.getIdSet().contains(tmpltId);
	}
	
	public List<TaskDescriptor> getBuiltInTaskTemplateAll() {
		return FStream.from(this).filter(tmplt -> isBuiltInTaskTemplate(tmplt.getId())).toList();
	}
	
	public List<TaskDescriptor> getUserDefinedTaskTemplateAll() {
		return FStream.from(this).filter(tmplt -> !isBuiltInTaskTemplate(tmplt.getId())).toList();
	}

	@Override
	public boolean add(TaskDescriptor desc) {
		if ( BuiltInTaskTemplates.getIdSet().contains(desc.getId()) ) {
			throw new IllegalArgumentException("Builtin TaskTemplate exists: id=" + desc.getId());
		}
		
		return super.add(desc);
	}

	@Override
	public void add(int index, TaskDescriptor desc) {
		assertValidIndex(index);
		if ( BuiltInTaskTemplates.getIdSet().contains(desc.getId()) ) {
			throw new IllegalArgumentException("Builtin TaskTemplate exists: id=" + desc.getId());
		}
		
		super.add(index, desc);
	}
	
	@Override
	public TaskDescriptor remove(int index) {
		TaskDescriptor tmplt = get(index);
		if ( BuiltInTaskTemplates.getIdSet().contains(tmplt.getId()) ) {
			throw new IllegalArgumentException("Cannot remove builtin TaskTemplate: id=" + tmplt.getId());
		}
		
		return super.remove(index);
	}

	@Override
	public boolean removeOfKey(String key) {
		if ( BuiltInTaskTemplates.getIdSet().contains(key) ) {
			throw new IllegalArgumentException("Cannot remove builtin TaskTemplate: id=" + key);
		}
		
		return super.removeOfKey(key);
	}
}
