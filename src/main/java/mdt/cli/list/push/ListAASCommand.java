package mdt.cli.list.push;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.nocrala.tools.texttablefmt.Table;

import utils.func.FOption;
import utils.stream.FStream;

import mdt.model.InvalidResourceStatusException;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ListAASCommand implements ListBuilder {
	private MDTInstanceManager m_manager;
	private String m_filter;
	
	public ListAASCommand(MDTInstanceManager manager, String filter) {
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
	
	private String collect(ListCollector collector) {
		List<? extends MDTInstance> instances = (m_filter != null)
									?  m_manager.getAllInstancesByFilter(m_filter)
									: m_manager.getAllInstances();
		
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
