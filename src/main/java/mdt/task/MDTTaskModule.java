package mdt.task;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import mdt.model.instance.MDTInstanceManager;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTTaskModule {
	public static class Outputs {
		private final Map<String,String> m_inoutPortValues;
		private final Map<String,String> m_outputPortValues;
		
		public Outputs(Map<String,String> inoutPortValues, Map<String,String> outputPortValues) {
			m_inoutPortValues = inoutPortValues;
			m_outputPortValues = outputPortValues;
		}
		
		public Map<String,String> getInoutPortValues() {
			return m_inoutPortValues;
		}
		
		public Map<String,String> getOutputPortValues() {
			return m_outputPortValues;
		}
	}
	
	public Outputs run(MDTInstanceManager manager,
						Map<String,Object> inputPortValues,
						Map<String,Object> inoutPortValues,
						List<String> outputPortNames,
						Map<String,String> options,
						Duration timeout) throws Exception;
}
