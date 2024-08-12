package mdt.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import mdt.model.instance.MDTInstanceManager;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class CopySubmodelElement implements MDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(CopySubmodelElement.class);
	
	private MDTInstanceManager m_manager;

	@Override
	public void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options) {
		Preconditions.checkState(m_manager != null);
		
		Port srcPort = inputPorts.get("src");
		Port tarPort = outputPorts.get("tar");
		
		if ( srcPort == null ) {
			throw new IllegalArgumentException("source port is missing");
		}
		if ( tarPort == null ) {
			throw new IllegalArgumentException("target port is missing");
		}
		
		Object src = srcPort.get();
		
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("read source port: " + srcPort + ", value=" + src);
			s_logger.debug("writing target port: " + tarPort);
		}
		
		tarPort.set(src);
	}

	@Override
	public void setMDTInstanceManager(MDTInstanceManager manager) {
		m_manager = manager;
	}
	
	public static class Command extends MDTTaskCommand<CopySubmodelElement> {
		@Override
		protected CopySubmodelElement newTask() {
			return new CopySubmodelElement();
		}

		public static final void main(String... args) throws Exception {
			main(new Command(), args);
		}
	}
}
