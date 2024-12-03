package mdt.cli.list;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.InternalException;
import utils.func.FOption;

import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.SimpleListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.model.DescriptorUtils;
import mdt.model.ReferenceUtils;
import mdt.model.service.MDTInstance;
import mdt.model.sm.ai.AI;
import mdt.model.sm.data.Data;
import mdt.model.sm.info.InformationModel;
import mdt.model.sm.simulation.Simulation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "submodels",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all Submodels."
)
public class ListSubmodelCommand extends AbstractListCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListSubmodelCommand.class);

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;
	
	public ListSubmodelCommand() {
		setLogger(s_logger);
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
	public String buildTreeString() {
		throw new UnsupportedOperationException();
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									? getMDTInstanceManager().getAllInstancesByFilter(m_filter)
									: getMDTInstanceManager().getAllInstances();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			for ( SubmodelDescriptor smDesc: inst.getAllSubmodelDescriptors() ) {
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
