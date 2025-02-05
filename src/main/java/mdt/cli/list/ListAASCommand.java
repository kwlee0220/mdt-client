package mdt.cli.list;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.list.ListCommands.ListCollector;
import mdt.cli.list.ListCommands.SimpleListCollector;
import mdt.cli.list.ListCommands.TableCollector;
import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Command(
	name = "aas",
	aliases = {"shells"},
	parameterListHeading = "Parameters:%n",
	optionListHeading = "Options:%n",
	mixinStandardHelpOptions = true,
	description = "\nList all AssetAdministrationShells."
)
public class ListAASCommand extends AbstractListCommand {
	private static final Logger s_logger = LoggerFactory.getLogger(ListAASCommand.class);

	@Option(names={"--filter", "-f"}, paramLabel="filter-expr", description="instance filter.")
	private String m_filter = null;
	
	public ListAASCommand() {
		setLogger(s_logger);
	}

	@Override
	public String buildListString(String delim) {
		return collect(new SimpleListCollector(delim));
	}

	@Override
	public String buildTableString() {
		Table table = new Table(7);
		table.setColumnWidth(1, 20, 70);
		table.setColumnWidth(5, 10, 50);
		table.setColumnWidth(6, 10, 50);

		table.addCell(" # ");
		table.addCell(" ID ");
		table.addCell(" ID_SHORT ");
		table.addCell(" GLOBAL_ASSET_ID ");
		table.addCell(" INSTANCE ");
		table.addCell(" DISPLAY_NAMES ");
		table.addCell(" SUBMODELS ");
		
		return collect(new TableCollector(table));
	}

	@Override
	public String buildTreeString() {
		throw new UnsupportedOperationException();
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									? getMDTInstanceManager().getInstanceAllByFilter(m_filter)
									: getMDTInstanceManager().getInstanceAll();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			try {
				AssetAdministrationShell aas = inst.getAssetAdministrationShellService()
													.getAssetAdministrationShell();
				Object[] cols = toColumns(seqNo, aas, inst);
				collector.collectLine(cols);
			}
			catch ( InvalidResourceStatusException expected ) {
				AssetAdministrationShellDescriptor aasDesc = inst.getAASDescriptor();
				Object[] cols = toColumns(seqNo, aasDesc, inst);
				collector.collectLine(cols);
			}
			++seqNo;
		}
		
		return collector.getFinalString();
	}
	
	private String[] toColumns(int seqNo, AssetAdministrationShell aas, MDTInstance inst) {
		String displayNames = FOption.ofNullable(aas.getDisplayName())
									.flatMapFStream(names -> FStream.from(names))
									.join(", ");
		
		String smIdCsv = FStream.from(inst.getInstanceDescriptor().getInstanceSubmodelDescriptors())
								.map(InstanceSubmodelDescriptor::getIdShort)
								.join(", ");
		return new String[] {
			"" + seqNo,
			aas.getId(),										// ID
			aas.getIdShort(),									// ID_SHORT
			aas.getAssetInformation().getGlobalAssetId(),		// GLOBAL_ASSET_ID
			inst.getId(),										// INSTANCE
			displayNames,										// DISPLAY_NAME
			smIdCsv,											// SUBMODELS
		};
	}
	
	private String[] toColumns(int seqNo, AssetAdministrationShellDescriptor aasDesc, MDTInstance inst) {
		String displayNames = "";
		
		String smIdCsv = FStream.from(aasDesc.getSubmodelDescriptors())
								.map(SubmodelDescriptor::getIdShort)
								.join(", ");
		return new String[] {
			"" + seqNo,
			aasDesc.getId(),				// ID
			aasDesc.getIdShort(),			// ID_SHORT
			aasDesc.getGlobalAssetId(),		// GLOBAL_ASSET_ID
			inst.getId(),					// INSTANCE
			displayNames,					// DISPLAY_NAME
			smIdCsv,						// SUBMODELS
		};
	}
}
