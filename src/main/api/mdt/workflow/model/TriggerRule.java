package mdt.workflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import utils.stream.FStream;


/**
 * 직전 task들의 종료 상태에 따라 task를 실행할지 정하는 규칙.
 * <p>
 * Airflow의 trigger rule 중 워크플로에서 쓰임새가 분명한 것만 허용한다. 판정 대상은 직전(직접 upstream)
 * task뿐이며, 조건이 맞지 않으면 task는 실행되지 않고 {@code skipped} 또는 {@code upstream_failed}로 끝난다.
 * JSON에는 Airflow의 규칙 이름(예: {@code "none_failed_min_one_success"})으로 기록된다.
 * <p>
 * 주의: Airflow는 DAG 실행의 최종 상태를 마지막(leaf) task들의 상태로 정한다. {@link #ALL_DONE}이나
 * {@link #ONE_FAILED} task가 마지막에 성공하면, 중간 task가 실패했어도 DAG 실행이 성공으로 끝날 수 있다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public enum TriggerRule {
	/** 직전 task가 모두 성공하면 실행한다. 기본값. */
	ALL_SUCCESS("all_success"),
	/** 직전 task 중 실패가 없고 하나 이상 성공하면 실행한다. 분기 뒤 경로가 합류하는 task에 쓴다. */
	NONE_FAILED_MIN_ONE_SUCCESS("none_failed_min_one_success"),
	/** 직전 task가 상태와 관계없이 모두 끝나면 실행한다. 정리·마무리 task에 쓴다. */
	ALL_DONE("all_done"),
	/** 직전 task 중 하나라도 실패하면 나머지를 기다리지 않고 즉시 실행한다. 실패 처리 task에 쓴다. */
	ONE_FAILED("one_failed");

	private final String m_name;

	TriggerRule(String name) {
		m_name = name;
	}

	/**
	 * Airflow의 trigger rule 이름을 반환한다.
	 *
	 * @return	규칙 이름 (예: {@code "all_success"}).
	 */
	@JsonValue
	public String getName() {
		return m_name;
	}

	/**
	 * Airflow의 trigger rule 이름에 해당하는 규칙을 반환한다.
	 *
	 * @param name	규칙 이름.
	 * @return	규칙.
	 * @throws IllegalArgumentException	지원하지 않는 이름인 경우.
	 */
	@JsonCreator
	public static TriggerRule fromName(String name) {
		for ( TriggerRule rule: values() ) {
			if ( rule.m_name.equals(name) ) {
				return rule;
			}
		}
		String supported = FStream.of(values()).map(TriggerRule::getName).join(", ");
		throw new IllegalArgumentException(String.format("unsupported trigger rule: %s (supported: %s)",
															name, supported));
	}
}
