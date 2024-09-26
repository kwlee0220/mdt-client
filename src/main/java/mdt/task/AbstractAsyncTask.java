package mdt.task;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;

import mdt.model.instance.MDTInstanceManager;
import utils.LoggerSettable;
import utils.Throwables;
import utils.async.Guard;
import utils.async.StartableExecution;
import utils.async.op.AsyncExecutions;
import utils.async.op.TimedAsyncExecution;
import utils.func.FOption;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class AbstractAsyncTask<T> implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(AbstractAsyncTask.class);

	private Logger logger;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private StartableExecution<T> m_exec;
	@GuardedBy("m_guard") private Instant m_started = null;
	
	public abstract StartableExecution<T> buildExecution(MDTInstanceManager manager,
															Map<String,Port> inputPorts,
															Map<String,Port> outputPorts,
															Duration timeout) throws Exception;
	public abstract void updateOutputs(T outputs, Map<String,Port> outputPorts);
	
	protected AbstractAsyncTask() {
		setLogger(s_logger);
	}

	@Override
	public void run(MDTInstanceManager manager, Map<String,Port> inputPorts,
					Map<String,Port> outputPorts, Duration timeout)
		throws TimeoutException, InterruptedException, CancellationException, ExecutionException {
		try {
			m_guard.runAnSignalAllOrThrow(() -> {
				m_exec = buildExecution(manager, inputPorts, outputPorts, timeout);
				if ( timeout != null ) {
					Duration extendedTimeout = Duration.ofMillis(timeout.toMillis() + 1000);
					m_exec = AsyncExecutions.timed(m_exec, extendedTimeout);
				}
				
				// Output port들 중에서의 'MDTTask.ELAPSED_TIME_PORT_NAME' 이름을 갖는
				// Port가 존재하는 경우, 이 위치에 본 task의 수행시간을 기록한다.
				Tasks.findElapsedTimePort(outputPorts.values())
						.forEach(p -> {
							m_exec.whenStarted(() -> m_started = Instant.now());
							m_exec.whenCompleted(result -> {
								Preconditions.checkState(m_started != null, "Started time has not been set.");
								
								Duration elapsed = Duration.between(m_started, Instant.now());
								if ( getLogger().isDebugEnabled() ) {
									getLogger().debug("save the elapsed task-execution time: {}", elapsed);
								}
								p.setJsonNode(new TextNode(elapsed.toString()));
							});
						});
				m_exec.start();
			});
			
			T outputs = m_exec.waitForFinished().get();
			if ( getLogger().isInfoEnabled() ) {
				getLogger().info("Asynchronous Task completes: outputs={}", outputs);
			}
			if ( m_exec instanceof TimedAsyncExecution && ((TimedAsyncExecution<?>)m_exec).isTimedout() ) {
				String msg = String.format("timeout=%s", timeout);
				throw new TimeoutException(msg);
			}
			updateOutputs(outputs, outputPorts);
		}
		catch ( Exception e ) {
			Throwables.throwIfInstanceOf(e, TimeoutException.class);
			Throwables.throwIfInstanceOf(e, InterruptedException.class);
			Throwables.throwIfInstanceOf(e, CancellationException.class);
			Throwables.throwIfInstanceOf(e, ExecutionException.class);
			throw new ExecutionException(e);
		}
	}

	@Override
	public boolean cancel() {
		try {
			return m_guard.awaitUntilAndGet(() -> m_exec != null, () -> m_exec.cancel(true));
		}
		catch ( InterruptedException e ) {
			return false;
		}
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
