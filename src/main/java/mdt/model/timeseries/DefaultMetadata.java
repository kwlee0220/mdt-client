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
	private RecordMetadata record;	// 별도로 SubmodelElementCollection과의 변환을 처리해야 함.

	@Override
	public String getIdShort() {
		return "Metadata";
	}

	@Override
	public MultiLanguagePropertyValue getName() {
		return name;
	}
	
	public void setName(MultiLanguagePropertyValue mpv) {
		this.name = mpv;
	}

	@Override
	public MultiLanguagePropertyValue getDescription() {
		return description;
	}
	
	public void setDescription(MultiLanguagePropertyValue mpv) {
		this.description = mpv;
	}

	@Override
	public RecordMetadata getRecord() {
		return this.record;
	}

	@Override
	public void updateAasModel(SubmodelElement model) {
		super.updateAasModel(model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		List<SubmodelElement> fields = FStream.from(record.getFieldAll())
											    .map(this::toFieldProperty)
											    .castSafely(SubmodelElement.class)
											    .toList();
		SubmodelElementCollection recMeta = SubmodelUtils.newSubmodelElementCollection("Record", fields);
		smc.getValue().add(recMeta);
	}

	@Override
	public void updateFromAasModel(SubmodelElement model) {
		super.updateFromAasModel(model);
		
		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		SubmodelElementCollection recordSmc = SubmodelUtils.getFieldById(smc, "Record",
																		SubmodelElementCollection.class);
		List<Field> fields = FStream.from(recordSmc.getValue())
									.castSafely(Property.class)
									.map(this::parseField)
									.toList();
		this.record = new RecordMetadata(fields);
	}
	
	private Field parseField(Property prop) {
		return new Field(prop.getIdShort(), prop.getValueType());
	}
	
	private Property toFieldProperty(Field field) {
		return PropertyUtils.newProperty(field.getName(), field.getType().getTypeDefXsd());
	}
}