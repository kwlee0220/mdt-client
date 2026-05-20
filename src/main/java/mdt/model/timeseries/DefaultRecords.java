package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import com.google.common.base.Preconditions;

import utils.stream.FStream;

import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultRecords extends SubmodelElementCollectionEntity implements Records {
	private List<? extends DefaultRecord> m_recordList;
	private RecordMetadata m_schema;
	
	public DefaultRecords() {
		setIdShort("Records");
		setSemanticId(Records.SEMANTIC_ID_REFERENCE);

		m_recordList = List.of();
	}
	
	public DefaultRecords(RecordMetadata schema) {
		this();
		
		m_schema = schema;
	}
	
	public DefaultRecords(List<? extends DefaultRecord> recordList) {
		Preconditions.checkArgument(recordList != null, "'recordList' should not be null");
		
		setIdShort("Records");
		setSemanticId(Records.SEMANTIC_ID_REFERENCE);

		m_recordList = recordList;
	}
	
	public DefaultRecords(RecordMetadata metadata, List<? extends DefaultRecord> recordList) {
		Preconditions.checkArgument(metadata != null, "RecordMetadata should not be null");
		Preconditions.checkArgument(recordList != null, "'recordList' should not be null");
		
		setIdShort("Records");
		setSemanticId(Records.SEMANTIC_ID_REFERENCE);

		m_recordList = recordList;
		m_schema = metadata;
	}

//	@Override
//	public RecordMetadata getRecordMetadata() {
//		return m_metadata;
//	}
	
	@Override
	public List<? extends DefaultRecord> getRecordList() {
		return m_recordList;
	}
	
	public void setRecordList(List<? extends DefaultRecord> recList) {
		Preconditions.checkArgument(recList != null, "'recordList' should not be null");

		this.m_recordList = recList;
	}
	
	public int size() {
		return m_recordList.size();
	}

	@Override
	public void updateFromAasModel(SubmodelElement model) {
		super.updateFromAasModel(model);

		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		
		if ( m_schema == null ) {
			SubmodelElementCollection recSmc
							= FStream.from(smc.getValue())
												.castSafely(SubmodelElementCollection.class)
												.findFirst()
												.getOrThrow(() -> new IllegalStateException("Records has no Record"));
			DefaultRecord rec = new DefaultRecord();
			rec.updateFromAasModel(recSmc);
			m_schema = rec.getMetadata();
		}
		
		m_recordList = FStream.from(smc.getValue())
								.castSafely(SubmodelElementCollection.class)
								.map(recProp -> {
									DefaultRecord record = new DefaultRecord(m_schema);
									record.updateFromAasModel(recProp);
									return record;
								})
								.toList();
	}

	@Override
	public void updateAasModel(SubmodelElement model) {
		super.updateAasModel(model);

		SubmodelElementCollection smc = (SubmodelElementCollection)model;
		List<SubmodelElement> records = FStream.from(m_recordList)
												.map(DefaultRecord::newSubmodelElement)
												.cast(SubmodelElement.class)
												.toList();
		smc.setValue(records);
	}
}
