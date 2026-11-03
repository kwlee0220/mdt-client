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
 * SubmodelElement 계층 내의 idShort 경로(예: {@code a.b[2].c})를 표현하는 클래스.
 * <p>
 * 경로는 {@link IdShort}의 목록으로 구성된다. {@link KeyIdShort}는 {@link SubmodelElementCollection}의
 * 하위 원소를 idShort로, {@link IndexIdShort}는 {@link SubmodelElementList}의 하위 원소를 인덱스로 가리킨다.
 * 문자열 표현({@link #toString()})은 idShort를 {@code .}으로 잇고 인덱스는 {@code [n]}으로 붙인 형태이다.
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
	
	/**
	 * 경로를 구성하는 {@link IdShort} 목록을 반환한다.
	 *
	 * @return	{@link IdShort} 목록.
	 */
	public List<IdShort> getIdShortList() {
		return m_idShortList;
	}
	
	/**
	 * 주어진 SubmodelElement에서 시작하여 경로를 따라 내려간 위치의 SubmodelElement를 반환한다.
	 * <p>
	 * 경로가 비어 있으면 {@code parent}가 그대로 반환된다.
	 *
	 * @param parent	탐색을 시작할 SubmodelElement.
	 * @return	경로가 가리키는 SubmodelElement.
	 * @throws IllegalArgumentException	{@code parent}가 {@code null}이거나, 경로 중간 원소의 종류가 맞지 않는 경우
	 * 									(idShort 단계에서 SubmodelElementCollection이 아니거나, 인덱스 단계에서
	 * 									SubmodelElementList가 아닌 경우).
	 * @throws ResourceNotFoundException	경로 중의 idShort나 인덱스에 해당하는 원소가 없는 경우.
	 */
	public SubmodelElement traverse(SubmodelElement parent) {
		Preconditions.checkArgument(parent != null);

		SubmodelElement sme = parent;
		for ( IdShort idShort : m_idShortList ) {
			sme = idShort.getChild(sme);
		}

		return sme;
	}
	
	/**
	 * {@code .}으로 구분된 idShort 경로 문자열(예: {@code a.b[2].c})을 파싱하여 {@code IdShortPath}를 생성한다.
	 * <p>
	 * 각 구간은 {@code [a-zA-Z0-9_]+} 형태의 idShort이거나, idShort 뒤에 인덱스 하나가 붙은
	 * {@code idShort[n]} 형태여야 한다.
	 *
	 * @param path	idShort 경로 문자열.
	 * @return	생성된 {@code IdShortPath}.
	 * @throws IllegalArgumentException	경로 문자열의 형식이 잘못된 경우.
	 */
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
		if ( m_idShortList.isEmpty() ) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for ( IdShort idShort : m_idShortList ) {
			sb.append(idShort.toString());
		}
		return sb.substring(1);
	}
	
	/**
	 * 빈 경로에서 시작하는 {@link Builder}를 생성한다.
	 *
	 * @return	{@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * {@link IdShortPath}를 단계적으로 구성하는 빌더.
	 */
	public static class Builder {
		private List<IdShort> m_idShortList = Lists.newArrayList();

		/**
		 * 지금까지 추가된 단계들로 {@link IdShortPath}를 생성한다.
		 *
		 * @return	생성된 {@link IdShortPath}.
		 */
		public IdShortPath build() {
			return new IdShortPath(this);
		}

		/**
		 * 경로 끝에 idShort 단계({@link KeyIdShort})를 추가한다.
		 *
		 * @param idShort	추가할 idShort.
		 * @return	이 빌더 객체.
		 */
		public Builder idShort(String idShort) {
			m_idShortList.add(new KeyIdShort(idShort));
			return this;
		}

		/**
		 * 경로 끝에 인덱스 단계({@link IndexIdShort})를 추가한다.
		 *
		 * @param index	추가할 인덱스(0 이상).
		 * @return	이 빌더 객체.
		 * @throws IllegalArgumentException	{@code index}가 음수인 경우.
		 * @throws IllegalStateException	경로가 비어 있는 경우(경로는 인덱스로 시작할 수 없다).
		 */
		public Builder index(int index) {
			Preconditions.checkArgument(index >= 0, "index must be non-negative");
			Preconditions.checkState(m_idShortList.size() > 0, "IdShortPath should not start with IndexIdShort");
			
			m_idShortList.add(new IndexIdShort(index));
			return this;
		}
	}

	/**
	 * idShort 경로의 한 단계를 표현하는 추상 클래스.
	 */
	public static abstract class IdShort {
		/**
		 * 이 단계의 키(idShort 또는 인덱스 문자열)를 반환한다.
		 *
		 * @return	키 문자열.
		 */
		public abstract String getKey();
		/**
		 * 주어진 부모 원소에서 이 단계에 해당하는 하위 원소를 반환한다.
		 *
		 * @param parent	부모 SubmodelElement.
		 * @return	하위 SubmodelElement.
		 * @throws IllegalArgumentException	{@code parent}가 {@code null}이거나 이 단계에 맞는 종류가 아닌 경우.
		 * @throws ResourceNotFoundException	해당하는 하위 원소가 없는 경우.
		 */
		public abstract SubmodelElement getChild(SubmodelElement parent);
		
		/**
		 * {@link SubmodelElementCollection}의 하위 원소를 idShort로 가리키는 단계.
		 */
		public static class KeyIdShort extends IdShort {
			private final String m_key;

			/**
			 * 주어진 idShort를 가리키는 단계를 생성한다.
			 *
			 * @param key	idShort.
			 */
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
		
		/**
		 * {@link SubmodelElementList}의 하위 원소를 인덱스로 가리키는 단계.
		 */
		public static class IndexIdShort extends IdShort {
			private final int m_index;

			/**
			 * 주어진 인덱스를 가리키는 단계를 생성한다.
			 *
			 * @param index	인덱스.
			 */
			public IndexIdShort(int index) {
				m_index = index;
			}

			@Override
			public String getKey() {
				return "" + m_index;
			}

			/**
			 * 인덱스를 반환한다.
			 *
			 * @return	인덱스.
			 */
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
