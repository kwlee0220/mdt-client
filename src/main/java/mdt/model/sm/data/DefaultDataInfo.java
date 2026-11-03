package mdt.model.sm.data;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utils.stream.FStream;

import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.model.sm.entity.SubmodelElementEntity;


/**
 * {@link DataInfo} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * idShort가 {@code DataInfo}인 SubmodelElementCollection에 대응되며, 포함된 각 하위 요소는 idShort에 따라
 * {@link DefaultMDTEntityFactory}가 생성한 엔티티 객체로 변환되어 관리된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultDataInfo extends SubmodelElementCollectionEntity implements DataInfo {
	private static final String ENTITY_IDSHORT = "DataInfo";
	
	private static final DefaultMDTEntityFactory FACTORY = new DefaultMDTEntityFactory();
	private List<SubmodelElementEntity> m_topLevelEntityList = Lists.newArrayList();
	
	/**
	 * idShort가 {@code DataInfo}인 빈 {@code DefaultDataInfo} 객체를 생성한다.
	 */
	public DefaultDataInfo() {
		setIdShort(ENTITY_IDSHORT);
	}

	@Override
	public List<SubmodelElementEntity> getSubmodelElementEntityAll() {
		return m_topLevelEntityList;
	}

	@Override
	public List<SubmodelElementEntity> getSubmodelElementEntityAllByClass(Class<?> intfc) {
		return FStream.from(m_topLevelEntityList)
						.filter(intfc::isInstance)
						.toList();
	}
	
	/**
	 * 포함된 최상위 엔티티 목록을 설정한다.
	 *
	 * @param list	설정할 엔티티 객체 목록.
	 */
	public void setSubmodelElementEntityAll(List<SubmodelElementEntity> list) {
		m_topLevelEntityList = list;
	}

	/**
	 * 주어진 SubmodelElementCollection에 이 객체의 엔티티들을 반영한다.
	 * <p>
	 * 각 엔티티로부터 새 SubmodelElement를 생성하여 같은 idShort의 기존 하위 요소를 대체하거나 추가한다.
	 * 엔티티 목록에 없는 기존 하위 요소는 그대로 유지된다.
	 */
	@Override
	public void updateAasModel(SubmodelElement sme) {
		super.updateAasModel(sme);
		SubmodelElementCollection smc = (SubmodelElementCollection)sme;
		
		Map<String, SubmodelElement> elementMap = FStream.from(smc.getValue())
														.tagKey(SubmodelElement::getIdShort)
														.toMap(Maps.newLinkedHashMap());	// 기존 요소 순서를 유지한다.
		FStream.from(m_topLevelEntityList)
				.map(entity -> entity.newSubmodelElement())
				.forEach(newElm -> elementMap.put(newElm.getIdShort(), newElm));
		smc.setValue(Lists.newArrayList(elementMap.values()));
	}

	/**
	 * 주어진 SubmodelElementCollection으로부터 엔티티 목록을 다시 구성한다.
	 * <p>
	 * 각 하위 요소의 idShort로 {@link DefaultMDTEntityFactory#newInstance(String)}를 호출하여
	 * 엔티티 객체를 생성한다. 엔티티로 등록되지 않은 idShort의 하위 요소는 건너뛴다(이 요소들은
	 * {@link #updateAasModel(SubmodelElement)}에서 그대로 유지된다).
	 */
	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		super.updateFromAasModel(sme);
		SubmodelElementCollection smc = (SubmodelElementCollection)sme;
		
		m_topLevelEntityList = Lists.newArrayList();
		for ( SubmodelElement member: smc.getValue() ) {
			SubmodelElementEntity adaptor;
			try {
				adaptor = FACTORY.newInstance(member.getIdShort());
			}
			catch ( IllegalArgumentException unknownEntity ) {
				continue;
			}
			adaptor.updateFromAasModel(member);
			m_topLevelEntityList.add(adaptor);
		}
	}
	
	@Override
	public String toString() {
		return FStream.from(m_topLevelEntityList)
						.map(SubmodelElementEntity::getIdShort)
						.join(", ", getIdShort() + "{", "}");
	}
}
