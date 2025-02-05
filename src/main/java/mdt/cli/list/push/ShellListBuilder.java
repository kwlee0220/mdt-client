package mdt.cli.list.push;

import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.cli.list.push.ListCommand.ListBuilder;
import mdt.cli.list.push.ListCommand.ListCollector;
import mdt.cli.list.push.ListCommand.SimpleListCollector;
import mdt.cli.list.push.ListCommand.TableCollector;
import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ShellListBuilder implements ListBuilder {
	private MDTInstanceManager m_manager;
	private String m_filter;
	
	public ShellListBuilder(MDTInstanceManager manager, String filter) {
		m_manager = manager;
		m_filter = filter;
	}

	@Override
	public String buildListString() {
		return collect(new SimpleListCollector());
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
	public Node buildTreeNode() {
		return null;
	}
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									?  m_manager.getInstanceAllByFilter(m_filter)
									: m_manager.getInstanceAll();
		
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
