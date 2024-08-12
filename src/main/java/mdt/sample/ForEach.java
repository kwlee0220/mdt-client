package mdt.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.stream.Stream;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mdt.ksx9101.model.Data;
import mdt.ksx9101.model.InformationModel;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ForEach {
	private static final File MODEL_DIR = new File("D:\\Dropbox\\Temp\\fa3st-repository\\ispark\\models_postgresql");
	private static final File OUTPUT_DIR = new File("C:\\Temp\\output");
	
	private static final JsonMapper MAPPER = new JsonMapper();
	
	public static final void main(String... args) throws Exception {
		OUTPUT_DIR.mkdirs();
		
		Stream.of(MODEL_DIR.listFiles())
				.filter(file -> file.getName().startsWith("conf_KR"))
				.forEach(ForEach::changeIdShort);
	}
	
//	private static void addCertificateConfig(File confFile) {
//		try ( InputStream is = new FileInputStream(confFile) ) {
//			JsonNode root = MAPPER.readTree(confFile);
//			
//			ArrayNode endpoints = (ArrayNode)root.at("/endpoints");
//			ObjectNode endpoint = (ObjectNode)endpoints.get(0);
//			
//			ObjectNode certNode = MAPPER.createObjectNode();
//			certNode.put("keyStoreType", "PKCS12");
//			certNode.put("keyStorePath", "mdt.p12");
//			certNode.put("keyStorePassword", "mdt2024^^");
//			certNode.put("keyAlias", "server-key");
//			certNode.put("keyPassword", "mdt2024^^");
//			endpoint.set("certificate", certNode);
//			
//			File toModelFile = new File(OUTPUT_DIR, confFile.getName());
//			MAPPER.writerWithDefaultPrettyPrinter()
//					.writeValue(toModelFile, root);
//			System.out.println("update conf file: " + toModelFile);
//		}
//		catch ( Exception e ) {
//			e.printStackTrace();
//		}
//	}
	
	private static void changeIdShort(File confFile) {
		try ( InputStream is = new FileInputStream(confFile) ) {
			JsonNode root = MAPPER.readTree(confFile);
			
			ObjectNode entity = (ObjectNode)root.at("/persistence/entities/0");
			entity.put("idShort", "Equipment");
			
			File dir = confFile.getParentFile();
			File toModelFile = new File(OUTPUT_DIR, confFile.getName());
			MAPPER.writerWithDefaultPrettyPrinter()
					.writeValue(toModelFile, root);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
//	private static void fileAction(File confFile) {
//		try ( InputStream is = new FileInputStream(confFile) ) {
//			JsonNode root = MAPPER.readTree(confFile);
//			
//			ObjectNode jdbc = (ObjectNode)root.at("/persistence/jpa/jdbc");
//			
//			jdbc.put("jdbcUrl", "jdbc:postgresql://localhost:5432/mdt");
//			jdbc.put("user", "mdt");
//			jdbc.put("password", "urc2004");
//			
//			File dir = confFile.getParentFile();
//			File toModelFile = new File(OUTPUT_DIR, confFile.getName());
//			MAPPER.writerWithDefaultPrettyPrinter()
//					.writeValue(toModelFile, root);
//		}
//		catch ( Exception e ) {
//			e.printStackTrace();
//		}
//	}
	
//	private static void fileAction(File modelFile) {
//		File dir = modelFile.getParentFile();
//		String fileName = modelFile.getName().replace("aas_", "model_");
//		File toModelFile = new File(dir, fileName);
//		
//		try {
//			Files.move(modelFile, toModelFile);
//		}
//		catch ( IOException e ) {
//			e.printStackTrace();
//		}
//	}
	
//	private static void fileAction(File modelFile) {
//		try ( InputStream is = new FileInputStream(modelFile) ) {
//			Environment env = AASUtils.getJsonDeserializer().read(is, Environment.class);
//			env.getSubmodels().stream().forEach(ForEach::doOnSubmodel);
//			
//			File outputFile = new File(OUTPUT_DIR, modelFile.getName());
//			try ( OutputStream os = new FileOutputStream(outputFile) ) {
//				AASUtils.getJsonSerializer().write(os, env);
//			}
//		}
//		catch ( Exception e ) {
//			e.printStackTrace();
//		}
//	}
	
	private static void doOnSubmodel(Submodel sm) {
		switch ( sm.getIdShort() ) {
			case "Data":
				sm.setSemanticId(Data.SEMANTIC_ID);
				break;
			case "InformationModel":
				sm.setSemanticId(InformationModel.SEMANTIC_ID);
				break;
			default:
				break;
		}
	}
}
