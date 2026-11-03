package mdt.model.sm.data;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import mdt.model.MDTSemanticIds;


/**
 * {@code Operation}은 Data 서브모델에 정의된 공정 정보와 공정 파라미터들을 정의한다.
 * <p>
 * 공정 파라미터 ({@link OperationParameter})와 파라미터 값 ({@link OperationParameterValue})은
 * {@link ParameterCollection} 인터페이스를 통해 접근한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface Operation extends ParameterCollection {
	/**
	 * Operation SubmodelElementCollection의 semantic id 문자열.
	 */
	public static final String SEMANTIC_ID = MDTSemanticIds.OPERATION;
	/**
	 * {@link #SEMANTIC_ID}에 해당하는 semantic id {@link Reference}.
	 */
	public static final Reference SEMANTIC_ID_REFERENCE = MDTSemanticIds.toReference(SEMANTIC_ID);
	
	/**
	 * 공정 식별자({@code OperationID})를 반환한다.
	 *
	 * @return	공정 식별자.
	 */
	public String getOperationId();
	/**
	 * 공정 식별자({@code OperationID})를 설정한다.
	 *
	 * @param id	설정할 공정 식별자.
	 */
	public void setOperationId(String id);
	
	/**
	 * 공정 이름({@code OperationName})을 반환한다.
	 *
	 * @return	공정 이름.
	 */
	public String getOperationName();
	/**
	 * 공정 이름({@code OperationName})을 설정한다.
	 *
	 * @param name	설정할 공정 이름.
	 */
	public void setOperationName(String name);

	/**
	 * 공정 유형({@code OperationType})을 반환한다.
	 *
	 * @return	공정 유형.
	 */
	public String getOperationType();
	/**
	 * 공정 유형({@code OperationType})을 설정한다.
	 *
	 * @param operationType	설정할 공정 유형.
	 */
	public void setOperationType(String operationType);

	/**
	 * 공정 사용 여부 표시자({@code UseIndicator})를 반환한다.
	 *
	 * @return	공정 사용 여부 표시자.
	 */
	public String getUseIndicator();
	/**
	 * 공정 사용 여부 표시자({@code UseIndicator})를 설정한다.
	 *
	 * @param useIndicator	설정할 공정 사용 여부 표시자.
	 */
	public void setUseIndicator(String useIndicator);
	
	/**
	 * 공정에 할당된 생산 오더 목록 ({@code ProductionOrders})을 반환한다.
	 *
	 * @return	{@link ProductionOrder} 객체 목록.
	 */
	public List<ProductionOrder> getProductionOrders();
	/**
	 * 공정에 할당된 생산 오더 목록 ({@code ProductionOrders})을 설정한다.
	 *
	 * @param orders	설정할 {@link ProductionOrder} 객체 목록.
	 */
	public void setProductionOrders(List<ProductionOrder> orders);
	
//	public String getOperationStartDateTime();
//	public void setOperationStartDateTime(String dtStr);
//	
//	public String getOperationEndDateTime();
//	public void setOperationEndDateTime(String dtStr);
//	
//	public String getEventDateTime();
//	public void setEventDateTime(String dtStr);
}
