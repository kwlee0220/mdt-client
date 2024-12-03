package mdt.cli.list.push;

import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;

import utils.InternalException;
import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.list.push.ListCommand.ListBuilder;
import mdt.cli.list.push.ListCommand.ListCollector;
import mdt.cli.list.push.ListCommand.SimpleListCollector;
import mdt.cli.list.push.ListCommand.TableCollector;
import mdt.model.DescriptorUtils;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.ReferenceUtils;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;
import mdt.model.service.SimulationSubmodelService;
import mdt.model.service.SubmodelService;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.Simulation;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class AIListBuilder implements ListBuilder {
	private MDTInstanceManager m_manager;
	private String m_filter;
	
	public AIListBuilder(MDTInstanceManager manager, String filter) {
		m_manager = manager;
		m_filter = filter;
	}

	@Override
	public String buildListString() {
		return collect(new SimpleListCollector());
	}

	@Override
	public String buildTableString() {
		Table table = new Table(5);
		table.setColumnWidth(2, 20, 70);
		table.setColumnWidth(3, 10, 50);
		table.setColumnWidth(4, 30, 130);

		table.addCell(" # ");
		table.addCell(" AI ");
		table.addCell(" INPUTS ");
		table.addCell(" OUTPUTS ");
		table.addCell(" ENDPOINT ");
		
		return collect(new TableCollector(table));
	}

	@Override
	public Node buildTreeNode() {
		return null;
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									?  m_manager.getAllInstancesByFilter(m_filter)
									: m_manager.getAllInstances();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			for ( SubmodelDescriptor smDesc: inst.getAllSubmodelDescriptors() ) {
				if ( !AI.SEMANTIC_ID.equals(getSemanticIdString(smDesc)) ) {
					continue;
				}
				
				String[] cols = toSimulationColumns(seqNo, inst, smDesc);
				collector.collectLine(cols);
				++seqNo;
			}
		}
		
		return collector.getFinalString();
	}
	
	private String[] toSimulationColumns(int seqNo, MDTInstance inst, SubmodelDescriptor smDesc) {
		String ref = String.format("%s/%s", inst.getId(), smDesc.getIdShort());
		String epString = toEpString(smDesc.getEndpoints());
		if ( epString.length() > 0 ) {
			SubmodelService svc = inst.getSubmodelServiceById(smDesc.getId());
			Simulation sim = new SimulationSubmodelService(svc).getSimulation();
			
			String inputIdList = FStream.from(sim.getSimulationInfo().getInputs())
										.map(Input::getInputID)
										.join(", ");
			String outputIdList = FStream.from(sim.getSimulationInfo().getOutputs())
										.map(Output::getOutputID)
										.join(", ");
			
			return new String[] { ""+seqNo, ref, inputIdList, outputIdList, epString };
		}
		else {
			return new String[] { ""+seqNo, ref, "", "", epString };
		}
	}
	
	private String getSemanticIdString(SubmodelDescriptor smDesc) {
		String semanticId = ReferenceUtils.getSemanticIdStringOrNull(smDesc.getSemanticId());
		if ( semanticId == null ) {
			return "";
		}
		return switch ( semanticId ) {
			case Data.SEMANTIC_ID -> "Data";
			case InformationModel.SEMANTIC_ID -> "InformationModel";
			case Simulation.SEMANTIC_ID -> "Simulation";
			case AI.SEMANTIC_ID -> "AI";
			default -> throw new InternalException("Unsupported SemanticID: " + semanticId);
		};
	}
	
	private String toEpString(List<Endpoint> epList) {
		return FOption.getOrElse(DescriptorUtils.getEndpointString(epList), "");
	}
}
