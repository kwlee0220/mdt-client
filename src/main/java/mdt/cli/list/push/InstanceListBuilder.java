package mdt.cli.list.push;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.barfuin.texttree.api.Node;
import org.nocrala.tools.texttablefmt.Table;

import utils.stream.FStream;

import mdt.cli.IdPair;
import mdt.cli.list.push.ListCommand.ListBuilder;
import mdt.cli.list.push.ListCommand.ListCollector;
import mdt.cli.list.push.ListCommand.SimpleListCollector;
import mdt.cli.list.push.ListCommand.TableCollector;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.SubmodelUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InstanceListBuilder implements ListBuilder {
	private MDTInstanceManager m_manager;
	private String m_filter;
	
	public InstanceListBuilder(MDTInstanceManager manager, String filter) {
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
			Object[] cols = toColumns(seqNo, inst);
			collector.collectLine(cols);
			++seqNo;
		}
		
		return collector.getFinalString();
	}

	private Object[] toColumns(int seqNo, MDTInstance instance) {
		String submodelIdCsv = FStream.from(instance.getInstanceSubmodelDescriptorAll())
										.map(InstanceSubmodelDescriptor::getSemanticId)
										.map(SubmodelUtils::getShortSubmodelSemanticId)
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
