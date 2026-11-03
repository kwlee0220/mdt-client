package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.Preconditions;


/**
 * MDT 트윈 컴포지션(twin composition)의 정보를 담는 디스크립터.
 * <p>
 * MDT 트윈 컴포지션은 MDT 모델의 구성요소로서, 하나의 트윈을 구성하는 항목들
 * ({@link MDTCompositionItem})과 항목들 간의 의존관계({@link MDTCompositionDependency})를 기술한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "type", "compositionItems", "compositionDependencies"})
public class MDTTwinCompositionDescriptor {
	private final String m_id;
	private final String m_type;
	private final List<MDTCompositionItem> m_items;
	private final List<MDTCompositionDependency> m_dependencies;
	
	/**
	 * 트윈 컴포지션 디스크립터를 생성한다.
	 *
	 * @param id			트윈 컴포지션 식별자.
	 * @param type			트윈 컴포지션 타입.
	 * @param items			구성 항목 목록.
	 * @param dependencies	구성 항목들 간의 의존관계 목록.
	 * @throws IllegalArgumentException	인자 중 하나라도 {@code null}인 경우.
	 */
	public MDTTwinCompositionDescriptor(@JsonProperty("id") String id,
												@JsonProperty("type") String type,
												@JsonProperty("compositionItems") List<MDTCompositionItem> items,
												@JsonProperty("compositionDependencies") List<MDTCompositionDependency> dependencies) {
		Preconditions.checkNotNullArgument(id, "TwinComposition id is null");
		Preconditions.checkNotNullArgument(type, "TwinComposition type is null");
		Preconditions.checkNotNullArgument(items, "CompositionItems is null");
		Preconditions.checkNotNullArgument(dependencies, "CompositionDependencies is null");
		
		m_id = id;
		m_type = type;
		m_items = items;
		m_dependencies = dependencies;
	}

	/**
	 * MDT 트윈 컴포지션의 식별자를 반환한다.
	 * 
	 * @return	트윈 컴포지션 식별자.
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * MDT 트윈 컴포지션의 타입을 반환한다.
	 *
	 * @return	트윈 컴포지션 타입.
	 */
	public String getType() {
		return m_type;
	}

	/**
	 * MDT 트윈 컴포지션에 포함된 항목 목록을 반환한다.
	 * 
	 * @return	항목 목록.
	 */
	public List<MDTCompositionItem> getCompositionItems() {
		return m_items;
	}

	/**
	 * MDT 트윈 컴포지션에 포함된 항목들 간의 의존관계 목록을 반환한다.
	 * 
	 * @return	의존관계 목록.
	 */
	public List<MDTCompositionDependency> getCompositionDependencies() {
		return m_dependencies;
	}
	
	/**
	 * 트윈 컴포지션을 구성하는 항목 하나를 나타낸다.
	 */
	@JsonPropertyOrder({"id", "reference"})
	public static class MDTCompositionItem {
		private final String m_id;
		private final String m_reference;
		
		/**
		 * 구성 항목 객체를 생성한다.
		 *
		 * @param id		항목 식별자.
		 * @param reference	항목이 가리키는 대상의 참조 문자열.
		 */
		public MDTCompositionItem(@JsonProperty("id") String id, @JsonProperty("reference") String reference) {
			m_id = id;
			m_reference = reference;
		}
		
		/**
		 * 항목 식별자를 반환한다.
		 *
		 * @return	항목 식별자.
		 */
		public String getId() {
			return m_id;
		}
		
		/**
		 * 항목이 가리키는 대상의 참조 문자열을 반환한다.
		 *
		 * @return	참조 문자열.
		 */
		public String getReference() {
			return m_reference;
		}
	}

	/**
	 * 트윈 컴포지션의 두 구성 항목 간 의존관계를 나타낸다.
	 */
	@JsonPropertyOrder({"type", "sourceItem", "targetItem"})
	public static class MDTCompositionDependency {
		private final String m_type;
		private final String m_sourceItem;
		private final String m_targetItem;
		
		/**
		 * 의존관계 객체를 생성한다.
		 *
		 * @param type			의존관계 타입.
		 * @param sourceItem	의존관계의 시작 항목 식별자.
		 * @param targetItem	의존관계의 대상 항목 식별자.
		 */
		public MDTCompositionDependency(@JsonProperty("type") String type, @JsonProperty("sourceItem") String sourceItem,
										@JsonProperty("targetItem") String targetItem) {
			m_type = type;
			m_sourceItem = sourceItem;
			m_targetItem = targetItem;
		}
		
		/**
		 * 의존관계 타입을 반환한다.
		 *
		 * @return	의존관계 타입.
		 */
		public String getType() {
			return m_type;
		}
		
		/**
		 * 의존관계의 시작 항목 식별자를 반환한다.
		 *
		 * @return	시작 항목 식별자.
		 */
		public String getSourceItem() {
			return m_sourceItem;
		}
		
		/**
		 * 의존관계의 대상 항목 식별자를 반환한다.
		 *
		 * @return	대상 항목 식별자.
		 */
		public String getTargetItem() {
			return m_targetItem;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s -> %s", m_type, m_sourceItem, m_targetItem);
		}
	}
}
