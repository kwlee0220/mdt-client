package mdt.model.instance;

import picocli.CommandLine.Parameters;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Search {
	public static enum Property {
		MDT_ID,
		AAS_ID,
		AAS_IDSHORT,
		SUBMODEL_ID,
		SUBMODEL_IDSHORT,
		ASSET_ID,
		ASSET_TYPE,
		STATUS,
	};
	
	public static enum OP {
		EQUAL,
		LIKE,
	};
	
	public static class Condition {
		@Parameters(index = "0", paramLabel = "property") Search.Property m_prop;
		@Parameters(index = "1", paramLabel = "op") Search.OP m_op;
		@Parameters(index = "2", paramLabel = "key") String key;
	}
}
