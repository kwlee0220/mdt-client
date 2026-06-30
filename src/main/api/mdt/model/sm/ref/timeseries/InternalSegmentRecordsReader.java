package mdt.model.sm.ref.timeseries;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Iterables;

import utils.stream.FStream;
import utils.stream.KeyValueFStream;

import mdt.aas.DataTypes;
import mdt.model.sm.ref.DefaultElementReference;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.timeseries.DefaultRecord;
import mdt.model.timeseries.Metadata;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class InternalSegmentRecordsReader extends SegmentRecordsReader {
	private final DefaultElementReference m_recordsRef;
	
	public InternalSegmentRecordsReader(Metadata tsMetadata, @Nullable TimeSeriesRange range,
										@Nullable List<String> columns,
										DefaultElementReference segmentRef) {
		super(tsMetadata, range, columns);
		
		m_recordsRef = segmentRef.child("Records");
	}
	
	@Override
	protected SubmodelElementCollection read() throws IOException {
		SubmodelElementCollection records = m_recordsRef.readCollection();
		
		if ( m_range != null ) {
			records = readByRange(records, m_range);
		}
		if ( m_columns != null ) {
			records = project(records);
		}
		return records;
	}
	
	@Override
	protected ElementCollectionValue readValue() throws IOException {
		ElementValue smev = m_recordsRef.readValue();
		if ( smev instanceof ElementCollectionValue records ) {
			if ( m_range != null ) {
				records = readByRange(records, m_range);
			}
			if ( m_columns != null ) {
				records = project(records);
			}
			
			return records;
		}

		String msg = String.format("'Records' value is not an ElementCollectionValue: %s",
									smev.getClass().getName());
		throw new IOException(msg);
	}

	private SubmodelElementCollection project(SubmodelElementCollection records) throws IOException {
		Map<String,Integer> fieldMap = FStream.from(m_tsMetadata.getRecord().getFieldAll())
											.zipWithIndex()
											.toKeyValueStream(idxed -> idxed.value().getName(), idxed -> idxed.index())
											.toMap(new LinkedHashMap<>());
		List<Integer> colIdxList = FStream.from(m_columns)
											.lookup(fieldMap, true)
											.mapOrThrow(kv -> {
												if ( kv.value() == null ) {
													String msg = String.format("Not found column=%s in record metadata", kv.key());
													throw new IOException(msg);
												}
												return kv.value();
											})
											.toList();
		for ( SubmodelElement recSme: records.getValue() ) {
			if ( recSme instanceof SubmodelElementCollection recSmc ) {
				List<SubmodelElement> colList = recSmc.getValue();
				List<SubmodelElement> projectedColList = new ArrayList<>(colIdxList.size());
				for ( int colIdx: colIdxList ) {
					projectedColList.add(colList.get(colIdx));
				}
				colList.clear();
				colList.addAll(projectedColList);
			}
			else {
				String msg = String.format("Record is not a SubmodelElementCollection: %s",
											recSme.getClass().getName());
				throw new IOException(msg);
			}
		}
		
		return records;
	}
	private ElementCollectionValue project(ElementCollectionValue records) throws IOException {
		LinkedHashMap<String, ElementValue> members = new LinkedHashMap<>();
		for ( var ent : records.getFieldMap().entrySet() ) {
			if ( ent.getValue() instanceof ElementCollectionValue rec ) {
				LinkedHashMap<String, ElementValue> fields = new LinkedHashMap<>();
				for ( String c: m_columns ) {
					ElementValue col = rec.getField(c);
					if ( col == null ) {
						String msg = String.format("Not found column=%s in record=%s", c, rec);
						throw new IOException(msg);
					}
					fields.put(c, col);
				}
				members.put(ent.getKey(), new ElementCollectionValue(fields));
			}
			else {
				String msg = String.format("Record value is not an ElementCollectionValue: %s",
											ent.getValue().getClass().getName());
				throw new IOException(msg);
			}
		}
		
		return new ElementCollectionValue(members);
	}
	
	private SubmodelElementCollection readByRange(SubmodelElementCollection records,
													TimeSeriesRange range) throws IOException {
		if ( range instanceof TimeSeriesRange.Count count ) {
			return readByLength(records, count.length());
		}
		else if ( range instanceof TimeSeriesRange.Trailing trailing ) {
			return (trailing.anchor() == TimeSeriesRange.Anchor.NOW)
					? readByDuration(records, trailing.duration())		// 현재 시각 기준
					: readByLast(records, trailing.duration());			// 마지막 레코드 시각 기준
		}
		else if ( range instanceof TimeSeriesRange.Absolute absolute ) {
			return listBetween(records, absolute.from(), absolute.to());
		}
		else {
			throw new IllegalStateException("Invalid range: " + range);
		}
	}
	private ElementCollectionValue readByRange(ElementCollectionValue records,
												TimeSeriesRange range) throws IOException {
		if ( range instanceof TimeSeriesRange.Count count ) {
			return readByLength(records, count.length());
		}
		else if ( range instanceof TimeSeriesRange.Trailing trailing ) {
			return (trailing.anchor() == TimeSeriesRange.Anchor.NOW)
					? readByDuration(records, trailing.duration())
					: readByLast(records, trailing.duration());
		}
		else if ( range instanceof TimeSeriesRange.Absolute absolute ) {
			return listBetween(records, absolute.from(), absolute.to());
		}
		else {
			throw new IllegalStateException("Invalid range: " + range);
		}
	}
	
	private SubmodelElementCollection readByLength(SubmodelElementCollection records, int length)
		throws IOException {
		int recCount = records.getValue().size();
		if ( recCount > length ) {
			List<SubmodelElement> recList = records.getValue();
			int skipCount = recCount - length;
			for ( int i = 0; i < skipCount; i++ ) {
				recList.remove(0);
			}
		}
		
		return records;
	}
	private ElementCollectionValue readByLength(ElementCollectionValue records, int length)
		throws IOException {
		int recCount = records.size();
		if ( recCount > length ) {
			var reduced = KeyValueFStream.from(records.getFieldMap())
										.drop(recCount - length)
										.toKeyValueStream(kv -> kv)
										.toMap(new LinkedHashMap<>());
			records = new ElementCollectionValue(reduced);
		}
		
		return records;
	}
	
	private SubmodelElementCollection readByDuration(SubmodelElementCollection smec, @NotNull Duration duration)
		throws IOException {
		Instant startTs = Instant.now().minus(duration);
		return listAfterOrEqual(smec, startTs);
	}
	private ElementCollectionValue readByDuration(ElementCollectionValue records,
													@NotNull Duration duration) throws IOException {
		Instant start = Instant.now().minus(duration);
		return listAfterOrEqual(records, start);
	}
	
	private SubmodelElementCollection readByLast(SubmodelElementCollection smec, @NotNull Duration duration)
		throws IOException {
		if ( smec.getValue().size() <= 1 ) {
			return smec;
		}
		
		var lastSmc = (SubmodelElementCollection)Iterables.getLast(smec.getValue());
		var tsProp = (Property)lastSmc.getValue().get(0);
		Instant lastTs = DataTypes.DATE_TIME.parseValueString(tsProp.getValue());
		Instant start = lastTs.minus(duration);
		return listAfterOrEqual(smec, start);
	}
	private ElementCollectionValue readByLast(ElementCollectionValue records,
												@NotNull Duration duration) throws IOException {
		if ( records.size() <= 1 ) {
			return records;
		}
		
		var last = (ElementCollectionValue)Iterables.getLast(records.getFieldMap().values());
		Instant lastTs = (Instant)last.getField("Time").toValueObject();
		Instant start = lastTs.minus(duration);
		return listAfterOrEqual(records, start);
	}

	// 절대 시간 범위 [from, to] 필터. from/to 중 하나가 null이면 해당 경계는 개방.
	private SubmodelElementCollection listBetween(SubmodelElementCollection smec,
													@Nullable Instant from, @Nullable Instant to) throws IOException {
		DefaultRecord recHandle = new DefaultRecord(m_tsMetadata.getRecord());
		for ( Iterator<SubmodelElement> iter = smec.getValue().iterator(); iter.hasNext(); ) {
			SubmodelElement rec = iter.next();
			recHandle.updateFromAasModel(rec);
			Instant ts = recHandle.getTimestamp();

			if ( (from != null && ts.isBefore(from)) || (to != null && ts.isAfter(to)) ) {
				iter.remove();
			}
		}

		return smec;
	}
	private ElementCollectionValue listBetween(ElementCollectionValue records,
												@Nullable Instant from, @Nullable Instant to) throws IOException {
		var reduced = KeyValueFStream.from(records.getFieldMap())
									.filterValue(field -> {
										ElementCollectionValue rec = (ElementCollectionValue)field;
										Instant ts = (Instant)rec.getField("Time").toValueObject();
										return (from == null || !ts.isBefore(from)) && (to == null || !ts.isAfter(to));
									})
									.toMap(new LinkedHashMap<>());
		return new ElementCollectionValue(reduced);
	}

	private SubmodelElementCollection listAfterOrEqual(SubmodelElementCollection smec, Instant start)
		throws IOException {
		DefaultRecord recHandle = new DefaultRecord(m_tsMetadata.getRecord());
		for ( Iterator<SubmodelElement> iter = smec.getValue().iterator(); iter.hasNext(); ) {
			SubmodelElement rec = iter.next();
			recHandle.updateFromAasModel(rec);
			Instant ts = recHandle.getTimestamp();
			
			if ( ts.isBefore(start) ) {
				iter.remove();
			}
			else {
				break;
			}
		}
		
		return smec;
	}
	private ElementCollectionValue listAfterOrEqual(ElementCollectionValue records,
													Instant start) throws IOException {
		var reduced = KeyValueFStream.from(records.getFieldMap())
									.filterValue(field -> {
										ElementCollectionValue rec = (ElementCollectionValue)field;
										Instant ts = (Instant)rec.getField("Time").toValueObject();
										return !ts.isBefore(start);
									})
									.toMap(new LinkedHashMap<>());
		return new ElementCollectionValue(reduced);
	}
}
