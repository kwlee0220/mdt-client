package mdt.model.sm;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

import com.google.common.io.Files;

import utils.UnitUtils;
import utils.func.FOption;

import lombok.Getter;
import lombok.Setter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultMDTFile implements MDTFile {
	private String path;
	private String contentType;
	private byte[] content;
	
	public static DefaultMDTFile from(File file, String path) throws IOException {
		DefaultMDTFile mdtFile = new DefaultMDTFile();
		
		mdtFile.setContentType(new Tika().detect(file));
		mdtFile.setPath(path);
		mdtFile.load(file);
		
		return mdtFile;
	}
	
	public static DefaultMDTFile from(File file) throws IOException {
		return from(file, file.getName());
	}
	
	public void load(File file) throws IOException {
		this.content = Files.asByteSource(file).read();
	}
	
	public void save(File file) throws IOException {
		Files.write(this.content, file);
	}
	
	@Override
	public String toString() {
		String pathStr = FOption.getOrElse(this.path, "unknown");
		String typeStr = FOption.mapOrElse(this.contentType,
											t -> String.format("type=%s", t), "unknown");
		String szStr = FOption.mapOrElse(this.content,
										c -> String.format(": size=%s", UnitUtils.toByteSizeString(c.length)),
										"");
		return String.format("%s(%s)%s", pathStr, typeStr, szStr);
	}
}
