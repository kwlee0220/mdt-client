package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.stream.FStream;

import mdt.model.sm.PropertyUtils;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.entity.MultiLanguagePropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;
import mdt.model.sm.value.MultiLanguagePropertyValue;
import mdt.model.timeseries.RecordMetadata.Field;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultMetadata extends SubmodelElementCollectionEntity implements Metadata {
	@MultiLanguagePropertyField(idShort="Name") private MultiLanguagePropertyValue name;
	@MultiLanguagePropertyField(idShort="Description") private MultiLanguagePropertyValue description;
	private RecordMetadata m_metadata;

	@Override
	public String getIdShort() {
		return "Metadata";
	}

	@Override
	public MultiLanguagePropertyValue getName() {
		return name;
	}

	@Override
	public MultiLanguagePropertyValue getDescription() {
		return description;
	}

	@Override
	public RecordMetadata getRecordMetadata() {
		return m_metadata;
	}

	@Override
	public void updateAasModel(SubmodelElement model) {
		super.updateAasModel(model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		List<SubmodelElement> fields = FStream.from(m_metadata.getFieldAll())
											    .map(this::toFieldProperty)
											    .castSafely(SubmodelElement.class)
											    .toList();
		SubmodelElementCollection recMeta = SubmodelUtils.newSubmodelElementCollection("RecordMetadata", fields);
		smc.getValue().add(recMeta);
	}

	@Override
	public void updateFromAasModel(SubmodelElement model) {
		super.updateFromAasModel(model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		SubmodelElementCollection recordSmc = SubmodelUtils.getFieldById(smc, "RecordMetadata",
																		SubmodelElementCollection.class).value();
		List<Field> fields = FStream.from(recordSmc.getValue())
									.castSafely(Property.class)
									.map(this::parseField)
									.toList();
		m_metadata = new RecordMetadata(fields);
	}
	
	private Field parseField(Property prop) {
		return new Field(prop.getIdShort(), prop.getValueType());
	}
	
	private Property toFieldProperty(Field field) {
		return PropertyUtils.newProperty(field.getName(), field.getType().getTypeDefXsd());
	}
}