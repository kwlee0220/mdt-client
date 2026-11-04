package mdt.model.sm.data;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import lombok.experimental.Delegate;

import utils.Preconditions;

import mdt.model.SubmodelService;


/**
 * {@code DataSubmodelService}는 Data 서브모델에 대한 {@link SubmodelService}에
 * Data 서브모델 전용 접근 기능을 추가한 래퍼 클래스이다.
 * <p>
 * {@link SubmodelService}의 모든 메소드는 주어진 서비스 객체에 위임되며, 생성 시점에 서브모델을 읽어
 * {@link Data} 객체로 변환해 둔다. 따라서 {@link #getData()} 등이 반환하는 정보는 생성 시점의 내용이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DataSubmodelService implements SubmodelService {
	@Delegate private final SubmodelService m_service;
	private final DefaultData m_data;
	
	/**
	 * 주어진 서브모델 서비스를 감싸는 {@code DataSubmodelService} 객체를 생성한다.
	 * <p>
	 * 생성 시 {@code service}로부터 서브모델을 읽어 {@link Data} 객체로 변환한다.
	 *
	 * @param service	Data 서브모델에 대한 서브모델 서비스.
	 * @throws IllegalArgumentException	{@code service}가 {@code null}이거나, 서브모델의 semantic id가 없거나
	 * 			{@link Data#SEMANTIC_ID_REFERENCE}가 아닌 경우.
	 */
	public DataSubmodelService(SubmodelService service) {
		Preconditions.checkNotNullArgument(service, "SubmodelService is null");
		
		Submodel submodel = service.getSubmodel();
		Preconditions.checkArgument(Data.SEMANTIC_ID_REFERENCE.equals(submodel.getSemanticId()),
									"Not Data Submodel: id=%s, semanticId=%s",
									submodel.getId(), submodel.getSemanticId());
		
		m_service = service;
		
		m_data = new DefaultData();
		m_data.updateFromAasModel(submodel);
	}
	
	/**
	 * 생성 시점에 읽은 Data 서브모델 객체를 반환한다.
	 *
	 * @return	{@link Data} 객체.
	 */
	public Data getData() {
		return m_data;
	}
	
	/**
	 * DataSubmodel에 {@link Equipment}이 포함되어 있는지 여부를 반환한다.
	 *
	 * @return	Equipment가 포함되어 있으면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public boolean containsEquipment() {
		return m_data.getDataInfo().isEquipment();
	}
	
	/**
	 * DataSubmodel에 {@link Operation}이 포함되어 있는지 여부를 반환한다.
	 *
	 * @return	Operation이 포함되어 있으면 {@code true}, 그렇지 않으면 {@code false}.
	 */
	public boolean containsOperation() {
		return m_data.getDataInfo().isOperation();
	}
	
	/**
	 * DataSubmodel에서 정의된 모든 Parameter들의 컬렉션을 반환한다.
	 * <p>
	 * {@code DataInfo}에 포함된 첫번째 {@link ParameterCollection} 엔티티 ({@link Equipment} 또는
	 * {@link Operation})를 반환한다.
	 *
	 * @return	Parameter 컬렉션 객체.
	 * @throws mdt.model.ResourceNotFoundException	{@link ParameterCollection} 엔티티가 없는 경우.
	 */
	public ParameterCollection getParameterAll() {
		return m_data.getDataInfo().getFirstSubmodelElementEntityByClass(ParameterCollection.class);
    }
}
