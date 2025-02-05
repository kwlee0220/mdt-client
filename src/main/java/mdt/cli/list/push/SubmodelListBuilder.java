package mdt.cli.list.push;

import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;

import utils.InternalException;
import utils.func.FOption;

import mdt.cli.list.push.ListCommand.ListBuilder;
import mdt.cli.list.push.ListCommand.ListCollector;
import mdt.cli.list.push.ListCommand.SimpleListCollector;
import mdt.cli.list.push.ListCommand.TableCollector;
import mdt.model.DescriptorUtils;
import mdt.model.ReferenceUtils;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.Simulation;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelListBuilder implements ListBuilder {
	private MDTInstanceManager m_manager;
	private String m_filter;
	
	public SubmodelListBuilder(MDTInstanceManager manager, String filter) {
		m_manager = manager;
		m_filter = filter;
	}

	@Override
	public String buildListString() {
		return collect(new SimpleListCollector());
	}

	@Override
	public String buildTableString() {
		Table table = new Table(6);
		table.setColumnWidth(2, 20, 60);
		table.setColumnWidth(5, 5, 70);

		table.addCell(" # ");
		table.addCell(" MDT_ID ");
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" SEMANTIC_ID ");
		table.addCell(" ENDPOINT ");
		
		return collect(new TableCollector(table));
	}

	@Override
	public Node buildTreeNode() {
		return null;
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									? m_manager.getInstanceAllByFilter(m_filter)
									: m_manager.getInstanceAll();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			for ( SubmodelDescriptor smDesc: inst.getSubmodelDescriptorAll() ) {
				String[] cols = toColumns(seqNo, inst, smDesc);
				collector.collectLine(cols);
				
				++seqNo;
			}
		}
		
		return collector.getFinalString();
	}

	private String[] toColumns(int seqNo, MDTInstance inst, SubmodelDescriptor smDesc) {
		String[] cols = new String[] {
			""+seqNo, inst.getId(), smDesc.getId(), smDesc.getIdShort(),
			getSemanticIdString(smDesc), toEpString(smDesc.getEndpoints())
		};
		return cols;
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
