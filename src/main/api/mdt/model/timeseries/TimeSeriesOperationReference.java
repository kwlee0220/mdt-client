package mdt.model.timeseries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import utils.Throwables;

import mdt.aas.DataTypes;
import mdt.client.operation.AASOperationClient;
import mdt.model.SubmodelService;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.FileValue;

/**
 * 시계열 조회 연산을 주어진 시간 구간으로 호출한 결과를 가리키는 {@link MDTElementReference}.
 * <p>
 * {@link #read()}는 대상 연산({@code opRef})을 {@code Timespan} 입력 인자에 생성 시 지정한
 * 시간 구간({@link Range}, {@code xs:dateTime})을 설정하여 동기적으로 호출하고, 첫번째 출력 인자의
 * 값을 반환한다. 인스턴스 정보 및 idShortPath 관련 메소드는 대상 연산 참조에 위임된다.
 * <p>
 * 읽기 전용 참조로서 {@link #getPrototype()}, 첨부파일 읽기, 쓰기/갱신 계열 메소드는 모두
 * {@link UnsupportedOperationException}을 발생시킨다. 직렬화 형식도 정의되어 있지 않아
 * {@link #toStringExpr()}, {@link #getSerializationType()}, {@link #serializeFields(JsonGenerator)},
 * {@link #toJsonString()}, {@link #toJsonNode()}도 {@link UnsupportedOperationException}을 발생시킨다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TimeSeriesOperationReference implements MDTElementReference {
	private final MDTElementReference m_opRef;
	private final Range m_timeSpan;
	
	/**
	 * 시계열 연산 참조 객체를 생성한다.
	 *
	 * @param opRef	호출할 시계열 조회 연산에 대한 참조.
	 * @param start	조회 구간의 시작 시각.
	 * @param end	조회 구간의 종료 시각.
	 */
	public TimeSeriesOperationReference(MDTElementReference opRef, Instant start, Instant end) {
		m_opRef = opRef;
		
		m_timeSpan = new DefaultRange.Builder()
									.valueType(DataTypes.DATE_TIME.getTypeDefXsd())
									.min(start.toString())
									.max(end.toString())
									.build();
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

	/**
	 * 대상 연산을 시간 구간을 입력으로 호출하고, 그 첫번째 출력 인자 값을 반환한다.
	 * <p>
	 * 호출은 연산이 종료될 때까지 블록된다 (상태 확인 주기 1초).
	 *
	 * @return	연산의 첫번째 출력 인자 값.
	 * @throws IOException	연산이 취소되거나, 대기 중 쓰레드가 중단되거나, 연산 수행이 실패한 경우.
	 */
	@Override
	public SubmodelElement read() throws IOException {
		AASOperationClient opClient = new AASOperationClient(m_opRef, Duration.ofSeconds(1));
		opClient.setInputVariable("Timespan", m_timeSpan);
		try {
			OperationResult result = opClient.run();
			return result.getOutputArguments().get(0).getValue();
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
	public void updateFromValueJson(JsonNode valueNode) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateFromValueJson(String valueJsonString) throws IOException {
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

	// 이 참조는 직렬화 형식(serialization type, 문자열 표현식)이 정의되어 있지 않다.
	@Override
	public String toStringExpr() {
		throw new UnsupportedOperationException("TimeSeriesOperationReference has no string expression");
	}

	@Override
	public String getSerializationType() {
		throw new UnsupportedOperationException("TimeSeriesOperationReference is not serializable");
	}

	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesOperationReference is not serializable");
	}

	@Override
	public String toJsonString() throws IOException {
		throw new UnsupportedOperationException("TimeSeriesOperationReference is not serializable");
	}

	@Override
	public JsonNode toJsonNode() throws IOException {
		throw new UnsupportedOperationException("TimeSeriesOperationReference is not serializable");
	}

	@Override
	public String toString() {
		return String.format("TimeSeriesOperationReference[op=%s, timespan=%s..%s]",
								m_opRef, m_timeSpan.getMin(), m_timeSpan.getMax());
	}

}
