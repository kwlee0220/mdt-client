package mdt.cli.list.push;

import org.nocrala.tools.texttablefmt.Table;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
class TableCollector implements ListCollector {
	private Table m_table;
	
	TableCollector(Table table) {
		m_table = table;
	}

	@Override
	public void collectLine(Object[] cols) {
		FStream.of(cols).forEach(c -> m_table.addCell(""+c));
	}
	
	@Override
	public String getFinalString() {
		return m_table.render();
	}
}