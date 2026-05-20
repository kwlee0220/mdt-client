package mdt.model.sm.data;

import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.model.sm.entity.SubmodelElementEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultDataInfo extends SubmodelElementCollectionEntity implements DataInfo {
	private static final String ENTITY_IDSHORT = "DataInfo";
	
	private static final DefaultMDTEntityFactory FACTORY = new DefaultMDTEntityFactory();
	private List<SubmodelElementEntity> m_topLevelEntityList = Lists.newArrayList();
	
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
	
	public void setSubmodelElementEntityAll(List<SubmodelElementEntity> list) {
		m_topLevelEntityList = list;
	}

	@Override
	public void updateAasModel(SubmodelElement sme) {
		super.updateAasModel(sme);
		SubmodelElementCollection smc = (SubmodelElementCollection)sme;
		
		Map<String, SubmodelElement> elementMap = FStream.from(smc.getValue())
														.tagKey(SubmodelElement::getIdShort)
														.toMap();
		FStream.from(m_topLevelEntityList)
				.map(entity -> entity.newSubmodelElement())
				.forEach(newElm -> elementMap.put(newElm.getIdShort(), newElm));
		smc.setValue(Lists.newArrayList(elementMap.values()));
	}

	@Override
	public void updateFromAasModel(SubmodelElement sme) {
		super.updateFromAasModel(sme);
		SubmodelElementCollection smc = (SubmodelElementCollection)sme;
		
		m_topLevelEntityList = Lists.newArrayList();
		for ( SubmodelElement member: smc.getValue() ) {
			String id = member.getIdShort();
			SubmodelElementEntity adaptor = FACTORY.newInstance(id);
			adaptor.updateFromAasModel(member);
			if ( adaptor instanceof SubmodelElementEntity ) {
				m_topLevelEntityList.add((SubmodelElementEntity)adaptor);
			}
		}
	}
	
	@Override
	public String toString() {
		return FStream.from(m_topLevelEntityList)
						.map(SubmodelElementEntity::getIdShort)
						.join(", ", getIdShort() + "{", "}");
	}
}
