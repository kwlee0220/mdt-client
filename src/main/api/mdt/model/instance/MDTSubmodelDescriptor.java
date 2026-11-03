package mdt.model.instance;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.Preconditions;

/**
 * MDTInstance에 포함된 Submodel 하나의 정보를 담는 디스크립터.
 * <p>
 * Submodel의 식별자, idShort, semanticId와 Submodel 서비스 endpoint를 갖는다.
 * 두 디스크립터는 Submodel 식별자가 같으면 동일한 것으로 간주된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"id", "idShort", "semanticId", "endpoint"})
public final class MDTSubmodelDescriptor {
	private final String m_id;
	private final String m_idShort;
	private final String m_semanticId;
	private String m_endpoint;
	
	/**
	 * Submodel 디스크립터를 생성한다.
	 * <p>
	 * endpoint는 설정되지 않은 상태({@code null})로 생성된다.
	 *
	 * @param id			Submodel 식별자.
	 * @param idShort		Submodel의 idShort.
	 * @param semanticId	Submodel의 semanticId. {@code null}이 허용된다.
	 * @throws IllegalArgumentException	{@code id} 또는 {@code idShort}가 {@code null}인 경우.
	 */
	public MDTSubmodelDescriptor(@JsonProperty("id") String id,
								@JsonProperty("idShort") String idShort,
								@Nullable @JsonProperty("semanticId") String semanticId) {
		Preconditions.checkNotNullArgument(id, "null id");
		Preconditions.checkNotNullArgument(idShort, "null idShort");
		
		m_id = id;
		m_idShort = idShort;
		m_semanticId = semanticId;
		m_endpoint = null;
	}

	/**
	 * Submodel의 식별자를 반환한다.
	 *
	 * @return	Submodel 식별자.
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * Submodel의 idShort를 반환한다.
	 *
	 * @return	idShort.
	 */
	public String getIdShort() {
		return m_idShort;
	}

	/**
	 * Submodel의 semanticId를 반환한다.
	 *
	 * @return	semanticId. 지정되지 않은 경우 {@code null}.
	 */
	public String getSemanticId() {
		return m_semanticId;
	}
	
	/**
	 * Submodel 서비스의 endpoint를 반환한다.
	 *
	 * @return	endpoint URL. 설정되지 않은 경우 {@code null}.
	 */
	public String getEndpoint() {
		return m_endpoint;
	}
	
	/**
	 * Submodel 서비스의 endpoint를 설정한다.
	 *
	 * @param endpoint	endpoint URL.
	 */
	public void setEndpoint(String endpoint) {
		m_endpoint = endpoint;
	}
	
	@Override
	public int hashCode() {
		return m_id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		else if ( obj == null || getClass() != obj.getClass() ) {
            return false;
		}
		
		MDTSubmodelDescriptor other = (MDTSubmodelDescriptor)obj;
		return m_id.equals(other.m_id);
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s): %s", getClass().getSimpleName(), m_idShort, m_endpoint);
	}
}
