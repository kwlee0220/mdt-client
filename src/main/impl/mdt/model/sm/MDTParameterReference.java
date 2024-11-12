package mdt.model.sm;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import utils.InternalException;
import utils.Utilities;
import utils.func.Try;
import utils.func.Tuple;

import mdt.aas.DefaultSubmodelReference;
import mdt.model.ModelValidationException;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.ParameterCollection;
import mdt.model.service.SubmodelService;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Operation;
import mdt.model.sm.info.DefaultMDTInfo;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReference extends AbstractSubmodelElementReference
									implements MDTSubmodelElementReference {
	private final String m_instanceId;
	private final String m_parameterId;

	private volatile DefaultSubmodelElementReference m_ref;
	
	private MDTParameterReference(String instanceId, String parameterId) {
		Preconditions.checkArgument(parameterId != null && parameterId.trim().length() > 0);
		
		m_instanceId = instanceId;
		m_parameterId = parameterId;
	}

	@Override
	public boolean isActivated() {
		return m_ref != null;
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		activate(manager.getInstance(m_instanceId));
	}
	
	public MDTParameterReference activate(MDTInstance instance) {
		SubmodelService infoSvc = DefaultSubmodelReference.newInstance(instance, "InformationModel").get();
		
		DefaultMDTInfo mdtInfo = new DefaultMDTInfo();
		mdtInfo.updateFromAasModel(infoSvc.getSubmodelElementByPath("MDTInfo"));

		String assetType = mdtInfo.getAssetType();
		if ( assetType == null ) {
			throw new ModelValidationException("InformationModel.MDTInfo.AssertType is empty");
		}

		Class<? extends ParameterCollection> paramCollCls;
		String collName;
		if ( mdtInfo.getAssetType().equals("Machine") ) {
			paramCollCls = Equipment.class;
			collName = "Equipment";
		}
		else if ( mdtInfo.getAssetType().equals("Process") ) {
			paramCollCls = Operation.class;
			collName = "Operation";
		}
		else {
			throw new InternalException("Unknown AssetType: " + mdtInfo.getAssetType());
		}
		
		ParameterCollection paramColl = instance.getData()
												.getDataInfo()
												.getFirstSubmodelElementEntityByClass(paramCollCls);

		String submodelIdShort;
		if ( !m_parameterId.equals("*") ) {
			int paramIdx = Try.get(() -> paramColl.getParameterIndex(m_parameterId))
								.recover(failure -> Integer.parseInt(m_parameterId))
								.get();
			submodelIdShort = String.format("DataInfo.%s.%sParameterValues[%d].ParameterValue",
												collName, collName, paramIdx);
		}
		else {
			submodelIdShort = String.format("DataInfo.%s.%sParameterValues", collName, collName);
		}
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(instance, "Data");
		m_ref = DefaultSubmodelElementReference.newInstance(smRef, submodelIdShort);
		
		return this;
	}

	@Override
	public String getInstanceId() {
		return m_instanceId;
	}

	@Override
	public String getSubmodelIdShort() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getSubmodelIdShort();
	}

	@Override
	public String getElementIdShortPath() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getElementIdShortPath();
	}

	@Override
	public MDTInstance getInstance() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getInstance();
	}

	@Override
	public SubmodelService getSubmodelService() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getSubmodelService();
	}
	
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.read();
	}
	
	public Property getAsProperty() throws IOException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getAsProperty();
	}
	
	public void write(SubmodelElement sme) {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		m_ref.write(sme);
	}

	@Override
	public void update(SubmodelElement sme) throws IOException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		m_ref.update(ElementValues.getValue(sme));
	}

	@Override
	public void update(SubmodelElementValue value) throws ResourceNotFoundException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		m_ref.update(value);
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField("referenceType", SubmodelElementReferenceType.PARAMETER.name().toLowerCase());
		gen.writeStringField("instanceId", m_instanceId);
		gen.writeStringField("parameterId", m_parameterId);
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", m_instanceId, m_parameterId);
	}
	
	public static MDTParameterReference newInstance(String instanceId, String parameterId) {
		return new MDTParameterReference(instanceId, parameterId);
	}
	
	public static MDTParameterReference parseString(String refExpr) {
		Tuple<String,String> parts = Utilities.split(refExpr, '/');
		if ( parts == null ) {
			throw new IllegalArgumentException("invalid MDTParameterReference: " + refExpr);
		}
		
		return newInstance(parts._1, parts._2);
	}
	
	public static MDTParameterReference parseJson(ObjectNode topNode) {
		String mdtId = topNode.get("instanceId").asText();
		String parameterId = topNode.get("parameterId").asText();

		return new MDTParameterReference(mdtId, parameterId);
	}
}
