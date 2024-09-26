package mdt.ksx9101.model.impl;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.ksx9101.TopLevelEntity;
import mdt.ksx9101.model.DataInfo;
import mdt.model.SubmodelElementEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultDataInfo implements DataInfo, SubmodelElementEntity {
	private static final String ENTITY_IDSHORT = "DataInfo";
	
	private static final DefaultMDTEntityFactory FACTORY = new DefaultMDTEntityFactory();
	private List<TopLevelEntity> m_entities = Lists.newArrayList();

	@Override
	public List<TopLevelEntity> getKSX9101EntityList() {
		return m_entities;
	}
	
	public void setKSX9101EntityList(List<TopLevelEntity> list) {
		m_entities = list;
	}

	@Override
	public SubmodelElementCollection toAasModel() {
		List<SubmodelElement> elements = FStream.from(m_entities)
												.map(this::toSubmodelElement)
												.toList();
		return new DefaultSubmodelElementCollection.Builder()
					.idShort(ENTITY_IDSHORT)
					.value(elements)
					.build();
	}

	@Override
	public void fromAasModel(SubmodelElement model) {
		Preconditions.checkArgument(model instanceof SubmodelElementCollection);
		
		m_entities = Lists.newArrayList();
		for ( SubmodelElement member: ((SubmodelElementCollection)model).getValue() ) {
			String id = member.getIdShort();
			SubmodelElementEntity adaptor = FACTORY.newInstance(id);
			adaptor.fromAasModel(member);
			if ( adaptor instanceof TopLevelEntity entity ) {
				m_entities.add(entity);
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format(ENTITY_IDSHORT);
	}
	
	private SubmodelElement toSubmodelElement(TopLevelEntity entity) {
		if ( entity instanceof SubmodelElementEntity adaptor ) {
			return adaptor.toAasModel();
		}
		else {
			String msg = String.format("AASModelEntity does not implement MDTSubmodelElement: entity=%s", entity);
			throw new IllegalArgumentException(msg);
		}
	}
}
