package mdt.model.instance;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Preconditions;


/**
 * MDT 트윈 컴포지션의 인터페이스를 정의한다.
 * <p>
 * MDT 트윈 컴포지션는 MDT 모델의 구성요소이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"id", "type", "compositionItems", "compositionDependencies"})
public class MDTTwinCompositionDescriptor {
	private final String m_id;
	private final String m_type;
	private final List<MDTCompositionItem> m_items;
	private final List<MDTCompositionDependency> m_dependencies;
	
	public MDTTwinCompositionDescriptor(@JsonProperty("id") String id,
												@JsonProperty("type") String type,
												@JsonProperty("compositionItems") List<MDTCompositionItem> items,
												@JsonProperty("compositionDependencies") List<MDTCompositionDependency> dependencies) {
		Preconditions.checkArgument(id != null, "TwinComposition id is null");
		Preconditions.checkArgument(type != null, "TwinComposition type is null");
		Preconditions.checkArgument(items != null, "CompositionItems is null");
		Preconditions.checkArgument(dependencies != null, "CompositionDependencies is null");
		
		m_id = id;
		m_type = type;
		m_items = items;
		m_dependencies = dependencies;
	}

	/**
	 * MDT 트윈 컴포지션의 식별자를 반환한다.
	 * 
	 * @return	트윈 컴포지션 식별자
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * MDT 트윈 컴포지션의 타입을 반환한다.
	 *
	 * @return 트윈 컴포지션 타입
	 */
	public String getType() {
		return m_type;
	}

	/**
	 * MDT 트윈 컴포지션에 포함된 항목 목록을 반환한다.
	 * 
	 * @return	항목 목록
	 */
	public List<MDTCompositionItem> getCompositionItems() {
		return m_items;
	}

	/**
	 * MDT 트윈 컴포지션에 포함된 항목들 간의 의존관계 목록을 반환한다.
	 * 
	 * @return	의존관계 목록
	 */
	public List<MDTCompositionDependency> getCompositionDependencies() {
		return m_dependencies;
	}
	
	@JsonPropertyOrder({"id", "reference"})
	public static class MDTCompositionItem {
		private final String m_id;
		private final String m_reference;
		
		public MDTCompositionItem(@JsonProperty("id") String id, @JsonProperty("reference") String reference) {
			m_id = id;
			m_reference = reference;
		}
		
		public String getId() {
			return m_id;
		}
		
		public String getReference() {
			return m_reference;
		}
	}

	@JsonPropertyOrder({"type", "sourceItem", "targetItem"})
	public static class MDTCompositionDependency {
		private final String m_type;
		private final String m_sourceItem;
		private final String m_targetItem;
		
		public MDTCompositionDependency(@JsonProperty("type") String type, @JsonProperty("sourceItem") String sourceItem,
										@JsonProperty("targetItem") String targetItem) {
			m_type = type;
			m_sourceItem = sourceItem;
			m_targetItem = targetItem;
		}
		
		public String getType() {
			return m_type;
		}
		
		public String getSourceItem() {
			return m_sourceItem;
		}
		
		public String getTargetItem() {
			return m_targetItem;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s -> %s", m_type, m_sourceItem, m_targetItem);
		}
	}
}
