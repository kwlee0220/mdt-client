package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import utils.stream.FStream;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.ModelValidationException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTModelService;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.data.Equipment;
import mdt.model.sm.data.Operation;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.info.MDTAssetType;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReference extends SubmodelBasedElementReference implements MDTElementReference {
	private static final String ALL = "*";
	public static final String SERIALIZATION_TYPE = "mdt:ref:param";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_PARAMETER_ID = "parameterId";
	
	private final String m_instanceId;
	private final String m_parameterId;

	private volatile DefaultElementReference m_ref;
	
	private MDTParameterReference(String instanceId, String parameterId) {
		Preconditions.checkArgument(instanceId != null, "instanceId is null");
		Preconditions.checkArgument(parameterId != null, "parameterId is null");
		
		m_instanceId = instanceId;
		m_parameterId = parameterId;
	}

	@Override
	public boolean isActivated() {
		return m_ref != null;
	}

	@Override
	public void activate(MDTInstanceManager manager) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(m_instanceId, "Data");
		String idShortPath = buildIdShortPath(manager);
		m_ref = DefaultElementReference.newInstance(smRef, idShortPath);
		m_ref.activate(manager);
	}

	@Override
	public String getInstanceId() {
		return m_instanceId;
	}

	@Override
	public MDTInstance getInstance() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getInstance();
	}
	
	public String getParameterId() {
		return m_parameterId;
	}

	@Override
	public String getIdShortPathString() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getIdShortPathString();
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

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStringField(FIELD_INSTANCE_ID, m_instanceId);
		gen.writeStringField(FIELD_PARAMETER_ID, m_parameterId);
	}
	
	public static MDTParameterReference deserializeFields(JsonNode jnode) {
		String instanceId = jnode.get(FIELD_INSTANCE_ID).asText();
		String parameterId = jnode.get(FIELD_PARAMETER_ID).asText();

		return new MDTParameterReference(instanceId, parameterId);
	}

	@Override
	public String toStringExpr() {
		return String.format("param:%s:%s", m_instanceId, m_parameterId);
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	public static MDTParameterReference newInstance(String instanceId, String parameterId) {
		return new MDTParameterReference(instanceId, parameterId);
	}
	
	private String buildIdShortPath(MDTInstanceManager manager) {
		MDTInstance instance = manager.getInstance(m_instanceId);
		MDTModelService mdtInfo =  MDTModelService.of(instance);
		
		// InformationModel의 MDTInfo에서 assetType을 활용하여
		// MDTInstance가 Equipment인지 Operation인지를 확인한다.
		InformationModel infoModel = mdtInfo.getInformationModel();
		
		MDTAssetType assetType = infoModel.getMDTInfo().getAssetType();
		if ( assetType == null ) {
			throw new ModelValidationException("InformationModel.MDTInfo.AssertType is empty");
		}
	
		Class<? extends ParameterCollection> paramCollCls;
		String collName;
		switch ( assetType ) {
			case Machine:
				paramCollCls = Equipment.class;
				collName = "Equipment";
				break;
			case Process:
				paramCollCls = Operation.class;
				collName = "Operation";
				break;
			default:
				throw new IllegalArgumentException("MDTParameter is not supported for assetType: " + assetType);
		}
		
		String idShortPath = null;
		if ( m_parameterId.equals(ALL) ) {
			idShortPath = String.format("DataInfo.%s.%sParameterValues", collName, collName);
		}
		else {
			int paramIdx;
			try {
				paramIdx = Integer.parseInt(m_parameterId);
			}
			catch ( NumberFormatException e ) {
				// Parameter id가 숫자가 아닌 경우에는 parameter 식별자로 간주하고
				// 해당 식별자의 Parameter를 찾는다.
				ParameterCollection paramColl = mdtInfo.getData()
														.getDataInfo()
														.getFirstSubmodelElementEntityByClass(paramCollCls);
				paramIdx = paramColl.getParameterIndex(m_parameterId);
			}
			idShortPath = String.format("DataInfo.%s.%sParameterValues[%d].ParameterValue",
													collName, collName, paramIdx);
		}
	
		return idShortPath;
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		MDTParameterReference ref = MDTParameterReference.newInstance("test", "IncAmount");
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsInt());
		
		String json = ref.toJsonString();
		System.out.println(json);
		
		MDTElementReference ref2 = (MDTElementReference)ElementReferences.parseJsonString(json);
		ref2.activate(manager);
		System.out.println(ref2.readValue());
	}
}
