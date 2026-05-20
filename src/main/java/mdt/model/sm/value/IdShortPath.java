package mdt.model.sm.value;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import utils.func.Lazy;
import utils.stream.FStream;

import mdt.model.ResourceNotFoundException;
import mdt.model.sm.value.IdShortPath.IdShort.IndexIdShort;
import mdt.model.sm.value.IdShortPath.IdShort.KeyIdShort;

/**
*
* @author Kang-Woo Lee (ETRI)
*/
public class IdShortPath {
	private static final Pattern ID_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)");
	private static final Pattern INDEX_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)\\[(\\d+)\\]");
	
	private final List<IdShort> m_idShortList;
	private final Lazy<String> m_path = Lazy.of(this::buildPath);
	
	private IdShortPath(Builder builder) {
		m_idShortList = builder.m_idShortList;
	}
	
	public List<IdShort> getIdShortList() {
		return m_idShortList;
	}
	
	public SubmodelElement traverse(SubmodelElement parent) {
		Preconditions.checkArgument(parent != null);

		SubmodelElement sme = parent;
		for ( IdShort idShort : m_idShortList ) {
			sme = idShort.getChild(sme);
		}

		return sme;
	}
	
	public static IdShortPath fromString(String path) {
		Builder builder = IdShortPath.builder();
		
		String[] parts = path.split("\\.");
		for ( int i =0; i < parts.length; ++i ) {
			Matcher idMatcher = ID_PATTERN.matcher(parts[i]);
			if ( idMatcher.matches() ) {
				builder = builder.idShort(idMatcher.group(1));
				continue;
			}
			Matcher indexMatcher = INDEX_PATTERN.matcher(parts[i]);
			if ( indexMatcher.matches() ) {
				builder = builder.idShort(indexMatcher.group(1));
				int index = Integer.parseInt(indexMatcher.group(2));
				builder = builder.index(index);
				continue;
			}
			
			throw new IllegalArgumentException("Invalid IdShortPath: " + path);
		}
		
		return builder.build();
	}
	
	@Override
	public String toString() {
		return m_path.get();
	}
	
	private String buildPath() {
		StringBuilder sb = new StringBuilder();
		for ( IdShort idShort : m_idShortList ) {
			sb.append(idShort.toString());
		}
		return sb.substring(1);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private List<IdShort> m_idShortList = Lists.newArrayList();

		public IdShortPath build() {
			return new IdShortPath(this);
		}

		public Builder idShort(String idShort) {
			m_idShortList.add(new KeyIdShort(idShort));
			return this;
		}

		public Builder index(int index) {
			Preconditions.checkArgument(index >= 0, "index must be non-negative");
			Preconditions.checkState(m_idShortList.size() > 0, "IdShortPath should not start with IndexIdShort");
			
			m_idShortList.add(new IndexIdShort(index));
			return this;
		}
	}

	public static abstract class IdShort {
		public abstract String getKey();
		public abstract SubmodelElement getChild(SubmodelElement parent);
		
		public static class KeyIdShort extends IdShort {
			private final String m_key;

			public KeyIdShort(String key) {
				m_key = key;
			}

			@Override
			public String getKey() {
				return m_key;
			}
			
			public SubmodelElement getChild(SubmodelElement parent) {
				Preconditions.checkArgument(parent != null);
				Preconditions.checkArgument(parent instanceof SubmodelElementCollection);
				
				return FStream.from(((SubmodelElementCollection)parent).getValue())
								.findFirst(sme -> m_key.equals(sme.getIdShort()))
								.getOrThrow(() -> new ResourceNotFoundException("SubmodelElement", "idshort=" + m_key));
			}
			
			@Override
			public String toString() {
				return "." + m_key;
			}
		}
		
		public static class IndexIdShort extends IdShort {
			private final int m_index;

			public IndexIdShort(int index) {
				m_index = index;
			}

			@Override
			public String getKey() {
				return "" + m_index;
			}

			public int getIndex() {
				return m_index;
			}
			
			public SubmodelElement getChild(SubmodelElement parent) {
				Preconditions.checkArgument(parent != null);
				Preconditions.checkArgument(parent instanceof SubmodelElementList);
				
				List<SubmodelElement> children = ((SubmodelElementList)parent).getValue();
				if ( m_index >= children.size() ) {
					throw new ResourceNotFoundException("SubmodelElement", "index=" + m_index);
				}
				
				return children.get(m_index);
			}
			
			@Override
			public String toString() {
				return "[" + m_index + "]";
			}
		}
	}
}
