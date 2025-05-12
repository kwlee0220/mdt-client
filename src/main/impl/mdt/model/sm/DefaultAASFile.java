package mdt.model.sm;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

import com.google.common.io.Files;

import utils.UnitUtils;
import utils.func.FOption;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultAASFile implements AASFile {
	private String m_path;
	private String m_contentType;
	private byte[] m_content;
	
	public static DefaultAASFile from(File file, String path, String mimeType) throws IOException {
		DefaultAASFile mdtFile = new DefaultAASFile();
		
		if ( mimeType == null ) {
			mimeType = new Tika().detect(file);
		}
		mdtFile.setContentType(mimeType);
		if ( path == null ) {
			path = file.getName();
		}
		mdtFile.setPath(path);
		mdtFile.load(file);
		
		return mdtFile;
	}
	
	public static DefaultAASFile from(File file) throws IOException {
		return from(file, file.getName(), null);
	}
	
	public String getPath() {
		return m_path;
	}

	public void setPath(String path) {
		m_path = path;
	}

	public String getContentType() {
		return m_contentType;
	}

	public void setContentType(String contentType) {
		m_contentType = contentType;
	}

	public byte[] getContent() {
		return m_content;
	}

	public void setContent(byte[] content) {
		m_content = content;
	}
	
	public void load(File file) throws IOException {
		m_content = Files.asByteSource(file).read();
	}
	
	public void save(File file) throws IOException {
		Files.write(m_content, file);
	}
	
	@Override
	public String toString() {
		String pathStr = FOption.getOrElse(m_path, "unknown");
		String typeStr = FOption.mapOrElse(m_contentType,
											t -> String.format("type=%s", t), "unknown");
		String szStr = FOption.mapOrElse(m_content,
										c -> String.format(": size=%s", UnitUtils.toByteSizeString(c.length)),
										"");
		return String.format("%s(%s)%s", pathStr, typeStr, szStr);
	}
}
