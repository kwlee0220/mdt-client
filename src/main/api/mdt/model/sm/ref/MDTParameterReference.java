package mdt.model.sm.ref;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import utils.func.FOption;
import utils.json.JacksonUtils;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.ModelValidationException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.ParameterValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReference extends SubmodelBasedElementReference implements MDTElementReference {
	private static final String ALL = "*";
	public static final String SERIALIZATION_TYPE = "mdt:ref:param";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_PARAMETER_ID = "parameterId";
	private static final String FIELD_SUB_PATH = "subPath";
	
	private final String m_instanceId;
	private final String m_parameterId;
	private final String m_subPath;

	private volatile DefaultElementReference m_ref;
	private volatile SubmodelElement m_proto;
	
	private MDTParameterReference(String instanceId, String parameterId, String subPath) {
		Preconditions.checkArgument(instanceId != null, "instanceId is null");
		Preconditions.checkArgument(parameterId != null, "parameterId is null");
		Preconditions.checkArgument(!parameterId.equals(ALL) || subPath == null || subPath.length() > 0,
									"parameterId is empty");
		
		m_instanceId = instanceId;
		m_parameterId = parameterId;
		m_subPath = subPath;
	}

	@Override
	public boolean isActivated() {
		return m_ref != null;
	}

	@Override
	public void activate(MDTInstanceManager manager) {
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort(m_instanceId, "Data");
		
		MDTInstance instance = manager.getInstance(m_instanceId);
		String idShortPath = buildIdShortPath(instance);
		
		m_ref = DefaultElementReference.newInstance(smRef, idShortPath);
		m_ref.activate(manager);
		try {
			m_proto = m_ref.read();
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to read parameter prototype: instanceId=%s, parameterId=%s, subPath=%s, "
										+ "cause=%s",
										m_instanceId, m_parameterId, m_subPath, ""+e);
			throw new UncheckedIOException(msg, e);
		}
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
	
	public String getSubPath() {
		return m_subPath;
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

	@Override
	public MDTSubmodelReference getSubmodelReference() {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		return m_ref.getSubmodelReference();
	}
	
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(m_ref != null, "MDTParameterReference is not activated");
		
		SubmodelElement sme = m_ref.read();
		if ( m_parameterId.equals(ALL) ) {
			List<SubmodelElement> paramValueSmeList
					= FStream.from(((SubmodelElementList)sme).getValue())
								.castSafely(SubmodelElementCollection.class)
								.map(smc -> {
									String id = SubmodelUtils.getPropertyById(smc, "ParameterID").value().getValue();
									smc.setIdShort(id);
									return (SubmodelElement)smc;
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
	public void write(SubmodelElement sme) throws IOException {
		ElementValue smev = ElementValues.getValue(sme);
		updateValue(smev);
//		throw new UnsupportedOperationException("Cannot update MDTParameter entirely: ref=" + this);
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		SubmodelService service = getSubmodelService();
		
		// smev의 타입이 ElementCollectionValue가 아닌 경우에는 Parameter의 value 부분만
		// 갱신하는 것으로 간주하고, EventDateTime을 추가하여 ElementCollectionValue로 변환한다.
		// 만일 smev의 타입이 ElementCollectionValue인 경우에는
		// 호출자가 Parameter 값 전체를 갱신하려는 것인지 value 부분만 갱신하려는 것인지
		// 판단하기 어려워 주어진 smev를 그대로 사용한다.
		// TODO: 물론 이것은 나중에 문제를 읽으킬 여지가 있다.
		if ( smev instanceof ElementCollectionValue || m_subPath != null ) {
			service.updateSubmodelElementValueByPath(getIdShortPathString(), smev);
		}
		else {
			ParameterValue paramValue = ParameterValue.builder()
														.value(smev)
														.eventDateTime(Instant.now())
														.build();
			service.updateSubmodelElementValueByPath(getIdShortPathString(), paramValue);
		}
	}

	@Override
	public void updateWithValueJsonString(String valueJsonString) throws IOException {
		ElementValue newVal = null;
		try {
			newVal = ElementValues.parseValueJsonString(m_proto, valueJsonString);
		}
		catch ( IOException e ) {
			JsonNode jnode = MDTModelSerDe.getJsonMapper().readTree(valueJsonString);
			if ( jnode.isValueNode() && m_proto instanceof SubmodelElementCollection ) {
				Property valProp = SubmodelUtils.getPropertyById((SubmodelElementCollection)m_proto, "ParameterValue").value();
				newVal = ElementValues.parseValueJsonNode(valProp, jnode);
				newVal = ParameterValue.builder()
										.value(newVal)
										.eventDateTime(Instant.now())
										.build();
			}
			else {
				throw e;
			}
		}
		updateValue(newVal);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStringField(FIELD_INSTANCE_ID, m_instanceId);
		gen.writeStringField(FIELD_PARAMETER_ID, m_parameterId);
		if ( m_subPath != null ) {
			gen.writeStringField(FIELD_SUB_PATH, m_subPath);
		}
	}
	
	public static MDTParameterReference deserializeFields(JsonNode jnode) {
		String instanceId = jnode.get(FIELD_INSTANCE_ID).asText();
		String parameterId = jnode.get(FIELD_PARAMETER_ID).asText();
		String subPath = JacksonUtils.getStringFieldOrNull(jnode, FIELD_SUB_PATH);

		return new MDTParameterReference(instanceId, parameterId, subPath);
	}

	@Override
	public String toStringExpr() {
		String subPathStr = FOption.mapOrElse(m_subPath, p -> ":" + p, "");
		return String.format("param:%s:%s%s", m_instanceId, m_parameterId, subPathStr);
	}
	
	@Override
	public String toString() {
		String actStr = isActivated() ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || !(obj instanceof MDTParameterReference) ) {
			return false;
		}

		MDTParameterReference other = (MDTParameterReference) obj;
		return m_instanceId.equals(other.m_instanceId)
				&& m_parameterId.equals(other.m_parameterId)
				&& Objects.equals(m_subPath, other.m_subPath);
	}
	
	public static MDTParameterReference newInstance(String instanceId, String parameterId, String subPath) {
		return new MDTParameterReference(instanceId, parameterId, subPath);
	}
	public static MDTParameterReference newInstance(String instanceId, String parameterId) {
		return new MDTParameterReference(instanceId, parameterId, null);
	}
	
	private String buildIdShortPath(MDTInstance instance) {
		MDTAssetType assetType = instance.getAssetType();
		if ( assetType == null ) {
			throw new ModelValidationException("AssertType is empty");
		}
	
		String assetTypeName = switch ( assetType ) {
			case Machine -> "Equipment";
            case Process -> "Operation";
            default -> throw new IllegalArgumentException("MDTParameter is not supported for assetType: " + assetType);
		};
		String paramCollPathPrefix = String.format("DataInfo.%s.%sParameterValues", assetTypeName, assetTypeName);
		
		String idShortPath = null;
		if ( m_parameterId.equals(ALL) ) {
			idShortPath = paramCollPathPrefix;
		}
		else {
			int paramIdx;
			try {
				paramIdx = Integer.parseInt(m_parameterId);
			}
			catch ( NumberFormatException e ) {
				// Parameter id가 숫자가 아닌 경우에는 parameter 식별자로 간주하고
				// 해당 식별자의 Parameter를 찾는다.
				ParameterCollection paramColl = instance.getParameterCollection();
				paramIdx = paramColl.getParameterIndex(m_parameterId);
			}
			idShortPath = String.format("%s[%d]", paramCollPathPrefix, paramIdx);
			
			if ( m_subPath != null && m_subPath.length() > 0 ) {
				idShortPath += "." + m_subPath;
			}
		}
	
		return idShortPath;
	}
}
