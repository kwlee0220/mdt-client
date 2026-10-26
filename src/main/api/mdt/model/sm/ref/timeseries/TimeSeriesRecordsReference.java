package mdt.model.sm.ref.timeseries;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import utils.UnitUtils;
import utils.func.Funcs;
import utils.json.JacksonUtils;
import utils.stream.FStream;

import mdt.model.MDTModelSerDe;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.DefaultSubmodelReference;
import mdt.model.sm.ref.ElementReference;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.SubmodelBasedElementReference;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.timeseries.TimeSeriesUtils;


/**
 * 시계열(TimeSeries) Submodel의 레코드를 조회하는 {@link ElementReference} 구현체이다.
 * <p>
 * 대상 시계열 Submodel에 대한 참조와 선택적 조회 범위({@link TimeSeriesRange}),
 * 선택적 조회 컬럼 목록으로 구성되며, 읽기 시 대상 Submodel의 {@code ReadRecordsByRange}
 * 연산을 호출하여 레코드를 얻는다. 레코드 갱신({@code write}/{@code updateValue})은
 * 지원하지 않는다.
 * <p>
 * Json 직렬화 시 {@link #SERIALIZATION_TYPE}({@code "mdt:ref:timeseries"}) 타입으로 식별되며,
 * 레코드를 읽기 위해서는 {@link #activate(MDTInstanceManager)}를 호출하여 참조를 활성화해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TimeSeriesRecordsReference extends SubmodelBasedElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:timeseries";
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_LAST = "last";			// 마지막 N개(Count, 숫자) 또는 "<기간>@<anchor>"(Trailing, 문자열, optional)
	private static final String FIELD_TIME_SPAN = "timeSpan";	// 절대 범위
	private static final String FIELD_FROM = "from";			// 절대 범위 시작 시각 (ISO-8601 Instant, optional)
	private static final String FIELD_TO = "to";				// 절대 범위 종료 시각 (ISO-8601 Instant, optional)
	private static final String FIELD_SINCE = "since";			// 시작 시각 기준 개수 범위(Since)
	private static final String FIELD_LENGTH = "length";		// Since 범위에서 읽어올 레코드 개수
	private static final String FIELD_COLUMNS = "columns";		// 읽어올 컬럼명 리스트 (optional, default: all)
	
	@Nullable private final TimeSeriesRange m_range;
	@Nullable private final List<String> m_columns;
	
	private TimeSeriesRecordsReference(DefaultSubmodelReference submodelRef,
										@Nullable TimeSeriesRange range, @Nullable List<String> columns) {
		super(submodelRef);
		
		m_range = range;

		m_columns = (columns != null) ? Lists.newArrayList(columns) : null;
		if ( m_columns != null ) {
			Funcs.removeFirstIf(m_columns, n -> n.equals("Time"));
			m_columns.add(0, "Time");
		}
	}

	/**
	 * 지정된 범위/컬럼의 시계열 레코드들을 읽어 반환한다.
	 * <p>
	 * 대상 시계열 Submodel의 {@code ReadRecordsByRange} 연산을 호출하여 결과를 얻는다.
	 *
	 * @return 레코드들을 담은 {@link SubmodelElementCollection}.
	 * @throws IOException 읽기 과정에서 예외가 발생한 경우.
	 */
	@Override
	public SubmodelElementCollection read() throws IOException {
		return TimeSeriesUtils.readRecords(getSubmodelReference(), m_range, m_columns);
	}

	/**
	 * 지정된 범위/컬럼의 시계열 레코드들의 값을 읽어 반환한다.
	 *
	 * @return 레코드들의 값을 담은 {@link ElementCollectionValue}.
	 * @throws IOException 읽기 과정에서 예외가 발생한 경우.
	 */
	@Override
	public ElementCollectionValue readValue() throws IOException {
		return ElementCollectionValue.from(read());
	}

	/**
	 * 시계열 레코드 참조는 갱신을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void write(SubmodelElement newElm) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesRecordsReference does not support element update");
	}

	/**
	 * 시계열 레코드 참조는 갱신을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void updateValue(ElementValue smev) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesRecordsReference does not support value update");
	}

	/**
	 * 시계열 레코드 참조는 갱신을 지원하지 않는다.
	 *
	 * @throws UnsupportedOperationException 항상 발생한다.
	 */
	@Override
	public void updateValue(String valueJsonString) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesRecordsReference does not support value update");
	}

	/**
	 * 이 참조의 MdtExpr 표현식({@code timeseries:<submodelSpec>[#<range>][|<cols>]})을 반환한다.
	 * <p>
	 * 내부용 선두 {@code "Time"} 컬럼은 직렬화에서 제외된다.
	 *
	 * @return 표현식 문자열.
	 */
	@Override
	public String toStringExpr() {
		// MdtExpr 문법(timeseriesSpec)에 맞춰 직렬화한다:
		//   timeseries:<submodelSpec>:<tsSpec>[#<range>][|<col,...>]
		StringBuilder sb = new StringBuilder("timeseries:");
		sb.append(getSubmodelReference().toStringExpr());

		if ( m_range != null ) {
			sb.append('#').append(m_range.toString());
		}
		if ( m_columns != null ) {
			String columnsExpr = FStream.from(m_columns)
										.drop(1)	// Time 컬럼은 제외
										.join(',');
			if ( !columnsExpr.isEmpty() ) {
				sb.append('|').append(columnsExpr);
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		String actStr = ( isActivated() ) ? "activated" : "deactivated";
		return String.format("%s (%s)", toStringExpr(), actStr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || obj.getClass() != getClass() ) {
			return false;
		}

		TimeSeriesRecordsReference other = (TimeSeriesRecordsReference) obj;
		return Objects.equals(getSubmodelReference(), other.getSubmodelReference());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSubmodelReference());
	}
	
	/**
	 * {@link TimeSeriesRecordsReference}를 생성하기 위한 {@link Builder}를 반환한다.
	 *
	 * @return 새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * {@link TimeSeriesRecordsReference}를 단계적으로 구성하는 빌더이다.
	 */
	public static class Builder {
		private DefaultSubmodelReference m_submodelRef;
		private TimeSeriesRange m_range;
		private List<String> m_columns;

		/**
		 * 지금까지 설정된 값으로 {@link TimeSeriesRecordsReference}를 생성한다.
		 *
		 * @return 생성된 {@link TimeSeriesRecordsReference} 객체.
		 */
		public TimeSeriesRecordsReference build() {
			return new TimeSeriesRecordsReference(m_submodelRef, m_range, m_columns);
		}

		/**
		 * 인자가 속한 Submodel 참조를 설정한다.
		 *
		 * @param smRef	대상 Submodel에 대한 참조.
		 * @return 이 빌더 객체.
		 */
		public Builder submodelReference(DefaultSubmodelReference smRef) {
			m_submodelRef = smRef;
			return this;
		}
		
		/**
		 * 읽어올 컬럼명 목록을 설정한다.
		 * <p>
		 * 설정하지 않으면 전체 컬럼을 읽는다.
		 *
		 * @param columns	읽어올 컬럼명 목록.
		 * @return 이 빌더 객체.
		 */
		public Builder columns(List<String> columns) {
			m_columns = columns;
			return this;
		}

		/**
		 * 최근 {@code length}개 레코드만 읽도록 범위를 설정한다.
		 *
		 * @param length	읽어올 최근 레코드 개수.
		 * @return 이 빌더 객체.
		 */
		public Builder last(int length) {
			m_range = TimeSeriesRange.length(length);
			return this;
		}
		
		/**
		 * 마지막 {@code duration} 기간의 레코드만 읽도록 범위를 설정한다.
		 *
		 * @param last	읽어올 마지막 기간.
		 * @return 이 빌더 객체.
		 */
		public Builder last(Duration last, TimeSeriesRange.Anchor anchor) {
			m_range = TimeSeriesRange.last(last, anchor);
			return this;
		}
		
		/**
		 * 절대 범위 {@code from}~{@code to}에 해당하는 레코드만 읽도록 범위를 설정한다.
		 *
		 * @param from	읽어올 범위의 시작 시각(포함).
		 * @param to	읽어올 범위의 종료 시각(포함).
		 * @return 이 빌더 객체.
		 */
		public Builder between(Instant from, Instant to) {
			m_range = TimeSeriesRange.between(from, to);
			return this;
		}

		/**
		 * {@code from} 시각부터 시간 오름차순으로 {@code length}개 레코드만 읽도록 범위를 설정한다.
		 *
		 * @param from		읽어올 범위의 시작 시각(포함).
		 * @param length	읽어올 레코드 개수.
		 * @return 이 빌더 객체.
		 */
		public Builder since(Instant from, int length) {
			m_range = TimeSeriesRange.since(from, length);
			return this;
		}
		
		/**
		 * 레코드 조회 범위를 설정한다.
		 * <p>
		 * 설정하지 않으면 전체 레코드를 읽는다.
		 *
		 * @param range	조회 범위.
		 * @return 이 빌더 객체.
		 */
		public Builder range(TimeSeriesRange range) {
			m_range = range;
			return this;
		}
	}

	@Override
	public String getSerializationType() {
		return SERIALIZATION_TYPE;
	}
	
	/**
	 * 이 참조의 필드들을 주어진 {@link JsonGenerator}로 직렬화한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Serializer}가 {@link ElementReference} 객체를
	 * 직렬화하는 과정에서 호출된다.
	 *
	 * @param gen	직렬화에 사용할 {@link JsonGenerator}.
	 * @throws	IOException	Json 직렬화 과정에서 예외가 발생한 경우.
	 */
	@Override
	public void serializeFields(JsonGenerator gen) throws IOException {
		gen.writeObjectField(FIELD_SUBMODEL_REF, getSubmodelReference());
		if ( m_range != null ) {
			if ( m_range instanceof TimeSeriesRange.Count count ) {
				gen.writeNumberField(FIELD_LAST, count.length());
			}
			else if ( m_range instanceof TimeSeriesRange.Trailing trailing ) {
				// 'last' 필드 하나에 기간과 anchor를 함께 인코딩한다: "<기간>@<anchor>" (예: PT10S@latest, PT10S@now)
				String anchorName = trailing.anchor().name().toLowerCase();
				gen.writeStringField(FIELD_LAST, trailing.duration().toString() + "@" + anchorName);
			}
			else if ( m_range instanceof TimeSeriesRange.Absolute absolute ) {
				gen.writeObjectFieldStart(FIELD_TIME_SPAN);
				if ( absolute.from() != null ) {
					gen.writeStringField(FIELD_FROM, absolute.from().toString());
				}
				if ( absolute.to() != null ) {
					gen.writeStringField(FIELD_TO, absolute.to().toString());
				}
				gen.writeEndObject();
			}
			else if ( m_range instanceof TimeSeriesRange.Since since ) {
				gen.writeObjectFieldStart(FIELD_SINCE);
				gen.writeStringField(FIELD_FROM, since.from().toString());
				gen.writeNumberField(FIELD_LENGTH, since.length());
				gen.writeEndObject();
			}
			else {
				throw new IllegalStateException("Invalid range: " + m_range);
			}
		}
		if ( m_columns != null ) {
			// 선두의 내부용 'Time' 컬럼(index 0)은 제외하고 직렬화한다(toStringExpr와 동일).
			// 역직렬화 시 생성자가 'Time'을 다시 맨 앞에 추가하므로 round-trip이 보존된다.
			gen.writeArrayFieldStart(FIELD_COLUMNS);
			for ( int i = 1; i < m_columns.size(); ++i ) {
				gen.writeString(m_columns.get(i));
			}
			gen.writeEndArray();
		}
	}

	/**
	 * Json 객체로부터 {@link TimeSeriesRecordsReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code submodelReference} 필드와 선택적 {@code last}/{@code timeSpan}/{@code columns}
	 * 				필드를 담은 Json 노드.
	 * @return 복원된 {@link TimeSeriesRecordsReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static TimeSeriesRecordsReference deserializeFields(JsonNode jnode) throws IOException {
		JsonNode smRefNode = checkJsonField(jnode, FIELD_SUBMODEL_REF);
		DefaultSubmodelReference smRef = MDTModelSerDe.readValue(smRefNode, DefaultSubmodelReference.class);
		
		TimeSeriesRange range = null;
		JsonNode lastNode = JacksonUtils.getFieldOrNull(jnode, FIELD_LAST);
		if ( lastNode != null && !lastNode.isNull() ) {
			if ( lastNode.isInt() ) {
				range = TimeSeriesRange.length(lastNode.asInt());
			}
			else if ( lastNode.isTextual() ) {
				// "<기간>@<anchor>" 형식. anchor가 생략되면 LATEST로 간주한다.
				String text = lastNode.asText();
				TimeSeriesRange.Anchor anchor = TimeSeriesRange.Anchor.LATEST;
				int atIdx = text.lastIndexOf('@');
				if ( atIdx >= 0 ) {
					String anchorStr = text.substring(atIdx + 1);
					anchor = switch ( anchorStr.toLowerCase() ) {
						case "latest" -> TimeSeriesRange.Anchor.LATEST;
						case "now" -> TimeSeriesRange.Anchor.NOW;
						default -> throw new IOException("Invalid anchor in last field: " + text);
					};
					text = text.substring(0, atIdx);
				}
				Duration dur = UnitUtils.parseDuration(text);
				range = TimeSeriesRange.last(dur, anchor);
			}
			else {
				throw new IOException("Invalid last field: " + lastNode);
			}
		}
		else {
			JsonNode timeSpanNode = JacksonUtils.getFieldOrNull(jnode, FIELD_TIME_SPAN);
			JsonNode sinceNode = JacksonUtils.getFieldOrNull(jnode, FIELD_SINCE);
			if ( timeSpanNode != null && !timeSpanNode.isNull() ) {
				String fromStr = JacksonUtils.getStringFieldOrNull(timeSpanNode, FIELD_FROM);
				String toStr = JacksonUtils.getStringFieldOrNull(timeSpanNode, FIELD_TO);
				if ( fromStr != null || toStr != null ) {
					Instant from = (fromStr != null) ? Instant.parse(fromStr) : null;
					Instant to = (toStr != null) ? Instant.parse(toStr) : null;
					range = TimeSeriesRange.between(from, to);
				}
				else {
					throw new IOException("Invalid timeSpan field: " + timeSpanNode);
				}
			}
			else if ( sinceNode != null && !sinceNode.isNull() ) {
				String fromStr = JacksonUtils.getStringFieldOrNull(sinceNode, FIELD_FROM);
				JsonNode lengthNode = JacksonUtils.getFieldOrNull(sinceNode, FIELD_LENGTH);
				if ( fromStr == null || lengthNode == null || !lengthNode.isInt() ) {
					throw new IOException("Invalid since field: " + sinceNode);
				}
				range = TimeSeriesRange.since(Instant.parse(fromStr), lengthNode.asInt());
			}
		}

		JsonNode colsNode = JacksonUtils.getFieldOrNull(jnode, FIELD_COLUMNS);
		List<String> columns = (colsNode != null) ? FStream.from(colsNode.elements())
															.map(JsonNode::asText)
															.toList() : null;
		
		return new TimeSeriesRecordsReference(smRef, range, columns);
	}
	
	/**
	 * 시계열 레코드가 위치한 고정 경로({@code "Segments.FullRange"})를 반환한다.
	 *
	 * @return 시계열 레코드의 idShort path.
	 */
	@Override
	protected String buildIdShortPath() {
		return "Segments.FullRange";
	}
}
