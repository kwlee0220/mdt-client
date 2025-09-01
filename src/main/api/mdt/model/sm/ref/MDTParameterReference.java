package mdt.model.sm.ref;

import java.io.IOException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

import mdt.model.ModelValidationException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.data.ParameterCollection;
import mdt.model.sm.info.MDTAssetType;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterReference extends SubmodelBasedElementReference implements MDTElementReference {
	private static final String ALL = "*";
	public static final String SERIALIZATION_TYPE = "mdt:ref:param";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_PARAMETER_EXPR = "parameterExpr";
	
	private final String m_instanceId;
	private final String m_parameterExpr;

	private volatile DefaultElementReference m_ref;
	private volatile SubmodelElement m_proto = null;
	
	private MDTParameterReference(String instanceId, String parameterExpr) {
		Preconditions.checkArgument(instanceId != null, "instanceId is null");
		Preconditions.checkArgument(parameterExpr != null, "parameterExpr is null");
		
		m_instanceId = instanceId;
		m_parameterExpr = parameterExpr;
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
	}

	@Override
	public String getInstanceId() {
		return m_instanceId;
	}

	@Override
	public MDTInstance getInstance() {
		assertActivated();
		
		return m_ref.getInstance();
	}
	
	public String getParameterExpr() {
		return m_parameterExpr;
	}

	@Override
	public String getIdShortPathString() {
		assertActivated();
		
		return m_ref.getIdShortPathString();
	}

	@Override
	public SubmodelService getSubmodelService() {
		assertActivated();
		
		return m_ref.getSubmodelService();
	}

	@Override
	public MDTSubmodelReference getSubmodelReference() {
		assertActivated();
		
		return m_ref.getSubmodelReference();
	}
	
	public SubmodelElement read() throws IOException {
		assertActivated();
		
		try {
			SubmodelElement sme = m_ref.read();
			if ( m_proto == null ) {
				m_proto = sme;
			}
			return sme;
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to read Parameter(%s), cause=%s", toStringExpr(), e.getMessage());
			throw new IOException(msg);
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
		assertActivated();
		
		m_ref.updateValue(smev);
	}

	@Override
	public void updateWithValueJsonString(String valueJsonString) throws IOException {
		assertActivated();
		
		// 해당 SubmodelElement의 구조를 알기 위해 prototype 객체를 활용한다.
		if ( m_proto == null ) {
			m_proto = read();
		}
		
		ElementValue newVal = ElementValues.parseValueJsonString(m_proto, valueJsonString);;
		updateValue(newVal);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStringField(FIELD_INSTANCE_ID, m_instanceId);
		gen.writeStringField(FIELD_PARAMETER_EXPR, m_parameterExpr);
	}
	
	public static MDTParameterReference deserializeFields(JsonNode jnode) {
		String instanceId = jnode.get(FIELD_INSTANCE_ID).asText();
		String parameterExpr = jnode.get(FIELD_PARAMETER_EXPR).asText();

		return new MDTParameterReference(instanceId, parameterExpr);
	}

	@Override
	public String toStringExpr() {
		return String.format("param:%s:%s", m_instanceId, m_parameterExpr);
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
				&& m_parameterExpr.equals(other.m_parameterExpr);
	}
	
	public static MDTParameterReference newInstance(String instanceId, String parameterExpr) {
		return new MDTParameterReference(instanceId, parameterExpr);
	}
	
	private void assertActivated() {
		Preconditions.checkState(m_ref != null,
								"MDTParameterReference(%s) is not activated", toStringExpr());
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

		// TODO: 새로운 수정
		String paramId = null;
		String subPath = "";
		int paramIdx;
		try {
			// 일단 parmeter-id가 숫자인 것으로 가정하고 파싱을 실시하여
			// parameter의 idShortPath를 생성하고, 숫자가 아니어서 예외가 발생한 경우에는
			// 일반적인 id 기반의 idShortPath를 생성한다.
			paramIdx = Integer.parseInt(m_parameterExpr);
		}
		catch ( NumberFormatException e ) {
			// parameter expr이 단일 parameter의 이름으로 구성되지 않고,
			// path로 구성될 수도 있기 때문에 paramter expr에 '.' 또는 '['가 포함되는지를 확인한다.
			// 만일 path인 경우에는 가장 첫번째 path segment를 'ParameterValue'로 치환시켜서
			// parameter의 idShortPath를 구성한다.
			int idx = m_parameterExpr.indexOf('.');
			if ( idx >= 0 ) {
				paramId = m_parameterExpr.substring(0, idx);
				subPath = m_parameterExpr.substring(idx);
			}
			else {
				idx = m_parameterExpr.indexOf('[');
				if ( idx >= 0 ) {
					paramId = m_parameterExpr.substring(0, idx);
					subPath = m_parameterExpr.substring(idx);
				}
				else {
					paramId = m_parameterExpr;
				}
			}
			
			// Parameter id가 숫자가 아닌 경우에는 parameter 식별자로 간주하고
			// 해당 식별자의 Parameter를 찾는다.
			ParameterCollection paramColl = instance.getParameterCollection();
			paramIdx = paramColl.getParameterIndex(paramId);
		}
		// TODO: 새로운 수정
		return String.format("%s[%d].ParameterValue%s",
							paramCollPathPrefix, paramIdx, subPath);
	}
}
