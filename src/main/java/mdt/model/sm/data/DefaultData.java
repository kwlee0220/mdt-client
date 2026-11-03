package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultSubModelInfo;
import mdt.model.SubModelInfo;
import mdt.model.sm.entity.SMCollectionField;
import mdt.model.sm.entity.SubmodelEntity;


/**
 * {@link Data} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * idShort가 {@code Data}인 서브모델에 대응되며, {@code SubModelInfo}와 {@code DataInfo}
 * SubmodelElementCollection을 각각 {@link SubModelInfo}와 {@link DataInfo} 객체로 관리한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultData extends SubmodelEntity implements Data {
	private static final String IDSHORT = "Data";
	
	@SMCollectionField(idShort="SubModelInfo", adaptorClass=DefaultSubModelInfo.class)
	private SubModelInfo subModelInfo;

	@SMCollectionField(idShort="DataInfo", adaptorClass=DefaultDataInfo.class)
	private DataInfo dataInfo;
	
	/**
	 * 주어진 AAS 서브모델로부터 {@code DefaultData} 객체를 생성한다.
	 *
	 * @param submodel	Data 서브모델.
	 * @return	생성된 {@code DefaultData} 객체.
	 */
	public static DefaultData from(Submodel submodel) {
		DefaultData data = new DefaultData();
		data.updateFromAasModel(submodel);
		
		return data;
	}
	
	/**
	 * idShort가 {@code Data}이고 semantic id가 {@link Data#SEMANTIC_ID_REFERENCE}인
	 * 빈 {@code DefaultData} 객체를 생성한다.
	 */
	public DefaultData() {
		setIdShort(IDSHORT);
		setSemanticId(Data.SEMANTIC_ID_REFERENCE);
	}
	
	@Override
	public String toString() {
		return String.format(IDSHORT);
	}
}
