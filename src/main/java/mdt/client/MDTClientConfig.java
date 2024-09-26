package mdt.client;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
@NoArgsConstructor
@Data
public final class MDTClientConfig {
	private static final Logger s_logger = LoggerFactory.getLogger(MDTClientConfig.class);
//	private static final String DEFAULT_ENDPOINT = "http://localhost:12985/instance-manager";

	private String endpoint;
	
	public static MDTClientConfig load(Path configFile) throws IOException {
		if ( s_logger.isInfoEnabled() ) {
			s_logger.info("reading a configuration from {}", configFile);
		}
		
		Yaml yaml = new Yaml(new Constructor(MDTClientConfig.class, new LoaderOptions()));
		MDTClientConfig config = yaml.load(new FileReader(configFile.toFile()));
		
		return config;
	}
}
	
