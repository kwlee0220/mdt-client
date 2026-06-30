package mdt.model.sm.ref.timeseries;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.jetbrains.annotations.Nullable;

import utils.Instants;
import utils.Tuple;
import utils.func.Funcs;
import utils.jdbc.JdbcProcessor;
import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.aas.DataType;
import mdt.model.sm.SubmodelUtils;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.PropertyValue;
import mdt.model.timeseries.Metadata;
import mdt.model.timeseries.RecordMetadata.Field;
import mdt.model.timeseries.Records;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
class LinkedSegmentRecordsReader extends SegmentRecordsReader {
	private final DefaultElementReference m_segmentRef;
	private final JdbcProcessor m_jdbc;
	private final String m_baseQuery;
	
	private static record Match(Field field, String dbColName) { };
	
	LinkedSegmentRecordsReader(Metadata tsMetadata, @Nullable TimeSeriesRange range,
								@Nullable List<String> columns,
								DefaultElementReference smeRef) throws IOException {
		super(tsMetadata, range, columns);
		
		m_segmentRef = smeRef;
		
		String endpoint = (String)getSegmentField("Endpoint");
		m_jdbc = JdbcProcessor.builderFromFullJdbcUrl(endpoint).build();
		
		m_baseQuery = (String)getSegmentField("Query");
	}
	
	@Override
	protected ElementCollectionValue readValue() throws IOException {
		// 'm_columns' 정보와 'm_range' 정보를 이용하여 쿼리를 구성한다.
		Tuple<String,List<Field>> queryInfo = buildQuery(m_jdbc);
		String query = queryInfo._1;
		List<Field> fields = queryInfo._2;
		
		int idx = 0;
		LinkedHashMap<String,ElementValue> records = new LinkedHashMap<>();
		try ( ResultSet rs = m_jdbc.executeQuery(query, true) ) {
			while ( rs.next() ) {
				ElementCollectionValue colEmcv = toRecordValue(fields, rs);
				
				String idShort = String.format("rec%02d", idx++);
				records.put(idShort, colEmcv);
			}
			return new ElementCollectionValue(records);
		}
		catch ( SQLException e ) {
			String msg = String.format("Failed to execute query for records: query=%s", query);
			throw new IOException(msg, e);
		}
	}
	
	@Override
	protected SubmodelElementCollection read() throws IOException {
		// 'm_columns' 정보와 'm_range' 정보를 이용하여 쿼리를 구성한다.
		Tuple<String,List<Field>> queryInfo = buildQuery(m_jdbc);
		String query = queryInfo._1;
		List<Field> fields = queryInfo._2;
		
		int idx = 0;
		List<SubmodelElement> records = new ArrayList<>();
		try ( ResultSet rs = m_jdbc.executeQuery(query, true) ) {
			while ( rs.next() ) {
				ElementCollectionValue colEmcv = toRecordValue(fields, rs);
				
				String idShort = String.format("rec%02d", idx++);
				records.add(toRecordSMC(idShort, colEmcv));
			}
			var recordsSmc = SubmodelUtils.newSubmodelElementCollection("Records", records);
			recordsSmc.setSemanticId(Records.SEMANTIC_ID_REFERENCE);
			return recordsSmc;
		}
		catch ( SQLException e ) {
			String msg = String.format("Failed to execute query for records: query=%s", query);
			throw new IOException(msg, e);
		}
	}
	
	private Tuple<String, List<Field>> buildQuery(JdbcProcessor jdbc) throws IOException {
		// 'm_columns'에 명시된 컬럼명과 매칭되는 필드명을 찾아 projection을 구성한다.
		List<Field> fields = m_tsMetadata.getRecord().getFieldAll();
		String colList = "*";
		if ( m_columns != null ) {
			List<Match> proj = getProjection(fields, jdbc, m_baseQuery);
			fields = FStream.from(proj)
							.map(Match::field)
							.toList();
			colList = FStream.from(proj).map(Match::dbColName).join(',');
		}

		// 'm_range' 정보를 WHERE 절로 구성한다.
		// (length 기반 LIMIT은 subquery로 감싼 뒤 적용하므로 아래에서 별도 처리한다.)
		String whereClause = "";
		if ( m_range != null ) {
			if ( m_range instanceof TimeSeriesRange.Count ) {
				// LIMIT/OFFSET은 subquery 래핑 이후에 적용한다.
			}
			else if ( m_range instanceof TimeSeriesRange.Trailing trailing ) {
				// NOW: 현재 시각 기준, LATEST: 가장 마지막 record의 timestamp 값을 기준으로 시작 시각을 계산한다.
				Instant start = (trailing.anchor() == TimeSeriesRange.Anchor.NOW)
								? Instant.now().minus(trailing.duration())
								: getLastTimestamp(jdbc, m_baseQuery).minus(trailing.duration());
				whereClause = String.format(" WHERE timestamp >= '%s'", Instants.toUTCString(start));
			}
			else if ( m_range instanceof TimeSeriesRange.Absolute absolute ) {
				List<String> conds = new ArrayList<>();
				if ( absolute.from() != null ) {
					conds.add(String.format("timestamp >= '%s'", Instants.toUTCString(absolute.from())));
				}
				if ( absolute.to() != null ) {
					conds.add(String.format("timestamp <= '%s'", Instants.toUTCString(absolute.to())));
				}
				whereClause = " WHERE " + String.join(" AND ", conds);
			}
			else {
				throw new IllegalStateException("Invalid range: " + m_range);
			}
		}

		// 추가 가공이 필요없는 경우는 원본 쿼리를 그대로 사용한다.
		if ( m_columns == null && m_range == null ) {
			return Tuple.of(m_baseQuery, fields);
		}

		// 원본 쿼리(baseQuery)는 임의의 SQL일 수 있어 (이미 WHERE/ORDER BY/LIMIT 등이 포함될 수 있음)
		// 절을 직접 이어붙이지 않고, 항상 subquery로 감싼 뒤 projection/WHERE/LIMIT를 적용한다.
		// (WHERE는 모든 원본 컬럼을 가진 subquery에 적용되므로 projection에서 빠진 'timestamp'로도 필터링 가능)
		String query = String.format("SELECT %s FROM (%s) AS subquery%s",
									colList, m_baseQuery, whereClause);
		
		// query 의 결과에서 마지막 record를 기준으로 LIMIT를 적용한다.
		// (length는 "가장 최근 length개 record"를 의미하므로, 전체 개수에서 length를 뺀
		//  위치부터 length개를 가져오도록 LIMIT/OFFSET을 구성한다.)
		if ( m_range instanceof TimeSeriesRange.Count count ) {
			long recCount = (Long)getSegmentField("RecordCount");
			long offset = Math.max(0, recCount - count.length());
			query = String.format("%s LIMIT %d OFFSET %d", query, count.length(), offset);
		}

		return Tuple.of(query, fields);
	}

	/**
	 * 원본 쿼리 결과에서 가장 마지막 record의 timestamp 값을 조회한다.
	 * <p>
	 * {@link TimeSeriesRange#last(java.time.Duration)} 기반 조회에서, 현재 시각이 아니라
	 * 실제로 저장된 마지막 record의 시각을 기준으로 시작 시각을 계산하기 위해 사용한다.
	 */
	private Instant getLastTimestamp(JdbcProcessor jdbc, String baseQuery) throws IOException {
		String query = String.format("SELECT MAX(timestamp) FROM (%s) AS subquery", baseQuery);
		try ( ResultSet rs = jdbc.executeQuery(query, true) ) {
			// timestamp는 record의 첫번째 필드이므로, 해당 필드의 DataType으로 변환한다.
			Field tsField = m_tsMetadata.getRecord().getFieldAll().get(0);

			Object obj = rs.next() ? readColumnFromResultSet(rs, 1, tsField.getType()) : null;
			if ( obj == null ) {
				throw new IOException("Cannot find the last record's timestamp: query=" + baseQuery);
			}
			return (Instant)obj;
		}
		catch ( SQLException e ) {
			String msg = String.format("Failed to query the last record's timestamp: query=%s", query);
			throw new IOException(msg, e);
		}
	}

	/**
	 * 길이 1개짜리 쿼리를 실행하여, DBMS에 저장된 컬럼명과 Record에 소속된 필드명을 매칭시킨다.
	 * <p>
	 * 매칭 방법은 컬럼과 필드의 순서를 사용한다.
	 * (컬럼명과 필드명은 일치하지 않을 수 있기 때문에, 컬럼명과 필드명을 직접 비교하여
	 * 매칭시키는 방법은 사용하지 않는다.)
	 */
	private List<Match> getProjection(List<Field> fields, JdbcProcessor jdbc, String query) throws IOException {
		// 원본 쿼리에 이미 LIMIT 등이 포함될 수 있으므로 subquery로 감싼 뒤 LIMIT 1을 적용한다.
		query = String.format("SELECT * FROM (%s) AS subquery LIMIT 1", query);
		try ( ResultSet rs = jdbc.executeQuery(query, true) ) {
			ResultSetMetaData rsMetadata = rs.getMetaData();
			var matchList = FStream.range(1, rsMetadata.getColumnCount()+1)
									.mapOrThrow(idx -> rsMetadata.getColumnName(idx))
									.zipWith(FStream.from(fields), (dbCol, field) -> new Match(field, dbCol))
									.toList();
			
			// 'm_columns'에 명시된 컬럼명과 매칭되는 필드명을 찾아 projection을 구성한다.
			List<Match> projection = new ArrayList<>();
			for ( String col : m_columns ) {
				var match = Funcs.findFirst(matchList, m -> m.field().getName().equals(col));
				if ( match == null) {
					// 매칭되는 필드명이 없는 경우는 오류로 간주한다.
					List<String> fieldNames = FStream.from(matchList)
													.map(m -> m.field().getName())
													.toList();
					String msg = String.format("Column not found in record metadata: column=%s, metadata=%s",
												col, fieldNames);
					throw new IOException(msg);
				}
				projection.add(match);
			}
			
			return projection;
		}
		catch ( SQLException e ) {
			String msg = String.format("Failed to execute query for column metadata: query=%s", query);
			throw new IOException(msg, e);
		}
	}
	
	private SubmodelElementCollection toRecordSMC(String recIdShort, ElementCollectionValue colEmcv) {
		List<SubmodelElement> elements = KeyValueFStream.from(colEmcv.getFieldMap())
												.map(kv -> ((PropertyValue<?>)kv.value()).toElementBuilder()
																					.idShort(kv.key())
																					.build())
												.cast(SubmodelElement.class)
												.toList();
		var recordSmc = SubmodelUtils.newSubmodelElementCollection(recIdShort, elements);
		recordSmc.setSemanticId(mdt.model.timeseries.Record.SEMANTIC_ID_REFERENCE);
		return recordSmc;
	}
	
	private ElementCollectionValue toRecordValue(List<Field> fields, ResultSet rs) throws IOException {
		try {
			LinkedHashMap<String,ElementValue> cols = new LinkedHashMap<>();
			for ( int i = 0; i < fields.size(); i++ ) {
				Field field = fields.get(i);
				DataType<?> dtype = field.getType();

				Object obj = readColumnFromResultSet(rs, i+1, dtype);
				PropertyValue<?> col = PropertyValue.fromValueObject(obj, dtype.getTypeDefXsd());
				cols.put(field.getName(), col);
			}
			
			return new ElementCollectionValue(cols);
		}
		catch ( SQLException e ) {
			throw new IOException("Failed to read record from ResultSet", e);
		}
	}
	
	private Object getSegmentField(String fieldName) throws IOException {
		return m_segmentRef.child(fieldName).readValue().toValueObject();
	}

	public static Object readColumnFromResultSet(ResultSet rs, int colIdx, DataType<?> dtype)
		throws SQLException {
		Object raw = ( dtype.getTypeDefXsd() == DataTypeDefXsd.DATE_TIME )
					? rs.getObject(colIdx, LocalDateTime.class)
					: rs.getObject(colIdx);
		return dtype.fromJdbcObject(raw);
	}
}
