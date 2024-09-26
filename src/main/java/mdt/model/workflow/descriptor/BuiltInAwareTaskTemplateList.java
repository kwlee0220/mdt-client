package mdt.model.workflow.descriptor;

import java.util.List;

import utils.KeyedValueList;
import utils.stream.FStream;

import mdt.model.workflow.BuiltInTaskTemplates;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BuiltInAwareTaskTemplateList extends KeyedValueList<String, TaskTemplateDescriptor> {
	BuiltInAwareTaskTemplateList() {
		super(TaskTemplateDescriptor::getId);
		
		for ( TaskTemplateDescriptor builtin: BuiltInTaskTemplates.getAll() ) {
			super.add(builtin);
		}
	}
	
	public boolean isBuiltInTaskTemplate(String tmpltId) {
		return BuiltInTaskTemplates.getIdSet().contains(tmpltId);
	}
	
	public List<TaskTemplateDescriptor> getBuiltInTaskTemplateAll() {
		return FStream.from(this).filter(tmplt -> isBuiltInTaskTemplate(tmplt.getId())).toList();
	}
	
	public List<TaskTemplateDescriptor> getUserDefinedTaskTemplateAll() {
		return FStream.from(this).filter(tmplt -> !isBuiltInTaskTemplate(tmplt.getId())).toList();
	}

	@Override
	public boolean add(TaskTemplateDescriptor desc) {
		if ( BuiltInTaskTemplates.getIdSet().contains(desc.getId()) ) {
			throw new IllegalArgumentException("Builtin TaskTemplate exists: id=" + desc.getId());
		}
		
		return super.add(desc);
	}

	@Override
	public void add(int index, TaskTemplateDescriptor desc) {
		assertValidIndex(index);
		if ( BuiltInTaskTemplates.getIdSet().contains(desc.getId()) ) {
			throw new IllegalArgumentException("Builtin TaskTemplate exists: id=" + desc.getId());
		}
		
		super.add(index, desc);
	}
	
	@Override
	public TaskTemplateDescriptor remove(int index) {
		TaskTemplateDescriptor tmplt = get(index);
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
