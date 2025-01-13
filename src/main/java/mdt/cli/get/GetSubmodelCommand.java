package mdt.cli.get;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.stream.FStream;

import mdt.cli.AbstractMDTCommand;
import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.DescriptorUtils;
import mdt.model.Input;
import mdt.model.MDTManager;
import mdt.model.MDTModelSerDe;
import mdt.model.Output;
import mdt.model.ReferenceUtils;
import mdt.model.ResourceNotFoundException;
import mdt.model.service.MDTInstance;
import mdt.model.service.ParameterCollection;
import mdt.model.service.SubmodelService;
import mdt.model.sm.data.Data;
import mdt.model.sm.data.DataInfo;
import mdt.model.sm.data.DefaultData;
import mdt.model.sm.data.Parameter;
import mdt.model.sm.data.ParameterValue;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.simulation.DefaultSimulation;
import mdt.model.sm.simulation.Simulation;
import mdt.model.sm.value.ElementValues;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "submodel",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "Get Submodel information."
)
public class GetSubmodelCommand extends AbstractMDTCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(GetSubmodelCommand.class);

	@Parameters(index="0", arity="0..1", paramLabel="ref",
				description="SubmodelReference (<twin-id>/<submodel_idshort>) to show")
	private String m_submodelRefString = null;
	
	@Option(names={"--id"}, paramLabel="id", description="Submodel id to show")
	private String m_submodelId = null;
	
	@Option(names={"--output", "-o"}, paramLabel="type", required=false,
			description="output type (candidnates: table or json)")
	private String m_output = "table";
	
	public GetSubmodelCommand() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTManager mdt) throws Exception {
		HttpMDTInstanceManagerClient manager = (HttpMDTInstanceManagerClient)mdt.getInstanceManager();
		
		SubmodelService submodelSvc = null;
		if ( m_submodelRefString != null ) {
			try {
				DefaultSubmodelReference ref = DefaultSubmodelReference.parseString(m_submodelRefString);
				ref.activate(manager);
				
				submodelSvc = ref.get();
			}
			catch ( ResourceNotFoundException e ) {
				System.err.printf("Unknown SubmodelReference: %s", m_submodelRefString);
				System.exit(-1);
			}
		}
		else if ( m_submodelId != null ) {
			try {
				MDTInstance inst = manager.getInstanceBySubmodelId(m_submodelId);
				submodelSvc = inst.getSubmodelServiceById(m_submodelId);
			}
			catch ( Exception e ) {
				System.err.printf("Unknown Submodel id: %s", m_submodelId);
				System.exit(-1);
			}
		}
			
		m_output = m_output.toLowerCase();
		if ( m_output == null || m_output.equalsIgnoreCase("table") ) {
			displayAsTable(mdt, submodelSvc);
		}
		else if ( m_output.equalsIgnoreCase("json") ) {
			displayAsJson(submodelSvc);
		}
		else {
			System.err.println("Unknown output: " + m_output);
			System.exit(-1);
		}
	}

	public static final void main(String... args) throws Exception {
		main(new GetSubmodelCommand(), args);
	}
	
	private void displayAsJson(SubmodelService submodelSvc)
		throws SerializationException {
		Submodel submodel = submodelSvc.getSubmodel();
		
		JsonSerializer ser = new JsonSerializer();
		String jsonStr = ser.write(submodel);
		System.out.println(jsonStr);
	}
	
	private void displayAsTable(MDTManager mdt, SubmodelService submodelSvc) {
		Table table = new Table(2);
		Submodel submodel = submodelSvc.getSubmodel();

		table.addCell(" FIELD "); table.addCell(" VALUE");
		table.addCell(" ID "); table.addCell(" " + submodel.getId());
		table.addCell(" ID_SHORT "); table.addCell(" " + getOrEmpty(submodel.getIdShort()) + " ");
		
		table.addCell(" SEMANTIC_ID ");
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(submodel.getSemanticId());
		if ( semanticId != null ) {
			table.addCell(" " + semanticId);
		}

		if ( Data.SEMANTIC_ID.equals(semanticId) ) {
			DefaultData data = DefaultData.from(submodel);
			displayData(data, table);
		}
		else if ( Simulation.SEMANTIC_ID.equals(semanticId) ) {
			DefaultSimulation sim = DefaultSimulation.from(submodel);
			
			FStream.from(sim.getSimulationInfo().getInputs())
					.zipWithIndex()
					.forEach(idxed -> {
						Input input = idxed.value();
						String valueStr = MDTModelSerDe.toJsonString(ElementValues.getValue(input.getInputValue()));
						if ( valueStr.length() > 80 ) {
							valueStr = valueStr.substring(0, 77) + "...";
						}
						
						table.addCell(String.format(" INPUT[%02d: %s] ", idxed.index(), input.getInputID()));
						table.addCell(String.format(" %s (%s)",valueStr, input.getInputType()));
					});
			
			FStream.from(sim.getSimulationInfo().getOutputs())
					.zipWithIndex()
					.forEach(idxed -> {
						Output output = idxed.value();
						String valueStr = MDTModelSerDe.toJsonString(ElementValues.getValue(output.getOutputValue()));
						if ( valueStr.length() > 80 ) {
							valueStr = valueStr.substring(0, 77) + "...";
						}
						
						table.addCell(String.format(" OUTPUT[%02d: %s] ", idxed.index(), output.getOutputID()));
						table.addCell(String.format(" %s (%s)", valueStr, output.getOutputType()));
					});
		}
		else {
			FStream.from(submodel.getSubmodelElements())
					.map(sme -> sme.getIdShort())
					.zipWithIndex()
					.forEach(tup -> {
						table.addCell(String.format(" SUB_MODEL_ELEMENT[%02d] ", tup.index()));
						table.addCell(" " + tup.value() + " ");
					});
		}
			
		List<LangStringNameType> names = submodel.getDisplayName();
		if ( names != null ) {
			String displayName = FStream.from(names)
										.map(name -> name.getText())
										.join(". ");
			table.addCell(" DISPLAY_NAME "); table.addCell(" " + displayName);
		}
		else {
			table.addCell(" DISPLAY_NAME "); table.addCell("");
		}
		
		List<LangStringTextType> descs = submodel.getDescription();
		if ( names != null ) {
			String description = FStream.from(descs)
										.map(desc -> desc.getText())
										.join(". ");
			table.addCell(" DESCRIPTION "); table.addCell(" " + description);
		}
		else {
			table.addCell(" DESCRIPTION "); table.addCell("");
		}
		
		SubmodelDescriptor descriptor = mdt.getSubmodelRegistry()
											.getSubmodelDescriptorById(submodel.getId());
		String endpoint = DescriptorUtils.getEndpointString(descriptor.getEndpoints());
		table.addCell(" ENDPOINT "); table.addCell(" " + endpoint);
		
		System.out.println(table.render());
	}
	
	private void displayData(Data data, Table table) {
		DataInfo dataInfo = data.getDataInfo();
		ParameterCollection coll = dataInfo.getFirstSubmodelElementEntityByClass(ParameterCollection.class);
		FStream.from(coll.getParameterList())
				.zipWithIndex()
				.forEach(idxed -> {
					Parameter param = idxed.value();
					try {
						ParameterValue pval = coll.getParameterValue(param.getParameterId());
						SubmodelElement value = pval.getParameterValue();
						table.addCell(String.format(" PARAMETER[%02d: %s] ", idxed.index(), param.getParameterId()));
						table.addCell(String.format(" %s (%s)",
													MDTModelSerDe.toJsonString(ElementValues.getValue(value)),
													param.getParameterType()));
					}
					catch ( ResourceNotFoundException e ) {
						System.err.println("Failed to find ParameterValue: "  + param.getParameterId() + ", skipped");
					}
				});
	}
	
	private String getOrEmpty(Object obj) {
		return (obj != null) ? obj.toString() : "";
	}
}
