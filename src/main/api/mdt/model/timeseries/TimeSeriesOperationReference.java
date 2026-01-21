package mdt.model.timeseries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Throwables;

import mdt.aas.DataTypes;
import mdt.aas.DataTypes.DateTimeType;
import mdt.client.operation.AASOperationClient;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;
import mdt.model.sm.value.RangeValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TimeSeriesOperationReference implements MDTElementReference {
	private final MDTElementReference m_opRef;
	private final RangeValue<DateTimeType> m_timeSpan;
	
	public TimeSeriesOperationReference(MDTElementReference opRef, Instant start, Instant end) {
		m_opRef = opRef;
		
		m_timeSpan = new RangeValue(DataTypes.DATE_TIME, start, end);
	}

	@Override
	public boolean isActivated() {
		return m_opRef.isActivated();
	}

	@Override
	public void activate(MDTInstanceManager manager) {
		m_opRef.activate(manager);
	}

	@Override
	public String getInstanceId() {
		return m_opRef.getInstanceId();
	}

	@Override
	public MDTInstance getInstance() throws IllegalStateException {
		return m_opRef.getInstance();
	}

	@Override
	public SubmodelService getSubmodelService() {
		return m_opRef.getSubmodelService();
	}

	@Override
	public String getIdShortPathString() {
		return m_opRef.getIdShortPathString();
	}

	@Override
	public SubmodelElement getPrototype() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SubmodelElement read() throws IOException {
		AASOperationClient opClient = new AASOperationClient(m_opRef, Duration.ofSeconds(1));
		opClient.setInputVariableValue("Timespan", m_timeSpan);
		try {
			OperationResult result = opClient.run();
			return result.getOutputArguments().getFirst().getValue();
		}
		catch ( CancellationException e ) {
			throw new IOException("operation is cancelled: " + m_opRef.getIdShortPathString(), e);
		}
		catch ( InterruptedException e ) {
			throw new IOException("operation is interrupted: " + m_opRef.getIdShortPathString(), e);
		}
		catch ( ExecutionException e ) {
			Throwable cause = Throwables.unwrapThrowable(e);
            throw new IOException("operation execution failed: " + m_opRef.getIdShortPathString(), cause);
		}
	}

	@Override
	public ElementValue readValue() throws IOException {
		return ElementValues.getValue(read());
	}

	@Override
	public void readAttachment(OutputStream out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(SubmodelElement newElm) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(SubmodelElement sme) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateAttachment(FileValue file, InputStream content) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttachment() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toStringExpr() {
		return null;
	}

	@Override
	public String getSerializationType() {
		return null;
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
	}

	@Override
	public String toJsonString() throws IOException {
		return null;
	}

	@Override
	public JsonNode toJsonNode() throws IOException {
		return null;
	}

}
