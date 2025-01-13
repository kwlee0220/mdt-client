package mdt.task.builtin;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.LoggerSettable;
import utils.func.FOption;

import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.SubmodelElementValue;
import mdt.task.MDTTask;
import mdt.task.TaskException;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class CopyTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(CopyTask.class);
	
	private Logger m_logger;
	
	protected CopyTask() {
		setLogger(m_logger);
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public Logger getLogger() {
		return FOption.getOrElse(m_logger, s_logger);
	}

	@Override
	public void setLogger(Logger logger) {
		m_logger = logger;
	}
	
	public static class CopyPropertyTask extends CopyTask {
		private static final Logger s_logger = LoggerFactory.getLogger(CopyPropertyTask.class);
		
		private final ElementReference m_from;
		private final ElementReference m_to;
		
		public CopyPropertyTask(ElementReference from, ElementReference to) {
			m_from = from;
			m_to = to;
			
			setLogger(s_logger);
		}

		@Override
		public void run(MDTInstanceManager manager) throws TimeoutException, InterruptedException,
																CancellationException, TaskException {
			try {
				m_to.update(m_from.readValue());
			}
			catch ( IOException e ) {
				String msg = String.format("Failed to copy data: from=%s, to=%s", m_from, m_to);
				throw new TaskException(msg, e);
			}
		}
	}

	public static class CopyFileTask extends CopyTask {
		private static final Logger s_logger = LoggerFactory.getLogger(CopyFileTask.class);
		
		private final MDTElementReference m_from;
		private final MDTElementReference m_to;
		
		public CopyFileTask(ElementReference from, ElementReference to) {
			Preconditions.checkArgument(from instanceof MDTElementReference,
										"Not MDTSubmodelElementReference, but {}", from.getClass());
			Preconditions.checkArgument(to instanceof MDTElementReference,
										"Not MDTSubmodelElementReference, but {}", to.getClass());
			
			m_from = (MDTElementReference)from;
			m_to = (MDTElementReference)to;
			
			setLogger(s_logger);
		}

		@Override
		public void run(MDTInstanceManager manager) throws TimeoutException, InterruptedException,
																CancellationException, TaskException {
			try {
				if ( m_from.getInstanceId().equals(m_to.getInstanceId()) ) {
					SubmodelElementValue fileValue = m_from.readValue();
					m_to.update(fileValue);
				}
				else {
					throw new UnsupportedOperationException();
//					MDTFile file = fromSvc.getFileByPath(m_from.getElementIdShortPath());
//					toSvc.putFileByPath(m_to.getElementIdShortPath(), file);
				}
			}
			catch ( Exception e ) {
				String msg = String.format("Failed to copy data: from=%s, to=%s", m_from, m_to);
				throw new TaskException(msg, e);
			}
		}
	}
}
