package mdt.task.skku;

import java.time.Duration;
import java.util.Map;

import mdt.task.MDTTaskModule;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class BuildProcessTaktTime implements MDTTaskModule {
	@Override
	public Map<String, Object> run(Map<String, Object> inputValues, Duration timeout) throws Exception {
		String equipId = (String)inputValues.get("id");
		String equipName = (String)inputValues.get("name");
		String distStr = (String)inputValues.get("dist");
		
		String[] parts = distStr.split(",");
		
		ProcessTaktTime timeDist = new ProcessTaktTime();
		timeDist.setMachineId(equipId);
		timeDist.setMachineName(equipName);
		timeDist.setTaktTimeDist(parts[0]);
		timeDist.setTaktTimeLoc(Double.parseDouble(parts[1]));
		timeDist.setTaktTimeScale(Double.parseDouble(parts[2]));
//		timeDist.setTaktTimeScale(999.99);
		
		return Map.of(equipId, timeDist.toAasModel());
	}
}
