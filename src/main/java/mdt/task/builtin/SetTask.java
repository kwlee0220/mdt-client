package mdt.task.builtin;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import utils.LoggerSettable;
import utils.func.FOption;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.service.SubmodelService;
import mdt.model.sm.DefaultMDTFile;
import mdt.model.sm.MDTSubmodelElementReference;
import mdt.model.sm.SubmodelElementReference;
import mdt.task.MDTTask;
import mdt.task.TaskException;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public abstract class SetTask implements MDTTask, LoggerSettable {
	private static final Logger s_logger = LoggerFactory.getLogger(SetTask.class);
	
	private Logger m_logger;
	
	protected SetTask() {
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
	
	public static class SetDefaultTask extends SetTask {
		private static final Logger s_logger = LoggerFactory.getLogger(SetDefaultTask.class);
		
		private final SubmodelElementReference m_target;
		private final String m_valueJson;
		
		public SetDefaultTask(SubmodelElementReference target, String valueJson) {
			m_target = target;
			m_valueJson = valueJson;
			
			setLogger(s_logger);
		}
		
		@Override
		public void run(MDTInstanceManager manager) throws TimeoutException, InterruptedException,
																CancellationException, TaskException {
			// m_valueJson 값이 SubmodelElement 전체인 경우도 처리하기 위해
			// 일단 SubmodelElement 형태로 파싱해보고 성공하면 갱신하고
			// 그렇지 않다만 value 값에 대한 json 문자열로 간주하여 처리한다.
			try {
				SubmodelElement newSme = MDTModelSerDe.readValue(m_valueJson, SubmodelElement.class);
				m_target.update(newSme);
			}
			catch ( IOException e ) {
				try {
					m_target.updateWithValueJsonString(m_valueJson);
				}
				catch ( IOException e1 ) {
					throw new TaskException("Invalid value to set: value=" + m_valueJson, e1);
				}
			}
		}
	}
	
	public static class SetFileTask extends SetTask {
		private static final Logger s_logger = LoggerFactory.getLogger(SetFileTask.class);
		
		private final MDTSubmodelElementReference m_target;
		private final java.io.File m_file;
		@Nullable private final String m_path;
		
		public SetFileTask(SubmodelElementReference target, java.io.File file, String path) {
			Preconditions.checkArgument(target instanceof MDTSubmodelElementReference,
										"Not MDTSubmodelElementReference, but {}", target.getClass());
			m_target = (MDTSubmodelElementReference)target;
			m_file = file;
			m_path = path;
			
			setLogger(s_logger);
		}

		@Override
		public void run(MDTInstanceManager manager) throws TimeoutException, InterruptedException,
																CancellationException, TaskException {
			SubmodelService svc = m_target.getSubmodelService();
			
			try {
				DefaultMDTFile mdtFile = (m_path != null)
										? DefaultMDTFile.from(m_file, m_path)
										: DefaultMDTFile.from(m_file);
				svc.putFileByPath(m_target.getElementIdShortPath(), mdtFile);
			}
			catch ( IOException e ) {
				throw new TaskException("Failed to read file", e);
			}
			
		}
	}
}
