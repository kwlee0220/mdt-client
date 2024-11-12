package mdt.cli.list;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import utils.stream.FStream;

import mdt.cli.IdPair;
import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.SimpleListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.model.service.MDTInstance;
import mdt.model.sm.SubmodelUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "instances",
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all MDTInstances."
)
public class ListMDTInstanceCommand extends AbstractListCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListMDTInstanceCommand.class);

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;
	
	public ListMDTInstanceCommand() {
		setLogger(s_logger);
	}

	@Override
	public String buildListString() {
		return collect(new SimpleListCollector());
	}

	@Override
	public String buildTableString() {
		Table table = new Table(6);
		table.setColumnWidth(2, 20, 70);
		table.setColumnWidth(3, 10, 35);

		table.addCell(" # ");
		table.addCell(" INSTANCE ");
		table.addCell(" AAS_IDs ");
		table.addCell(" SUB_MODELS ");
		table.addCell(" STATUS ");
		table.addCell(" ENDPOINT ");
		
		return collect(new TableCollector(table));
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									? getMDTInstanceManager().getAllInstancesByFilter(m_filter)
									: getMDTInstanceManager().getAllInstances();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			Object[] cols = toColumns(seqNo, inst);
			collector.collectLine(cols);
			++seqNo;
		}
		
		return collector.getFinalString();
	}

	private Object[] toColumns(int seqNo, MDTInstance instance) {
		List<String> outputs = Lists.newArrayList();
		FStream.from(instance.getAllInstanceSubmodelDescriptors())
				.map(isdesc -> SubmodelUtils.getShortSubmodelSemanticId(isdesc.getSemanticId()))
				.groupByKey(n -> n)
				.switcher()
				.ifCase("Info").consume(grp -> outputs.add("Info"))
				.ifCase("Data").consume(grp -> outputs.add("Data"))
				.otherwise().forEach((k, grp) -> outputs.add(String.format("%s(%d)", k, grp.size())));
		
		String submodelIdCsv = FStream.from(outputs).join(',');
		
		String serviceEndpoint = ObjectUtils.defaultIfNull(instance.getBaseEndpoint(), "");
		return new Object[] {
			String.format("%3d", seqNo),
			instance.getId(),
			IdPair.of(instance.getAasId(),instance.getAasIdShort()),
			submodelIdCsv,
			instance.getStatus(),
			serviceEndpoint
		};
	}
}
