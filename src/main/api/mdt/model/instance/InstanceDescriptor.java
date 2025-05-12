package mdt.model.instance;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * <code>InstanceDescriptor</code>은 MDTInstance의 등록정보를 정의하는 인터페이스이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonSerialize(using=InstanceDescriptorSerDe.Serializer.class)
@JsonDeserialize(using=InstanceDescriptorSerDe.Deserializer.class)
public interface InstanceDescriptor {
	/**
	 * 대상 MDTInstance의 식별자를 반환한다.
	 * 
	 * @return	식별자.
	 */
	public String getId();
	
	/**
	 * 대상 MDTInstance의 상태를 반환한다.
	 * 
	 * @return	상태 정보
	 */
	public MDTInstanceStatus getStatus();
	
	/**
	 * 대상 MDTInstance에 부여된 기반 endpoint를 반환한다.
	 * 대상 MDTInstance의 상태가 {@link MDTInstanceStatus#RUNNING}이 아닌 경우는
	 * {@code null}이 반환된다.
	 */
	@Nullable public String getBaseEndpoint();
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 식별자를 반환한다.
	 * 
	 * @return	AAS 식별자.
	 */
	public String getAasId();
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 idShort를 반환한다.
	 * 
	 * @return	idShort.
	 */
	@Nullable public String getAasIdShort();
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 GlobalAssetId 를 반환한다.
	 * 
	 * @return	자산 식별자.
	 */
	@Nullable public String getGlobalAssetId();
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 타입을 반환한다.
	 * 
	 * @return	자산 타입.
	 */
	@Nullable public String getAssetType();
	
	/**
	 * 대상 MDTInstance가 포함한 AssetAdministrationShell의 자산 종류를 반환한다.
	 * 
	 * @return	자산 종류.
	 */
	@Nullable public AssetKind getAssetKind();
	
	/**
	 * 대상 MDTInstance가 포함한 모든 Submodel들의 descriptor들을 반환한다.
	 * 
	 * @return	{@link InstanceSubmodelDescriptor}들의 리스트.
	 */
	public List<InstanceSubmodelDescriptor> getInstanceSubmodelDescriptorAll();
	
	/**
	 * 대상 MDTInstance가 포함한 모든 파라미터들의 descriptor들을 반환한다.
	 * 
	 * @return {@link MDTParameterDescriptor}들의 리스트.
	 */
	public List<MDTParameterDescriptor> getMDTParameterDescriptorAll();
	
	/**
	 * 대상 MDTInstance가 포함한 모든 작업들의 descriptor들을 반환한다.
	 * 
	 * @return {@link MDTOperationDescriptor}들의 리스트.
	 */
	public List<MDTOperationDescriptor> getMDTOperationDescriptorAll();
}