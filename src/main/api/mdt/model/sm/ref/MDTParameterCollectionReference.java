package mdt.model.sm.ref;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import utils.KeyValue;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.model.ModelValidationException;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.info.MDTAssetType;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTParameterCollectionReference extends SubmodelBasedElementReference implements MDTElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:param";
	private static final String FIELD_INSTANCE_ID = "instanceId";
	private static final String FIELD_PARAMETER_EXPR = "parameterExpr";
	
	private final String m_instanceId;

	private volatile DefaultElementReference m_ref;
	
	private MDTParameterCollectionReference(String instanceId) {
		Preconditions.checkArgument(instanceId != null, "instanceId is null");
		
		m_instanceId = instanceId;
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
			if ( sme instanceof SubmodelElementList sml ) {
				List<SubmodelElement> members = FStream.from(sml.getValue())
														.mapOrThrow(elm -> {
															var kv = toMemberElement(elm);
															kv.value().setIdShort(kv.key());
															return kv.value();
														})
														.toList();
				sml.setValue(members);
				return sml;
			}
			else {
				String msg = String.format("Invalid MDTParameterCollection read from %s (not SubmodelElementList): %s",
											toStringExpr(), sme);
				throw new IOException(msg);
			}
		}
		catch ( IOException e ) {
			String msg = String.format("Failed to read Parameter(%s), cause=%s", toStringExpr(), e.getMessage());
			throw new IOException(msg);
		}
	}
	private KeyValue<String,SubmodelElement> toMemberElement(SubmodelElement elm) throws IOException {
		if ( !(elm instanceof SubmodelElementCollection) ) {
			throw new IOException("Invalid MDTParameterCollection member element type: "
									+ elm.getClass().getName());
		}
		SubmodelElementCollection member = (SubmodelElementCollection)elm;
		
		String memberId = SubmodelUtils.getPropertyValueById(member, "ParameterID").value();
		SubmodelElement memberElm = SubmodelUtils.getFieldById(member, "ParameterValue").value();
		return KeyValue.of(memberId, memberElm);
	}
	
	@Override
	public ElementValue readValue() throws IOException {
		assertActivated();
		
		ElementValue smev = m_ref.readValue();
		if ( smev instanceof ElementListValue smelv ) {
			LinkedHashMap<String,ElementValue> paramValues = Maps.newLinkedHashMap();
			for ( ElementValue member : smelv.getElementAll() ) {
				KeyValue<String,ElementValue> kv = toMemberValue(member);
				paramValues.put(kv.key(), kv.value());
			}
			
			return new ElementCollectionValue(paramValues);
		}
		else {
			String msg = String.format("Invalid MDTParameterCollection value read from %s (not List): %s",
										toStringExpr(), smev);
			throw new IOException(msg);
		}
	}
	private KeyValue<String,ElementValue> toMemberValue(ElementValue member) throws IOException {
		if ( !(member instanceof ElementCollectionValue) ) {
			throw new IOException("Invalid MDTParameterCollection member value type: "
									+ member.getClass().getName());
		}
		ElementCollectionValue cv = (ElementCollectionValue)member;
		
		PropertyValue<String> pidValue = (PropertyValue<String>)cv.getField("ParameterID");
		if ( !(pidValue instanceof PropertyValue) ) {
			throw new IOException(String.format("Invalid ParameterID value type: %s", pidValue.getClass().getName()));
		}
		
		String paramId = (String)pidValue.get();
		return KeyValue.of(paramId, cv.getField("ParameterValue"));
	}
	
	@Override
	public void write(SubmodelElement sme) throws IOException {
		Preconditions.checkArgument(sme != null && sme instanceof SubmodelElementList,
									"smev should be a SubmodelElementList: %s", sme);

		LinkedHashMap<String,SubmodelElement> valueMap
								= FStream.from(((SubmodelElementList)sme).getValue())
										.mapOrThrow(elm -> KeyValue.of(elm.getIdShort(), elm))
										.mapToKeyValue(kv -> kv)
										.toMap(Maps.newLinkedHashMap());
		
		SubmodelElementList paramValues = (SubmodelElementList)m_ref.read();
		FStream.from(paramValues.getValue())
				.forEach(paramValue -> {
					String memberId = SubmodelUtils.getPropertyValueById(paramValue, "ParameterID").value();
					SubmodelElement newValue = valueMap.get(memberId);
					newValue.setIdShort("ParameterValue");
					SubmodelUtils.replaceFieldbyId((SubmodelElementCollection)paramValue, "ParameterValue", newValue);
				});
		m_ref.write(paramValues);
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		Preconditions.checkArgument(smev != null && smev instanceof ElementCollectionValue,
									"smev should be a ElementCollectionValue: %s", smev);
		assertActivated();
		
		List<ElementValue> paramValues = KeyValueFStream.from(((ElementCollectionValue)smev).getFieldAll())
														.map(kv -> {
															String paramId = kv.key();
															ElementValue paramValue = kv.value();
															
															var v = Map.of("ParameterID", PropertyValue.STRING(paramId),
																			"ParameterValue", paramValue);
															return (ElementValue)new ElementCollectionValue(v);
														})
														.toList();
		ElementListValue expandedValue = new ElementListValue(paramValues);
		m_ref.updateValue(expandedValue);
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		Preconditions.checkArgument(valueJsonString != null, "valueJsonString is null");
		assertActivated();
		
		SubmodelElement elm = read();
		ElementValues.updateWithValueJsonString(elm, valueJsonString);
		write(elm);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStringField(FIELD_INSTANCE_ID, m_instanceId);
		gen.writeStringField(FIELD_PARAMETER_EXPR, "*");
	}
	
	public static MDTParameterCollectionReference deserializeFields(JsonNode jnode) {
		String instanceId = jnode.get(FIELD_INSTANCE_ID).asText();
		String parameterExpr = jnode.get(FIELD_PARAMETER_EXPR).asText();
		Preconditions.checkArgument(parameterExpr.equals("*"),
										"invalid parameterExpr=%s for MDTParameterCollectionReference", parameterExpr);

		return new MDTParameterCollectionReference(instanceId);
	}

	@Override
	public String toStringExpr() {
		return String.format("param:%s:*", m_instanceId);
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
		if ( obj == null || !(obj instanceof MDTParameterCollectionReference) ) {
			return false;
		}

		MDTParameterCollectionReference other = (MDTParameterCollectionReference) obj;
		return m_instanceId.equals(other.m_instanceId);
	}
	
	public static MDTParameterCollectionReference newInstance(String instanceId) {
		return new MDTParameterCollectionReference(instanceId);
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
		
		return String.format("DataInfo.%s.%sParameterValues", assetTypeName, assetTypeName);
	}
}
