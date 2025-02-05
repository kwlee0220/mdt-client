package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import utils.InternalException;
import utils.Utilities;
import utils.func.Funcs;
import utils.func.Tuple;
import utils.stream.FStream;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.ModelValidationException;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReference extends AbstractElementReference
									implements MDTElementReference {
	private static final String ALL = "*";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_PARAMETER_ID = "parameterId";
	
	private final String m_instanceId;
	private final String m_parameterId;

	private volatile DefaultElementReference m_ref;
	
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
		// InformationModel의 MDTInfo에서 assetType을 활용하여
		// MDTInstance가 Equipment인지 Operation인지를 확인한다.
		InformationModel infoModel = instance.getInformationModel();
		
		String assetType = infoModel.getMDTInfo().getAssetType();
		if ( assetType == null ) {
			throw new ModelValidationException("InformationModel.MDTInfo.AssertType is empty");
		}

		Class<? extends ParameterCollection> paramCollCls;
		String collName;
		if ( assetType.equals("Machine") ) {
			paramCollCls = Equipment.class;
			collName = "Equipment";
		}
		else if ( assetType.equals("Process") ) {
			paramCollCls = Operation.class;
			collName = "Operation";
		}
		else {
			throw new InternalException("Unknown AssetType: " + assetType);
		}
		
		SubmodelDescriptor dataSmDesc
			= Funcs.getFirst(instance.getSubmodelDescriptorAllBySemanticId(Data.SEMANTIC_ID))
					.getOrThrow(() -> new ResourceNotFoundException("Submodel", "semanticId=" + Data.SEMANTIC_ID));
		
		String submodelIdShort = null;
		if ( m_parameterId.equals(ALL) ) {
			submodelIdShort = String.format("DataInfo.%s.%sParameterValues", collName, collName);
		}
		else {
			int paramIdx;
			try {
				paramIdx = Integer.parseInt(m_parameterId);
			}
			catch ( NumberFormatException e ) {
				// Parameter id가 숫자가 아닌 경우에는 parameter 식별자로 간주하고
				// 해당 식별자의 Parameter를 찾는다.
				ParameterCollection paramColl = instance.getData()
														.getDataInfo()
														.getFirstSubmodelElementEntityByClass(paramCollCls);
				paramIdx = paramColl.getParameterIndex(m_parameterId);
			}
			submodelIdShort = String.format("DataInfo.%s.%sParameterValues[%d].ParameterValue",
													collName, collName, paramIdx);
		}
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.newInstance(instance, dataSmDesc.getIdShort());
		m_ref = DefaultElementReference.newInstance(smRef, submodelIdShort);
		
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
	public String getElementPath() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getElementPath();
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
		
		SubmodelElement sme = m_ref.read();
		if ( m_parameterId.equals(ALL) ) {
			List<SubmodelElement> paramValueSmeList
					= FStream.from(((SubmodelElementList)sme).getValue())
								.castSafely(SubmodelElementCollection.class)
								.map(smc -> {
									String id = ((Property)(SubmodelUtils.getFieldById(smc, "ParameterID").value())).getValue();
									SubmodelElement value = SubmodelUtils.getFieldById(smc, "ParameterValue").value();
									value.setIdShort(id);
									return value;
								})
								.toList();
			return new DefaultSubmodelElementList.Builder()
							.idShort("Parameters")
							.value(paramValueSmeList)
							.build();
		}
		else {
			return sme;
		}
	}
	
	public void write(SubmodelElement sme) {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		m_ref.write(sme);
	}

	@Override
	public SubmodelElement update(SubmodelElement sme) throws IOException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.update(ElementValues.getValue(sme));
	}

	@Override
	public SubmodelElement update(SubmodelElementValue value) throws ResourceNotFoundException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.update(value);
	}

	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_REFERENCE_TYPE, ElementReferenceType.PARAMETER.getCode());
		gen.writeStringField(FIELD_INSTANCE_ID, m_instanceId);
		gen.writeStringField(FIELD_PARAMETER_ID, m_parameterId);
		gen.writeEndObject();
	}
	
	@Override
	public String toString() {
		return String.format("param:%s/%s", m_instanceId, m_parameterId);
	}
	
	public static MDTParameterReference newInstance(String instanceId, String parameterId) {
		return new MDTParameterReference(instanceId, parameterId);
	}
	
	public static MDTParameterReference parseString(String refExpr) {
		// refExpr: "<instanceId>/<parameterId>"
		Tuple<String,String> parts = Utilities.split(refExpr, '/');
		if ( parts == null ) {
			throw new IllegalArgumentException("invalid MDTParameterReference: " + refExpr);
		}
		
		return newInstance(parts._1, parts._2);
	}
	
	public static MDTParameterReference parseJson(ObjectNode topNode) {
		String mdtId = topNode.get(FIELD_INSTANCE_ID).asText();
		String parameterId = topNode.get(FIELD_PARAMETER_ID).asText();

		return new MDTParameterReference(mdtId, parameterId);
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "IncAmount");
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsInt());
		
		String json = ref.toJsonString();
		System.out.println(json);
		
		MDTElementReference ref2 = (MDTElementReference)ElementReferenceUtils.parseJsonString(json);
		ref2.activate(manager);
		System.out.println(ref2.readValue());
	}
}
