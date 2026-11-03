package mdt.model.instance;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import utils.stream.FStream;


/**
 * 한 MDTInstance에 속한 {@link MDTParameterService}들의 읽기 전용 리스트.
 * <p>
 * 파라미터 기술자 목록의 순서를 유지하며, 인덱스뿐 아니라 파라미터 식별자로도 접근할 수 있다.
 * 원소를 추가/변경/삭제하는 연산은 {@link UnsupportedOperationException}을 발생시킨다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterServiceCollection extends AbstractList<MDTParameterService> {
	private final List<MDTParameterService> m_paramSvcList;
	private final Map<String,MDTParameterService> m_paramSvcMap;
	
	/**
	 * 주어진 파라미터 기술자 목록으로부터 파라미터 서비스 리스트를 생성한다.
	 *
	 * @param instance	파라미터들이 속한 MDTInstance.
	 * @param descList	파라미터 기술자 목록.
	 */
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
	
	/**
	 * 주어진 식별자의 파라미터 서비스를 반환한다.
	 *
	 * @param paramId	파라미터 식별자.
	 * @return	파라미터 서비스 객체. 해당 식별자의 파라미터가 없는 경우 {@code null}.
	 */
	public MDTParameterService get(String paramId) {
		return m_paramSvcMap.get(paramId);
	}
	
	/**
	 * 주어진 식별자의 파라미터가 포함되어 있는지 여부를 반환한다.
	 *
	 * @param paramId	파라미터 식별자.
	 * @return	포함되어 있으면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public boolean contains(String paramId) {
		return m_paramSvcMap.containsKey(paramId);
	}
	
	@Override
	public Iterator<MDTParameterService> iterator() {
		return m_paramSvcList.iterator();
	}
	
	/**
	 * 포함된 파라미터들의 식별자 집합을 반환한다.
	 * <p>
	 * 반환되는 집합은 내부 map의 key 집합 view로, 파라미터 순서를 유지한다.
	 *
	 * @return	파라미터 식별자 집합.
	 */
	public Set<String> toKeyList() {
		return m_paramSvcMap.keySet();
	}
	
	/**
	 * 포함된 파라미터들의 기술자 목록을 반환한다.
	 *
	 * @return	파라미터 기술자 목록.
	 */
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
