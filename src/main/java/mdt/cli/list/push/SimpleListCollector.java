package mdt.cli.list.push;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import utils.func.Try;
import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
class SimpleListCollector implements ListCollector {
	private ByteArrayOutputStream m_baos;
	private PrintWriter m_writer;
	
	SimpleListCollector() {
		m_baos = new ByteArrayOutputStream();
		m_writer = new PrintWriter(m_baos);
	}

	@Override
	public void collectLine(Object[] cols) {
		m_writer.println(FStream.of(cols).join('|'));
	}
	
	void addLine(String line) {
	}
	
	@Override
	public String getFinalString() {
		m_writer.close();
		Try.run(m_baos::close);
		return m_baos.toString();
	}
}