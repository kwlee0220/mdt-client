package mdt.task;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.Preconditions;

import mdt.model.instance.MDTInstanceManager;
import mdt.workflow.model.TaskDescriptor;

/**
 * 원본 인자의 값을 대상 참조 인자가 가리키는 MDT 요소에 기록하는 {@link MDTTask} 구현체.
 * <p>
 * 원본은 참조 또는 리터럴 인자이고, 대상은 참조 인자여야 한다. 작업은 즉시 끝나므로 취소를 지원하지 않는다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SetTask implements MDTTask {
	/** 원본 인자의 이름. */
	public static final String ARG_SOURCE = "source";
	/** 대상 인자의 이름. {@link #run(MDTInstanceManager)} 결과 맵의 키로도 사용된다. */
	public static final String ARG_TARGET = "target";
	
	private final TaskArgument m_source;
	private final TaskArgument.ReferenceArgument m_target;
	
	private SetTask(Builder builder) {
		Preconditions.checkArgument(builder.m_source != null, "source argument is not specified");
		Preconditions.checkArgument(builder.m_target != null, "target argument is not specified");
		
		m_source = builder.m_source;
		m_target = builder.m_target;
	}

	/**
	 * Builder로 만든 태스크는 기술자가 없으므로 항상 {@code null}을 반환한다.
	 *
	 * @return	항상 {@code null}.
	 */
	@Override
	public TaskDescriptor getTaskDescriptor() {
		return null;
	}

	/**
	 * 원본 인자의 값을 읽어 대상 인자가 가리키는 요소의 값으로 갱신한다.
	 *
	 * @param manager	인자의 참조를 활성화할 MDT 인스턴스 관리자.
	 * @return	{@link #ARG_TARGET}을 키로, 갱신 후 다시 읽은 대상 요소를 값으로 하는 맵.
	 * @throws TaskException	값을 읽거나 쓰는 중 입출력 오류가 발생한 경우.
	 */
	@Override
	public Map<String, SubmodelElement> run(MDTInstanceManager manager)
			throws InterruptedException, CancellationException, TaskException {
		try {
			m_source.activate(manager);
			m_target.activate(manager);

			m_target.updateValue(m_source.readValue());
			return Map.of(ARG_TARGET, m_target.read());
		}
		catch ( IOException e ) {
			throw new TaskException("Failed to set value: cause=" + e, e);
		}
	}

	/**
	 * 취소를 지원하지 않는다.
	 *
	 * @return	항상 {@code false}.
	 */
	@Override
	public boolean cancel() {
		return false;
	}
	
	/**
	 * {@link SetTask}를 만드는 builder를 생성한다.
	 *
	 * @return	새 builder.
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * {@link SetTask} builder.
	 */
	public static class Builder {
		private TaskArgument m_source;
		private TaskArgument.ReferenceArgument m_target;
		
		/**
		 * 원본·대상 인자가 지정되지 않은 builder를 만든다.
		 */
		public Builder() {
		}
		
		/**
		 * 설정된 인자로 태스크를 만든다.
		 *
		 * @return	태스크 객체.
		 * @throws IllegalArgumentException	원본 또는 대상 인자가 지정되지 않은 경우.
		 */
		public SetTask build() {
			return new SetTask(this);
		}
		
		/**
		 * 원본 인자를 설정한다.
		 *
		 * @param source	값을 읽을 원본 인자.
		 * @return	이 builder.
		 */
		public Builder source(TaskArgument source) {
			m_source = source;
			return this;
		}
		
		/**
		 * 대상 인자를 설정한다.
		 *
		 * @param target	값을 기록할 대상 참조 인자.
		 * @return	이 builder.
		 */
		public Builder target(TaskArgument.ReferenceArgument target) {
			m_target = target;
			return this;
		}
	}
}