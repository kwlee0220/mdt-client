package mdt.cli.list;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.SimpleListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.model.DescriptorUtils;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.service.AISubmodelService;
import mdt.model.service.MDTInstance;
import mdt.model.service.SubmodelService;
import mdt.model.sm.ai.AI;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "ais",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all AI submodels."
)
public class ListAICommand extends AbstractListCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListAICommand.class);

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;
	
	public ListAICommand() {
		setLogger(s_logger);
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
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									? getMDTInstanceManager().getAllInstancesByFilter(m_filter)
									: getMDTInstanceManager().getAllInstances();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			for ( SubmodelDescriptor smDesc: inst.getAllSubmodelDescriptors() ) {
				if ( !AI.SEMANTIC_ID_REFERENCE.equals(smDesc.getSemanticId()) ) {
					continue;
				}
				
				String[] cols = toAIColumns(seqNo, inst, smDesc);
				collector.collectLine(cols);
				++seqNo;
			}
		}
		
		return collector.getFinalString();
	}
	
	private String[] toAIColumns(int seqNo, MDTInstance inst, SubmodelDescriptor smDesc) {
		String ref = String.format("%s/%s", inst.getId(), smDesc.getIdShort());
		String epString = toEpString(smDesc.getEndpoints());
		if ( epString.length() > 0 ) {
			SubmodelService svc = inst.getSubmodelServiceById(smDesc.getId());
			AI sim = new AISubmodelService(svc).getAI();
			
			String inputIdList = FStream.from(sim.getAIInfo().getInputs())
										.map(Input::getInputID)
										.join(", ");
			String outputIdList = FStream.from(sim.getAIInfo().getOutputs())
										.map(Output::getOutputID)
										.join(", ");
			
			return new String[] { ""+seqNo, ref, inputIdList, outputIdList, epString };
		}
		else {
			return new String[] { ""+seqNo, ref, "", "", epString };
		}
	}
	
	private String toEpString(List<Endpoint> epList) {
		return FOption.getOrElse(DescriptorUtils.getEndpointString(epList), "");
	}
}
