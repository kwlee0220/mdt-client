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
 * MDT 연산에 사용되는 입/출력 인자의 reference를 정의하는 인터페이스이다.
 * <p>
 * 입/출력 연산 인자의 reference는 다음과 같이 구성된다.
 * <ul>
 *     <li>instanceId: 연산을 포함하는 인스턴스의 ID
 *     <li>submodelIdShort: 연산을 포함하는 서브모델의 ID
 *     <li>kind: 인자의 종류 (입력 또는 출력)
 *     <li>argSpec: 인자의 명세. 인자의 이름 또는 인덱스 번호이거나 '*' (모든 인자)이다.
 * </ul>
 * <p>
 * 인자의 값을 읽거나 쓰기 위해서는 {@link #activate(MDTInstanceManager)}를 호출하여
 * 인자의 reference를 활성화해야 한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ReadLastRecordsReference extends SubmodelBasedElementReference {
	public static final String SERIALIZATION_TYPE = "mdt:ref:timeseries";
	private static final String FIELD_SUBMODEL_REF = "submodelReference";
	private static final String FIELD_LAST = "last";			// 마지막 N개(Count, 숫자) 또는 "<기간>@<anchor>"(Trailing, 문자열, optional)
	private static final String FIELD_TIME_SPAN = "timeSpan";	// 절대 범위
	private static final String FIELD_FROM = "from";			// 절대 범위 시작 시각 (ISO-8601 Instant, optional)
	private static final String FIELD_TO = "to";				// 절대 범위 종료 시각 (ISO-8601 Instant, optional)
	private static final String FIELD_COLUMNS = "columns";		// 읽어올 컬럼명 리스트 (optional, default: all)
	
	@Nullable private final TimeSeriesRange m_range;
	@Nullable private final List<String> m_columns;
	
	private ReadLastRecordsReference(DefaultSubmodelReference submodelRef,
										@Nullable TimeSeriesRange range, @Nullable List<String> columns) {
		super(submodelRef);
		
		m_range = range;

		m_columns = (columns != null) ? Lists.newArrayList(columns) : null;
		if ( m_columns != null ) {
			Funcs.removeFirstIf(m_columns, n -> n.equals("Time"));
			m_columns.add(0, "Time");
		}
	}

	@Override
	public SubmodelElementCollection read() throws IOException {
		return TimeSeriesUtils.readLastRecords(getSubmodelReference(), m_range, m_columns);
	}

	@Override
	public ElementCollectionValue readValue() throws IOException {
		return ElementCollectionValue.from(read());
	}

	@Override
	public void write(SubmodelElement newElm) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesElementReference does not support element update");
	}

	@Override
	public void updateValue(ElementValue smev) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesElementReference does not support value update");
	}

	@Override
	public void updateValue(String valueJsonString) throws IOException {
		throw new UnsupportedOperationException("TimeSeriesElementReference does not support value update");
	}

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
		if ( obj == null || !(obj instanceof ReadLastRecordsReference) ) {
			return false;
		}

		ReadLastRecordsReference other = (ReadLastRecordsReference) obj;
		return Objects.equals(getSubmodelReference(), other.getSubmodelReference());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSubmodelReference());
	}
	
	/**
	 * {@link ReadLastRecordsReference}를 생성하기 위한 {@link Builder}를 반환한다.
	 *
	 * @return 새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * {@link ReadLastRecordsReference}를 단계적으로 구성하는 빌더이다.
	 */
	public static class Builder {
		private DefaultSubmodelReference m_submodelRef;
		private TimeSeriesRange m_range;
		private List<String> m_columns;

		/**
		 * 지금까지 설정된 값으로 {@link ReadLastRecordsReference}를 생성한다.
		 *
		 * @return 생성된 {@link ReadLastRecordsReference} 객체.
		 */
		public ReadLastRecordsReference build() {
			return new ReadLastRecordsReference(m_submodelRef, m_range, m_columns);
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
	 * Json 객체로부터 {@link ReadLastRecordsReference}를 복원한다.
	 * <p>
	 * Jackson 기반의 {@link ElementReferences.Deserializer}가 {@link ElementReference} 객체를
	 * 역직렬화하는 과정에서 호출된다.
	 *
	 * @param jnode	{@code submodelReference}, {@code kind}, {@code argumentSpec} 필드를 담은 Json 노드.
	 * @return 복원된 {@link ReadLastRecordsReference} 객체.
	 * @throws IOException	Json 해석 과정에서 예외가 발생한 경우.
	 */
	public static ReadLastRecordsReference deserializeFields(JsonNode jnode) throws IOException {
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
		}

		JsonNode colsNode = JacksonUtils.getFieldOrNull(jnode, FIELD_COLUMNS);
		List<String> columns = (colsNode != null) ? FStream.from(colsNode.elements())
															.map(JsonNode::asText)
															.toList() : null;
		
		return new ReadLastRecordsReference(smRef, range, columns);
	}
	
	@Override
	protected String buildIdShortPath() {
		return "Segments.FullRange";
	}
}
