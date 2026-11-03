package mdt.model.sm.info;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 * {@link InformationModel} Submodel의 기본 구현 엔티티.
 * <p>
 * {@code MDTInfo}({@link DefaultMDTInfo})와 {@code TwinComposition}({@link DefaultTwinComposition})
 * SubmodelElementCollection을 필드로 갖는다. 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultInformationModel extends SubmodelEntity implements InformationModel {
	@SMCollectionField(idShort="MDTInfo", adaptorClass=DefaultMDTInfo.class)
	private MDTInfo MDTInfo;
	@SMCollectionField(idShort="TwinComposition", adaptorClass=DefaultTwinComposition.class)
	private TwinComposition twinComposition = new DefaultTwinComposition();
	
	/**
	 * 주어진 InformationModel Submodel로부터 {@link DefaultInformationModel} 객체를 생성한다.
	 *
	 * @param submodel	InformationModel Submodel.
	 * @return	생성된 {@link DefaultInformationModel} 객체.
	 */
	public static DefaultInformationModel from(Submodel submodel) {
		DefaultInformationModel infoModel = new DefaultInformationModel();
		infoModel.updateFromAasModel(submodel);
		
		return infoModel;
	}
	
	/**
	 * idShort가 {@link InformationModel#IDSHORT}이고 semanticId가
	 * {@link InformationModel#SEMANTIC_ID_REFERENCE}인 빈 InformationModel 엔티티를 생성한다.
	 */
	public DefaultInformationModel() {
		setIdShort(IDSHORT);
		setSemanticId(SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), this.MDTInfo.getAssetName());
	}
}
