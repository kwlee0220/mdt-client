package mdt.model.sm.ref;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

import utils.Indexed;

import mdt.client.HttpMDTManagerClient;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.SubmodelUtils;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class MDTArgumentReference extends AbstractElementReference implements MDTElementReference {
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_ARG_SPEC = "argumentSpec";
	private static final String ALL_ARGS = "*";
	
	private final MDTArgumentCollection m_argList;
	private final String m_argSpec;
	
	private volatile ElementReference m_argumentRef;
	private volatile int m_argIndex = -1;
	
	public static enum Kind {
		INPUT, OUTPUT;
		
		public static Kind fromString(String kindStr) {
			try {
				int ordinal = Integer.parseInt(kindStr);
				Preconditions.checkArgument(ordinal >= 0 && ordinal < 2,
											"OperationArgument's ordinal should be between 0 and 1, but {}", kindStr);
				return Kind.values()[ordinal];
			}
			catch ( NumberFormatException expected ) {
				kindStr = kindStr.trim().toLowerCase();
				if ( kindStr.startsWith("in") ) {
					return INPUT;
				}
				else if ( kindStr.startsWith("out") ) {
                    return OUTPUT;
                }
				else if ( kindStr.equals("*") ) {
					return null;
				}
				else {
					throw new IllegalArgumentException("Invalid OperationArgument's kind: " + kindStr);
				}
			}
		}
	}
	
	private MDTArgumentReference(MDTArgumentCollection argListColl, String argSpec) {
		Preconditions.checkArgument(argListColl != null, "Null OperationArgumentCollection");
		Preconditions.checkArgument(argSpec != null, "Null argSpec");
		
		m_argList = argListColl;
		m_argSpec = argSpec;
	}

	@Override
	public String getInstanceId() {
		return m_argList.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() {
		return m_argList.getInstance();
	}

	@Override
	public String getSubmodelIdShort() {
		return m_argList.getSubmodelIdShort();
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_argList.getSubmodelService();
	}

	@Override
	public String getElementPath() {
		if ( m_argumentRef instanceof MDTElementReference mdtRef ) {
			return mdtRef.getElementPath();
		}
		else {
			throw new IllegalStateException("Not MDTElementReference");
		}
	}

	@Override
	public SubmodelElement read() throws IOException {
		return m_argumentRef.read();
	}

	@Override
	public void write(SubmodelElement newElm) throws IOException {
		m_argumentRef.write(newElm);
	}
	
	public MDTSubmodelReference getSubmodelReference() {
		return m_argList.getSubmodelReference();
	}

	@Override
	public boolean isActivated() {
		return m_argumentRef != null;
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_argList.activate(manager);
		
		if ( m_argList.getKind() == null) {
			SubmodelElementCollection smc = (SubmodelElementCollection)m_argList.getOperationReference().read();
			SubmodelElementList inputs
						= SubmodelUtils.findFieldById(smc, "Inputs")
										.map(Indexed::value)
										.cast(SubmodelElementList.class)
										.getOrElse(() -> SubmodelUtils.newSubmodelElementList("Inputs", List.of()));
			SubmodelElementList outputs
						= SubmodelUtils.findFieldById(smc, "Outputs")
										.map(Indexed::value)
										.cast(SubmodelElementList.class)
										.getOrElse(() -> SubmodelUtils.newSubmodelElementList("Outputs", List.of()));
			SubmodelElementCollection group = SubmodelUtils.newSubmodelElementCollection("Arguments", List.of(inputs, outputs));
			m_argumentRef = InMemoryElementReference.of(group);
			
			return;
		}
		
		if ( m_argSpec.equals(ALL_ARGS) ) {
			m_argumentRef = DefaultElementReference.newInstance(m_argList.getSubmodelReference(),
																m_argList.getElementPath());
			return;
		}
		
		m_argIndex = -1;
		try {
			m_argIndex = Integer.parseInt(m_argSpec);
		}
		catch ( NumberFormatException expected ) {
			m_argIndex = m_argList.getArgumentIndex(m_argSpec);
		}
		
		String lastSegment = m_argList.getKind() == Kind.INPUT ? "InputValue" : "OutputValue";
		String path = String.format("%s[%d].%s", m_argList.getElementPath(), m_argIndex, lastSegment);
		m_argumentRef = DefaultElementReference.newInstance(m_argList.getSubmodelReference(), path);
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", m_argList.toString(), m_argSpec);
	}
	
	public static MDTArgumentReference newInstance(String instanceId, String submodelIdShort,
														String kind, String argSpec) {
		MDTArgumentCollection coll = MDTArgumentCollection.newInstance(instanceId, submodelIdShort, kind);
		return new MDTArgumentReference(coll, argSpec);
	}
	
	public static MDTArgumentReference newInstance(MDTSubmodelReference smRef, String kind, String argSpec) {
		MDTArgumentCollection coll = MDTArgumentCollection.newInstance(smRef, kind);
		
		return new MDTArgumentReference(coll, argSpec);
	}
	
	public static MDTArgumentReference parseString(String refExpr) {
		// <instanceId>/<submodelIdShort>/<kind>/<argSpec>
		String[] parts = refExpr.split("/");
		if ( parts.length != 4 ) {
			String msg = String.format("invalid %s: %s", MDTArgumentReference.class.getSimpleName(), refExpr);
			throw new IllegalArgumentException(msg);
		}
		
		return MDTArgumentReference.newInstance(parts[0], parts[1], parts[2], parts[3]);
	}
	
	public static MDTArgumentReference parseJson(ObjectNode topNode) throws IOException {
		JsonNode smRefNode = topNode.get(FIELD_SUBMODEL_REF);
		MDTSubmodelReference smRef = MDTModelSerDe.readValue(smRefNode, MDTSubmodelReference.class);
		
		String kindStr = topNode.get(FIELD_KIND).asText();
		String argSpec = topNode.get(FIELD_ARG_SPEC).asText();
		
		return MDTArgumentReference.newInstance(smRef, kindStr, argSpec);
	}
	
	@Override
	public void serialize(JsonGenerator gen) throws IOException, JsonProcessingException {
		gen.writeStartObject();
		gen.writeStringField(FIELD_REFERENCE_TYPE, ElementReferenceType.ARGUMENT.getCode());
		gen.writeObjectField(FIELD_SUBMODEL_REF, getSubmodelReference());
		gen.writeStringField(FIELD_KIND, m_argList.getKind().name().toLowerCase());
		gen.writeStringField(FIELD_ARG_SPEC, m_argSpec);
		gen.writeEndObject();
	}
	
	public static final void main(String... args) throws Exception {
		HttpMDTManagerClient mdt = HttpMDTManagerClient.connect("http://localhost:12985");
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		MDTSubmodelReference smRef = DefaultSubmodelReference.newInstance("inspector", "ThicknessInspection");
		MDTArgumentReference ref = MDTArgumentReference.newInstance(smRef, "input", "UpperImage");
		
		System.out.println(ref);
		ref.activate(manager);
		
		SubmodelElement sme = ref.read();
		System.out.println(sme);
		System.out.println(ref.readAsFile());
		
		String json = ref.toJsonString();
		System.out.println(json);
		System.out.println(ElementReferenceUtils.parseJsonString(json));
		
		ObjectNode node = (ObjectNode)ref.toJsonNode();
		node.put("kind", "1");
		node.put("argumentSpec", "0");
		ref = (MDTArgumentReference)ElementReferenceUtils.parseJsonNode(node);
		ref.activate(manager);
		System.out.println(ref.toString() + ": " + ref.readAsString());
	}
}
