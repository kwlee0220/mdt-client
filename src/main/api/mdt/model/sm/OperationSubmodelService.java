package mdt.model.sm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.experimental.Delegate;

import utils.KeyValue;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.client.operation.AASOperationClient;
import mdt.model.MDTModelSerDe;
import mdt.model.SubmodelService;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementListValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class OperationSubmodelService implements SubmodelService {
	@Delegate protected final SubmodelService m_service;
	private final Submodel m_metadata;
	private final String m_pathPrefix;
	
	public OperationSubmodelService(SubmodelService service) {
		m_service = service;
		
		m_metadata = m_service.getSubmodel(Modifier.METADATA);

		String semanticId = SubmodelUtils.getSemanticIdStringOrNull(m_metadata.getSemanticId());
		m_pathPrefix = switch ( semanticId ) {
			case Data.SEMANTIC_ID -> "DataInfo";
			case AI.SEMANTIC_ID -> "AIInfo";
			case Simulation.SEMANTIC_ID -> "SimulationInfo";
			default -> "";
		};
	}
	
	public ArgumentCollection getInputs() {
		return new ArgumentCollection("Input");
	}
	
	public ArgumentCollection getOutputs() {
		return new ArgumentCollection("Output");
	}
	
	public Map<String,ElementValue> run(Map<String,ElementValue> inputValues, Duration timeout, Duration pollInterval)
		throws CancellationException, InterruptedException, TimeoutException, ExecutionException {
		try {
			AASOperationClient opClient = new AASOperationClient(this, "Operation", pollInterval);
			opClient.setTimeout(timeout);
			
			opClient.setInputVariableValues(inputValues);
			OperationResult result = opClient.run();
			return FStream.from(result.getOutputArguments())
					.mapToKeyValue(opVar -> {
						String argId = opVar.getValue().getIdShort();
						ElementValue argValue = ElementValues.getValue(opVar.getValue());
						
						return KeyValue.of(argId, argValue);
					})
					.toMap();
		}
		catch ( IOException e ) {
			throw new ExecutionException(e);
		}
	}
	
	public class ArgumentCollection {
		private final String m_argKind;
		private final String m_path;
		private SubmodelElementList m_arguments;
		private Map<String,SubmodelElement> m_argMap;
		
		private ArgumentCollection(String argKind) {
			m_argKind = argKind;
			m_path = String.format("%s.%ss", m_pathPrefix, argKind);
			
			load();
		}
		
		public void load() {
			m_arguments = (SubmodelElementList)m_service.getSubmodelElementByPath(m_path);
			m_argMap = FStream.from(m_arguments.getValue())
								.cast(SubmodelElementCollection.class)
								.mapToKeyValue(smc -> {
									String id = SubmodelUtils.getStringFieldById(smc, String.format("%sID", m_argKind));
									SubmodelElement value = SubmodelUtils.getFieldById(smc, String.format("%sValue", m_argKind));
									return KeyValue.of(id, value);
								})
								.toMap();
		}
		
		public void save() {
			m_service.updateSubmodelElementByPath(m_path, m_arguments);
		}
		
		public Map<String,ElementValue> getValue() {
			ElementListValue elv = (ElementListValue)ElementValues.getValue(m_arguments);
			return FStream.from(elv.getElementAll())
							.cast(ElementCollectionValue.class)
							.mapToKeyValue(ecv -> {
								StringPropertyValue id = (StringPropertyValue)ecv.getField(String.format("%sID", m_argKind));
								ElementValue value = ecv.getField(String.format("%sValue", m_argKind));
								return KeyValue.of(""+id.toValueObject(), value);
							})
							.toMap();
		}
		
		public ArgumentCollection setValue(Map<String,ElementValue> values) {
			for ( SubmodelElement sme: m_arguments.getValue() ) {
				String id = SubmodelUtils.getStringFieldById(sme, String.format("%sID", m_argKind));
				ElementValue ev = values.get(id);
				if ( ev != null ) {
					SubmodelElement value = SubmodelUtils.getFieldById(sme, String.format("%sValue", m_argKind));
					ElementValues.update(value, ev);
				}
			}
			return this;
		}
		public ArgumentCollection setValue(String id, ElementValue value) {
			SubmodelElement arg = m_argMap.get(id);
			if ( arg != null ) {
				ElementValues.update(arg, value);
			}
			return this;
		}
		
		public List<OperationVariable> toOperationVariables() {
			return KeyValueFStream.from(m_argMap)
							.map((argId, argValue) -> {
								try {
									JsonNode jnode = MDTModelSerDe.toJsonNode(argValue);
									SubmodelElement copied = MDTModelSerDe.readValue(jnode, SubmodelElement.class);
									copied.setIdShort(argId);
									
									return (OperationVariable)new DefaultOperationVariable.Builder()
																		.value(copied)
																		.build();
								}
								catch ( IOException e ) {
									throw new UncheckedIOException(e);
								}
							})
							.toList();
		}
	}
}
