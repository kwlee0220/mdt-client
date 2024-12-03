package mdt.model.sm;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.Indexed;
import utils.InternalException;
import utils.func.Funcs;
import utils.func.Try;

import mdt.aas.DefaultSubmodelReference;
import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.ai.AI;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.SubmodelElementValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationArgumentReference extends AbstractSubmodelElementReference
										implements SubmodelElementReference, MDTSubmodelElementReference {
	private final MDTSubmodelReference m_smRef;
	@Nullable private final Kind m_kind;
	private final String m_argIndex;
	
	private volatile DefaultSubmodelElementReference m_opInfoRef;
	private volatile String m_argIdShortPath;
	
	public enum Kind { INPUT, OUTPUT };
	
	private OperationArgumentReference(MDTSubmodelReference smRef, Kind kind, String ordinal) {
		Preconditions.checkNotNull(smRef);
		Preconditions.checkNotNull(ordinal);
		
		m_smRef = smRef;
		m_kind = kind;
		m_argIndex = ordinal;
	}

	@Override
	public boolean isActivated() {
		return m_smRef.isActivated();
	}
	
	@Override
	public void activate(MDTInstanceManager manager) {
		m_smRef.activate(manager);
	}
	
	public MDTSubmodelReference getSubmodelReference() {
		return m_smRef;
	}
	
	public Kind getArgumentKind() {
		return m_kind;
	}

	@Override
	public String getInstanceId() {
		return m_smRef.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() {
		return m_smRef.getInstance();
	}

	@Override
	public String getSubmodelIdShort() {
		return m_smRef.getSubmodelIdShort();
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_smRef.get();
	}

	@Override
	public String getElementIdShortPath() {
		if ( m_argIdShortPath == null ) {
			DefaultSubmodelElementReference argRef = getOperationInfo();
			String pathSeg = switch ( m_kind ) {
				case INPUT -> "Inputs";
				case OUTPUT -> "Outputs";
			};
			if ( m_argIndex.equals("*") ) {
				m_argIdShortPath = String.format("%s.%s",
												argRef.getElementIdShortPath(), pathSeg);
			}
			else {
				try {
					int argIndex = Integer.parseInt(m_argIndex);
					m_argIdShortPath = String.format("%s.%s[%d]", argRef.getElementIdShortPath(), pathSeg, argIndex);
				}
				catch ( NumberFormatException e ) {
					SubmodelElementCollection arg = (SubmodelElementCollection)argRef.read();
					SubmodelElementList argList = (SubmodelElementList)SubmodelUtils.getFieldById(arg, pathSeg).value();
					int argIndex = getArgumentField(argList, m_kind, m_argIndex).index();
					m_argIdShortPath = String.format("%s.%s[%d]",
													argRef.getElementIdShortPath(), pathSeg, argIndex);
				}
			}
		}
		
		return m_argIdShortPath;
	}

	@Override
	public SubmodelElement read() throws IOException {
		DefaultSubmodelElementReference opInfoRef = getOperationInfo();
		SubmodelElementCollection opInfo = (SubmodelElementCollection)opInfoRef.read();
		Indexed<SubmodelElement> idxedArg = getArgument(opInfo, m_kind, m_argIndex);
		return idxedArg.value();
	}

	@Override
	public void write(SubmodelElement newSme) throws IOException {
		Preconditions.checkState(!m_argIndex.equals("*"), "Multi-value update is not supported");

		DefaultSubmodelElementReference opInfoRef = getOperationInfo();
		SubmodelElementCollection opInfo = (SubmodelElementCollection)opInfoRef.read();
		Indexed<SubmodelElement> idxedArg = getArgument(opInfo, m_kind, m_argIndex);
		
		SubmodelElementCollection argument = (SubmodelElementCollection)idxedArg.value();
		String fieldName = switch ( m_kind ) { case INPUT -> "InputValue"; case OUTPUT -> "OutputValue"; };
		int argValueFieldIndex = SubmodelUtils.getFieldById(argument, fieldName).index();
		
		argument.getValue().set(argValueFieldIndex, newSme);
		opInfoRef.write(opInfo);
	}

	@Override
	public void update(SubmodelElementValue value) throws ResourceNotFoundException, IOException {
		Preconditions.checkState(!m_argIndex.equals("*"), "Multi-value update is not supported");
		
		DefaultSubmodelElementReference opInfoRef = getOperationInfo();
		SubmodelElementCollection opInfo = (SubmodelElementCollection)opInfoRef.read();
		SubmodelElement argValue = getArgumentValue(opInfo);
		ElementValues.update(argValue, value);
		opInfoRef.write(opInfo);
	}
	
	@Override
	public void updateWithValueJsonNode(JsonNode valueNode) throws IOException {
		Preconditions.checkState(!m_argIndex.equals("*"), "Multi-value update is not supported");
		
		DefaultSubmodelElementReference opInfoRef = getOperationInfo();
		SubmodelElementCollection opInfo = (SubmodelElementCollection)opInfoRef.read();
		SubmodelElement argValue = getArgumentValue(opInfo);
		ElementValues.update(argValue, valueNode);
		
		opInfoRef.write(opInfo);
	}
	
	@Override
	public String toString() {
		String kindStr = switch ( m_kind ) { case INPUT -> "in"; case OUTPUT -> "out"; };
		return String.format("arg:%s/%s/%s/%s",
								m_smRef.getInstanceId(), m_smRef.getSubmodelIdShort(), kindStr, m_argIndex);
	}
	
	public static OperationArgumentReference newInstance(String instanceId, String submodelIdShort,
															Kind kind, String argIndex) {
		DefaultSubmodelReference smeRef
							= DefaultSubmodelReference.newInstance(instanceId, submodelIdShort);
		return new OperationArgumentReference(smeRef, kind, argIndex);
	}
	
	public static OperationArgumentReference newInstance(MDTSubmodelReference smRef, Kind kind, String argIndex) {
		return new OperationArgumentReference(smRef, kind, argIndex);
	}
	
	public static OperationArgumentReference parseString(String refExpr) {
		// <mdt-id>/<sm-id>/<inout>/<index>
		String[] parts = refExpr.split("/");
		if ( parts.length != 4 ) {
			throw new IllegalArgumentException("invalid OperationArgumentReference: " + refExpr);
		}

		Kind kind = parseKind(parts[2]);
		return newInstance(parts[0], parts[1], kind, parts[3]);
	}
	
	public static OperationArgumentReference parseJson(ObjectNode topNode) {
		String mdtId = topNode.get("mdtId").asText();
		String submodelIdShort = topNode.get("submodelIdShort").asText();
		String kindStr = topNode.get("kind").asText().toUpperCase();
		String argIndex = topNode.get("ordinal").asText();

		return newInstance(mdtId, submodelIdShort, parseKind(kindStr), argIndex);
	}
	
	private DefaultSubmodelElementReference getOperationInfo() {
		if ( m_opInfoRef == null ) {
			Submodel sm = getSubmodelService().getSubmodel();
			
			String semanticIdStr = ReferenceUtils.getSemanticIdStringOrNull(sm.getSemanticId());
			String opTypeStr = switch ( semanticIdStr ) {
				case AI.SEMANTIC_ID -> "AI";
				case Simulation.SEMANTIC_ID -> "Simulation";
				default -> throw new InternalException("Unexpected Operation type: " + semanticIdStr);
			};
			
			m_opInfoRef = DefaultSubmodelElementReference.newInstance(m_smRef,
																	String.format("%sInfo", opTypeStr));
		}
		
		return m_opInfoRef;
	}
	
	private SubmodelElement getArgumentValue(SubmodelElementCollection opInfo) {
		Indexed<SubmodelElement> idxedArg = getArgument(opInfo, m_kind, m_argIndex);
		SubmodelElementCollection argument = (SubmodelElementCollection)idxedArg.value();
		String fieldName = switch ( m_kind ) { case INPUT -> "InputValue"; case OUTPUT -> "OutputValue"; };
		return SubmodelUtils.getFieldById(argument, fieldName).value();
	}
	
	private static Indexed<SubmodelElement> getArgument(SubmodelElementCollection opInfo, Kind kind,
															String argIndexStr) {
		if ( kind == null ) {
			List<SubmodelElement> inoutArgList = Lists.newArrayList();
			Try.get(() -> SubmodelUtils.getFieldById(opInfo, "Inputs"))
				.ifSuccessful(found -> inoutArgList.add(found.value()));
			Try.get(() -> SubmodelUtils.getFieldById(opInfo, "Outputs"))
				.ifSuccessful(found -> inoutArgList.add(found.value()));
			DefaultSubmodelElementCollection result = new DefaultSubmodelElementCollection.Builder()
															.idShort("Arguments")
															.value(inoutArgList)
															.build();
			return Indexed.with(result, -1);
		}
		
		SubmodelElementList argList = switch ( kind ) {
			case INPUT -> (SubmodelElementList)SubmodelUtils.getFieldById(opInfo, "Inputs").value();
			case OUTPUT -> (SubmodelElementList)SubmodelUtils.getFieldById(opInfo, "Outputs").value();
		};
		return getArgumentField(argList, kind, argIndexStr);
	}
	
	private static Indexed<SubmodelElement> getArgumentField(SubmodelElementList argListElm,
																Kind kind, String argIndexStr) {
		List<SubmodelElement> args = argListElm.getValue();
		if ( argIndexStr.equals("*") ) {
			return Indexed.with(argListElm, -1);
		}
		try {
			int argIndex = Integer.parseInt(argIndexStr);
			return Indexed.with(args.get(argIndex), argIndex);
		}
		catch ( NumberFormatException e ) {
			String fieldName = switch ( kind ) { case INPUT -> "InputID"; case OUTPUT -> "OutputID"; };
			
			return Funcs.findFirstIndexed(args, arg -> {
				try {
					SubmodelElement found = SubmodelUtils.getFieldById((SubmodelElementCollection)arg, fieldName).value();
					return ((Property)found).getValue().equals(argIndexStr);
				}
				catch ( Exception e1 ) {
					return false;
				}
			})
			.getOrThrow(() -> new IllegalArgumentException("Invalid OperationArgument reference: "
															+ "unknown argument name: " + argIndexStr));
		}
	}
	
	private static Kind parseKind(String kindStr) {
		try {
			int ordinal = Integer.parseInt(kindStr);
			Preconditions.checkArgument(ordinal >= 0 && ordinal < 2,
										"OperationArgument's ordinal should be between 0 and 1, but {}", kindStr);
			return Kind.values()[ordinal];
		}
		catch ( NumberFormatException expected ) {
			kindStr = kindStr.toLowerCase();
			
			if ( Kind.INPUT.name().toLowerCase().startsWith(kindStr) ) {
				return Kind.INPUT;
			}
			else if ( Kind.OUTPUT.name().toLowerCase().startsWith(kindStr) ) {
				return Kind.OUTPUT;
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
