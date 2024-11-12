package mdt.cli.list.push;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.nocrala.tools.texttablefmt.Table;

import utils.stream.FStream;

import mdt.cli.IdPair;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.MDTInstance;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ListInstanceCommand implements ListBuilder {
	private MDTInstanceManager m_manager;
	private String m_filter;
	
	public ListInstanceCommand(MDTInstanceManager manager, String filter) {
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
									?  m_manager.getAllInstancesByFilter(m_filter)
									: m_manager.getAllInstances();
		
		int seqNo = 1;
		for ( MDTInstance inst: instances ) {
			Object[] cols = toColumns(seqNo, inst);
			collector.collectLine(cols);
		}
		
		return collector.getFinalString();
	}
	
	private String shorten(String submodelIdShort) {
		return switch ( submodelIdShort ) {
			case "InformationModel" -> "Info";
			case "Data" -> "Data";
			case "Simulation" -> "Sim";
			default -> submodelIdShort;
		};
	}

	private Object[] toColumns(int seqNo, MDTInstance instance) {
		String submodelIdCsv = FStream.from(instance.getAllInstanceSubmodelDescriptors())
										.map(InstanceSubmodelDescriptor::getIdShort)
										.map(this::shorten)
										.join(',');
		
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
