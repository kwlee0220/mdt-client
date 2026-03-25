package mdt.model.instance;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterServiceCollection extends AbstractList<MDTParameterService> {
	private final List<MDTParameterService> m_paramSvcList;
	private final Map<String,MDTParameterService> m_paramSvcMap;
	
	public MDTParameterServiceCollection(MDTInstance instance, List<MDTParameterDescriptor> descList) {
		m_paramSvcMap = Maps.newLinkedHashMap();
		m_paramSvcList = FStream.from(descList)
								.map(desc -> new MDTParameterService(instance, desc))
								.peek(svc -> m_paramSvcMap.put(svc.getDescriptor().getId(), svc))
				                .toList();
	}

	@Override
	public int size() {
		return m_paramSvcList.size();
	}

	@Override
	public MDTParameterService get(int index) {
		return m_paramSvcList.get(index);
	}
	
	public MDTParameterService get(String paramId) {
		return m_paramSvcMap.get(paramId);
	}
	
	public boolean contains(String paramId) {
		return m_paramSvcMap.containsKey(paramId);
	}
	
	@Override
	public Iterator<MDTParameterService> iterator() {
		return m_paramSvcList.iterator();
	}
	
	public Set<String> toKeyList() {
		return m_paramSvcMap.keySet();
	}
	
	public List<MDTParameterDescriptor> toDescriptorList() {
		return FStream.from(m_paramSvcList).map(svc -> svc.getDescriptor()).toList();
	}
	
	@Override
	public boolean add(MDTParameterService svc) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void add(int index, MDTParameterService svc) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MDTParameterService set(int index, MDTParameterService svc) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MDTParameterService remove(int index) {
		throw new UnsupportedOperationException();
	}
}
