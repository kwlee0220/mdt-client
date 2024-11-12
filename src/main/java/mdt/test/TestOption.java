package mdt.test;

import java.util.List;

import mdt.model.MDTModelSerDe;
import mdt.model.workflow.BoolOption;
import mdt.model.workflow.MultilineOption;
import mdt.model.workflow.SMERefOption;
import mdt.model.workflow.StringArrayOption;
import mdt.model.workflow.StringOption;
import mdt.model.workflow.SubmodelRefOption;
import mdt.model.workflow.TwinRefOption;
import mdt.workflow.model.Option;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOption {
	public static final void main(String... args) throws Exception {
		Option opt, opt2;
		String jsonStr;
		
		opt = new StringOption("opt1", "value");
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt2.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new BoolOption("sync", true);
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new BoolOption("sync", false);
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new StringArrayOption("command", List.of("aaa", "bbb", "ccc"));
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new MultilineOption("script", """
		asdf
		if ( x ) {
			System.out.println();
		}
		""");
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new TwinRefOption("twin", "KR3");
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new SubmodelRefOption("simulation", "KR3", "Simulation");
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
		
		opt = new SMERefOption("simulation", "KR3", "Simulation", "SimulationInfo.Inputs[1].InputValue");
		jsonStr = MDTModelSerDe.toJsonString(opt);
		opt2 = MDTModelSerDe.readValue(jsonStr, Option.class);
		System.out.println(opt.toCommandOptionSpec());
		System.out.println(MDTModelSerDe.toJsonString(opt2));
	}
}
