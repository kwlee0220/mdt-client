package mdt.model.sm.ref;

import java.io.IOException;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import utils.Indexed;
import utils.stream.FStream;

import mdt.client.HttpMDTManager;
import mdt.client.instance.HttpMDTInstanceManager;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceNotFoundException;
import mdt.model.SubmodelService;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ai.AI;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdShortSubmodelReference;
import mdt.model.sm.ref.DefaultSubmodelReference.ByIdSubmodelReference;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.IdShortPath;


/**
 * MDT 연산에 사용되는 입/출력 인자의 reference를 정의하는 인터페이스이다.
 * <p>
 * 입/출력 연산 인자의 reference는 다음과 같이 구성된다.
 * <ul>
 *     <li>instanceId: 연산을 포함하는 인스턴스의 ID
 *     <li>submodelIdShort: 연산을 포함하는 서브모델의 ID
 *     <li>kind: 인자의 종류 (입력 또는 출력)
 *     <li>argSpec: 인자의 명세. 인자의 이름 또는 인덱스 번호이거나 '*' (모든 인자)이다.
 * </ul>
 * <p>
 * 인자의 값을 읽거나 쓰기 위해서는 {@link #activate(MDTInstanceManager)}를 호출하여
 * 인자의 reference를 활성화해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTArgumentReference extends SubmodelBasedElementReference implements MDTElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:oparg";
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_ARG_SPEC = "argumentSpec";
	private static final String ALL_ARGS = "*";
	
	private final DefaultSubmodelReference m_submodelRef;
	private final MDTArgumentKind m_kind;
	private final String m_argSpec;
	
	private volatile DefaultElementReference m_argRef;
	
	private MDTArgumentReference(DefaultSubmodelReference submodelRef, MDTArgumentKind kind, String argSpec) {
		Preconditions.checkArgument(submodelRef != null, "Null OperationSubmodelReference");
		Preconditions.checkArgument(argSpec != null, "Null argSpec");
		
		m_submodelRef = submodelRef;
		m_kind = kind;
		m_argSpec = argSpec;
	}

	@Override
	public String getInstanceId() {
		return m_submodelRef.getInstanceId();
	}

	@Override
	public boolean isActivated() {
		return m_argRef != null;
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_submodelRef.activate(manager);
		
		String elementPath = buildIdShortPath();
		m_argRef = DefaultElementReference.newInstance(m_submodelRef, elementPath);
	}

	@Override
	public MDTInstance getInstance() {
		return m_submodelRef.getInstance();
	}
	
	public MDTArgumentKind getKind() {
		return m_kind;
	}
	
	public String getArgumentSpec() {
		return m_argSpec;
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_submodelRef.get();
	}

	@Override
	public String getIdShortPathString() {
		Preconditions.checkState(m_argRef != null, "not activated");
		
		return m_argRef.getIdShortPathString();
	}

	@Override
	public IdShortPath getIdShortPath() {
		Preconditions.checkState(m_argRef != null, "not activated");
		
		return m_argRef.getIdShortPath();
	}

	@Override
	public SubmodelElement read() throws IOException {
		Preconditions.checkState(m_argRef != null, "not activated");
		
		SubmodelElement argValue = m_argRef.read();
		if ( !m_argSpec.equals(ALL_ARGS) ) {
			return argValue;
		}

		String argKindStr = switch ( m_kind ) {
			case INPUT -> "Input";
			case OUTPUT -> "Output";
		};
		String idField = String.format("%sID", argKindStr);
		String valueField = String.format("%sValue", argKindStr);
		List<SubmodelElement> argList = FStream.from(((SubmodelElementList)argValue).getValue())
												.castSafely(SubmodelElementCollection.class)
												.map(smc -> {
													String argName = SubmodelUtils.getFieldById(smc, idField, Property.class)
																					.value().getValue();
													SubmodelElement field = SubmodelUtils.findFieldById(smc, valueField)
																							.map(Indexed::value)
																							.getOrNull();
													field.setIdShort(argName);
													return field;
												})
												.toList();
		String idShort = String.format("%sArguments", argKindStr);
		return new DefaultSubmodelElementList.Builder().idShort(idShort).value(argList).build();
	}

	@Override
	public void write(SubmodelElement newElm) throws IOException {
		Preconditions.checkState(m_argRef != null, "not activated");
		
		m_argRef.write(newElm);
	}

	@Override
	public SubmodelElement updateValue(ElementValue smev) throws IOException {
		Preconditions.checkState(m_argRef != null, "not activated");
		
		return m_argRef.updateValue(smev);
	}
	
	public MDTSubmodelReference getSubmodelReference() {
		return m_submodelRef;
	}

	@Override
	public String toStringExpr() {
		String kindStr = switch ( m_kind ) {
			case INPUT -> "in";
			case OUTPUT -> "out";
		};
		return "oparg:" + m_submodelRef.toStringExpr() + ":" + kindStr + ":" + m_argSpec;
	}
	
	@Override
	public String toString() {
		String actStr = ( m_argRef != null ) ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || !(obj instanceof MDTArgumentReference) ) {
			return false;
		}

		MDTArgumentReference other = (MDTArgumentReference) obj;
		return Objects.equals(m_submodelRef, other.m_submodelRef)
				&& Objects.equals(m_kind, other.m_kind)
				&& Objects.equals(m_argSpec, other.m_argSpec);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private DefaultSubmodelReference m_submodelRef;
		private MDTArgumentKind m_kind;
		private String m_argSpec;

		public MDTArgumentReference build() {
			return MDTArgumentReference.newInstance(m_submodelRef, m_kind, m_argSpec);
		}
		
		public Builder submodelReference(DefaultSubmodelReference smRef) {
			m_submodelRef = smRef;
			return this;
		}
		
		public Builder kind(MDTArgumentKind kind) {
			m_kind = kind;
			return this;
		}

		public Builder argument(String argSpec) {
			m_argSpec = argSpec;
			return this;
		}
	}
	
	public static MDTArgumentReference newInstance(DefaultSubmodelReference smRef, MDTArgumentKind kind,
													String argSpec) {
		return new MDTArgumentReference(smRef, kind, argSpec);
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeObjectField(FIELD_SUBMODEL_REF, getSubmodelReference());
		gen.writeStringField(FIELD_KIND, m_kind.name().toLowerCase());
		gen.writeStringField(FIELD_ARG_SPEC, m_argSpec);
	}
	
	public static MDTArgumentReference deserializeFields(JsonNode jnode) throws IOException {
		JsonNode smRefNode = jnode.get(FIELD_SUBMODEL_REF);
		DefaultSubmodelReference smRef = MDTModelSerDe.readValue(smRefNode, DefaultSubmodelReference.class);
		
		String kindStr = jnode.get(FIELD_KIND).asText();
		String argSpec = jnode.get(FIELD_ARG_SPEC).asText();
		
		return MDTArgumentReference.newInstance(smRef, MDTArgumentKind.fromString(kindStr), argSpec);
	}
	
	private String buildIdShortPath() {
		List<InstanceSubmodelDescriptor> ismDescList = m_submodelRef.getInstance().getInstanceSubmodelDescriptorAll();
		InstanceSubmodelDescriptor found = null;
		if ( m_submodelRef instanceof ByIdSubmodelReference byId ) {
			found = FStream.from(ismDescList)
							.findFirst(ism -> byId.getSubmodelId().equals(ism.getId()))
							.getOrThrow(() -> new ResourceNotFoundException("MDTArgument", "id=" + byId.getSubmodelId()));
        }
		else if ( m_submodelRef instanceof ByIdShortSubmodelReference byIdShort ) {
			found = FStream.from(ismDescList)
							.findFirst(ism -> byIdShort.getSubmodelIdShort().equals(ism.getIdShort()))
							.getOrThrow(() -> new ResourceNotFoundException("MDTArgument",
																		"idShort=" + byIdShort.getSubmodelIdShort()));
		}
		else {
			throw new IllegalArgumentException("Unknown submodel reference: " + m_submodelRef);
		}
		
		String opType = switch ( found.getSemanticId() ) {
			case Simulation.SEMANTIC_ID -> "Simulation";
			case AI.SEMANTIC_ID -> "AI";
			default -> throw new IllegalArgumentException("Unknown submodel semanticId: " + found.getSemanticId());
		};
		String argKindStr = switch ( m_kind ) {
			case INPUT -> "Input";
			case OUTPUT -> "Output";
		};
		String pathPrefix = String.format("%sInfo.%ss", opType, argKindStr, argKindStr);
		
		if ( m_argSpec.equals(ALL_ARGS) ) {
			return pathPrefix;
		}
		
		int argIndex = -1;
		try {
			argIndex = Integer.parseInt(m_argSpec);
		}
		catch ( NumberFormatException expected ) {
			String field = String.format("%sID", argKindStr);
			SubmodelElementList argsList = (SubmodelElementList)m_submodelRef.get().getSubmodelElementByPath(pathPrefix);
			argIndex = SubmodelUtils.findFieldSMCByIdValue(argsList.getValue(), field, m_argSpec)
									.getOrThrow(() -> new ResourceNotFoundException("MDTArgument", "arg=" + m_argSpec))
									.index();
		}
		
		return String.format("%s[%d].%sValue", pathPrefix, argIndex, argKindStr);
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTManager mdt = HttpMDTManager.connect("http://localhost:12985");
		HttpMDTInstanceManager manager = (HttpMDTInstanceManager)mdt.getInstanceManager();
		
		DefaultSubmodelReference smRef = DefaultSubmodelReference.ofIdShort("inspector", "ThicknessInspection");
		MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, MDTArgumentKind.INPUT, "UpperImage");
		
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsFile());
		
		String json = ref.toJsonString();
		System.out.println(json);
		System.out.println(ElementReferences.parseJsonString(json));
		
		ObjectNode node = (ObjectNode)ref.toJsonNode();
		node.put("kind", "1");
		node.put("argumentSpec", "0");
		ref = (MDTArgumentReference)ElementReferences.parseJsonNode(node);
		ref.activate(manager);
		System.out.println(ref.toString() + ": " + ref.readAsString());
	}
}
