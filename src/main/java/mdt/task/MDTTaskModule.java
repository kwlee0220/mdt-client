package mdt.task;

import java.time.Duration;
import java.util.Map;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTTaskModule {
	public Map<String,Object> run(Map<String,Object> inputValues, Duration timeout) throws Exception;
}
