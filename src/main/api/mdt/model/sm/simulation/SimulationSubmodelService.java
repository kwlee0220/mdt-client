package mdt.model.sm.simulation;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import mdt.model.SubmodelService;
import mdt.model.sm.OperationSubmodelService;


/**
 * Simulation Submodel에 대한 {@link OperationSubmodelService}.
 * <p>
 * 입출력 인자는 {@code SimulationInfo.Inputs}/{@code SimulationInfo.Outputs}에 저장된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SimulationSubmodelService extends OperationSubmodelService {
	/**
	 * 주어진 Simulation Submodel 서비스를 감싸는 객체를 생성한다.
	 *
	 * @param service	대상 Simulation Submodel 서비스.
	 */
	public SimulationSubmodelService(SubmodelService service) {
		super(service);
	}
	
	/**
	 * 원격 Submodel 전체를 읽어 {@link Simulation} 엔티티로 변환하여 반환한다.
	 *
	 * @return	{@link Simulation} 엔티티 ({@link DefaultSimulation}).
	 */
	public Simulation getSimulation() {
		Submodel submodel = m_service.getSubmodel();
		DefaultSimulation sim = new DefaultSimulation();
		sim.updateFromAasModel(submodel);
		
		return sim;
	}
}
