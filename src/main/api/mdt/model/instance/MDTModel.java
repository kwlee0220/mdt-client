package mdt.model.instance;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import mdt.model.sm.info.MDTAssetType;

/**
 * MDTInstance의 MDT 모델 전체 정보를 담는 JSON 전송용 객체.
 * <p>
 * {@link InstanceDescriptor}에 담긴 기본 정보에 더하여, MDTInstance에 포함된 Submodel,
 * 파라미터, 연산, 그리고 트윈 구성(twin composition) 정보를 함께 담는다.
 * 기본 정보는 생성자에서 {@link InstanceDescriptor}로부터 복사되며, 나머지 정보는 setter로 설정한다.
 * JSON 직렬화 시에는 {@code null}인 필드도 모두 출력된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"id", "status", "baseEndpoint", "aasId", "aasIdShort", "globalAssetId", "assetType",
					"submodels", "parameters", "operations", "twinComposition"})
public class MDTModel {
	private String m_id;
	private MDTInstanceStatus m_status;
	private @Nullable String m_baseEndpoint;
	
	private String m_aasId;
	private @Nullable String m_aasIdShort;
	private @Nullable String m_globalAssetId;
	private MDTAssetType m_assetType;
	
	private List<MDTSubmodelDescriptor> m_submodels;
	private List<MDTParameterDescriptor> m_parameters;
	private List<MDTOperationDescriptor> m_operations;
	private MDTTwinCompositionDescriptor m_twinComposition;
	
	/**
	 * 주어진 {@link InstanceDescriptor}의 기본 정보를 복사하여 MDT 모델 객체를 생성한다.
	 * <p>
	 * Submodel/파라미터/연산/트윈 구성 정보는 설정되지 않은 상태({@code null})로 생성된다.
	 *
	 * @param desc	기본 정보를 담은 {@link InstanceDescriptor}.
	 */
	public MDTModel(InstanceDescriptor desc) {
		m_id = desc.getId();
		m_status = desc.getStatus();
		m_baseEndpoint = desc.getBaseEndpoint();
		
		m_aasId = desc.getAasId();
		m_aasIdShort = desc.getAasIdShort();
		m_globalAssetId = desc.getGlobalAssetId();
		m_assetType = desc.getAssetType();
	}
	
	/**
	 * MDTInstance의 식별자를 반환한다.
	 *
	 * @return	식별자.
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * MDTInstance의 식별자를 설정한다.
	 *
	 * @param id	식별자.
	 */
	public void setId(String id) {
		m_id = id;
	}
	
	/**
	 * MDTInstance의 상태를 반환한다.
	 *
	 * @return	상태 정보.
	 */
	public MDTInstanceStatus getStatus() {
		return m_status;
	}
	
	/**
	 * MDTInstance의 상태를 설정한다.
	 *
	 * @param status	상태 정보.
	 */
	public void setStatus(MDTInstanceStatus status) {
		m_status = status;
	}
	
	/**
	 * MDTInstance에 부여된 기반 endpoint를 반환한다.
	 *
	 * @return	기반 endpoint URL. 설정되지 않은 경우 {@code null}.
	 */
	public @Nullable String getBaseEndpoint() {
		return m_baseEndpoint;
	}
	
	/**
	 * MDTInstance에 부여된 기반 endpoint를 설정한다.
	 *
	 * @param baseEndpoint	기반 endpoint URL.
	 */
	public void setBaseEndpoint(@Nullable String baseEndpoint) {
		m_baseEndpoint = baseEndpoint;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 식별자를 반환한다.
	 *
	 * @return	AAS 식별자.
	 */
	public String getAasId() {
		return m_aasId;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 식별자를 설정한다.
	 *
	 * @param aasId	AAS 식별자.
	 */
	public void setAasId(String aasId) {
		m_aasId = aasId;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 *
	 * @return	idShort. 설정되지 않은 경우 {@code null}.
	 */
	public @Nullable String getAasIdShort() {
		return m_aasIdShort;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 idShort를 설정한다.
	 *
	 * @param aasIdShort	idShort.
	 */
	public void setAasIdShort(@Nullable String aasIdShort) {
		m_aasIdShort = aasIdShort;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId를 반환한다.
	 *
	 * @return	자산 식별자. 설정되지 않은 경우 {@code null}.
	 */
	public @Nullable String getGlobalAssetId() {
		return m_globalAssetId;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId를 설정한다.
	 *
	 * @param globalAssetId	자산 식별자.
	 */
	public void setGlobalAssetId(@Nullable String globalAssetId) {
		m_globalAssetId = globalAssetId;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 *
	 * @return	자산 타입.
	 */
	public MDTAssetType getAssetType() {
		return m_assetType;
	}
	
	/**
	 * MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 설정한다.
	 *
	 * @param assetType	자산 타입.
	 */
	public void setAssetType(MDTAssetType assetType) {
		m_assetType = assetType;
	}
	
	/**
	 * MDTInstance에 포함된 Submodel들의 디스크립터 목록을 반환한다.
	 *
	 * @return	Submodel 디스크립터 목록. 설정되지 않은 경우 {@code null}.
	 */
	public List<MDTSubmodelDescriptor> getSubmodels() {
		return m_submodels;
	}
	
	/**
	 * MDTInstance에 포함된 Submodel들의 디스크립터 목록을 설정한다.
	 *
	 * @param submodels	Submodel 디스크립터 목록.
	 */
	public void setSubmodels(List<MDTSubmodelDescriptor> submodels) {
		m_submodels = submodels;
	}
	
	/**
	 * MDTInstance의 파라미터 디스크립터 목록을 반환한다.
	 *
	 * @return	파라미터 디스크립터 목록. 설정되지 않은 경우 {@code null}.
	 */
	public List<MDTParameterDescriptor> getParameters() {
		return m_parameters;
	}
	
	/**
	 * MDTInstance의 파라미터 디스크립터 목록을 설정한다.
	 *
	 * @param parameters	파라미터 디스크립터 목록.
	 */
	public void setParameters(List<MDTParameterDescriptor> parameters) {
		m_parameters = parameters;
	}
	
	/**
	 * MDTInstance가 제공하는 연산 디스크립터 목록을 반환한다.
	 *
	 * @return	연산 디스크립터 목록. 설정되지 않은 경우 {@code null}.
	 */
	public List<MDTOperationDescriptor> getOperations() {
		return m_operations;
	}
	
	/**
	 * MDTInstance가 제공하는 연산 디스크립터 목록을 설정한다.
	 *
	 * @param operations	연산 디스크립터 목록.
	 */
	public void setOperations(List<MDTOperationDescriptor> operations) {
		m_operations = operations;
	}
	
	/**
	 * MDTInstance의 트윈 구성 디스크립터를 반환한다.
	 *
	 * @return	트윈 구성 디스크립터. 설정되지 않은 경우 {@code null}.
	 */
	public MDTTwinCompositionDescriptor getTwinComposition() {
		return m_twinComposition;
	}
	
	/**
	 * MDTInstance의 트윈 구성 디스크립터를 설정한다.
	 *
	 * @param twinComposition	트윈 구성 디스크립터.
	 */
	public void setTwinComposition(MDTTwinCompositionDescriptor twinComposition) {
		m_twinComposition = twinComposition;
	}
}
