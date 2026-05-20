package mdt.model;

import mdt.aas.SubmodelRegistry;
import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.WorkflowManager;

/**
 * {@link MDTManager}는 MDT 시스템의 최상위 관리자 인터페이스를 정의한다.
 * <p>
 * {@code MDTManager}는 MDT 시스템에서 제공하는 다양한 서비스에 대한 진입점 역할을 수행하며,
 * MDTInstance 관리자 및 Submodel 레지스트리 등 주요 하위 관리자에 대한 접근을 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTManager {
	/**
	 * 주어진 서비스 클래스에 해당하는 서비스 객체를 반환한다.
	 *
	 * @param <T>		반환할 서비스 객체의 타입.
	 * @param svcClass	검색할 서비스 클래스.
	 * @return			주어진 클래스에 해당하는 서비스 객체.
	 * 					해당 서비스가 존재하지 않는 경우 {@code null}을 반환할 수 있다.
	 */
	public <T> T getService(Class<T> svcClass);

	/**
	 * MDTInstance 관리자({@link MDTInstanceManager})를 반환한다.
	 *
	 * @return	{@link MDTInstanceManager} 객체.
	 */
	public MDTInstanceManager getInstanceManager();

	/**
	 * Submodel 레지스트리({@link SubmodelRegistry})를 반환한다.
	 *
	 * @return	{@link SubmodelRegistry} 객체.
	 */
	public SubmodelRegistry getSubmodelRegistry();

	/**
	 * 워크플로우 관리자({@link WorkflowManager})를 반환한다.
	 *
	 * @return	{@link WorkflowManager} 객체.
	 */
	public WorkflowManager getWorkflowManager();
}
