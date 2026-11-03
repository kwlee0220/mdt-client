package mdt.model.timeseries;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.Preconditions;
import utils.stream.FStream;

import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link Records}의 기본 구현 클래스.
 * <p>
 * idShort는 {@code "Records"}이며, 각 레코드는 하위 SubmodelElementCollection 하나로 표현된다.
 * 레코드 스키마({@link RecordMetadata})가 주어지지 않은 경우, AAS 모델로부터 읽을 때 첫번째 레코드의
 * 구조로부터 스키마를 유추한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class DefaultRecords extends SubmodelElementCollectionEntity implements Records {
	private List<? extends DefaultRecord> m_recordList;
	private RecordMetadata m_schema;
	
	/**
	 * 레코드가 없고 스키마도 지정되지 않은 Records 객체를 생성한다.
	 */
	public DefaultRecords() {
		setIdShort("Records");
		setSemanticId(Records.SEMANTIC_ID_REFERENCE);

		m_recordList = List.of();
	}
	
	/**
	 * 주어진 스키마를 사용하는, 레코드가 없는 Records 객체를 생성한다.
	 *
	 * @param schema	레코드 스키마.
	 */
	public DefaultRecords(RecordMetadata schema) {
		this();
		
		m_schema = schema;
	}
	
	/**
	 * 주어진 레코드 리스트로 구성된 Records 객체를 생성한다. 스키마는 지정되지 않는다.
	 *
	 * @param recordList	레코드 리스트.
	 * @throws IllegalArgumentException	{@code recordList}가 {@code null}인 경우.
	 */
	public DefaultRecords(List<? extends DefaultRecord> recordList) {
		Preconditions.checkNotNullArgument(recordList, "'recordList' should not be null");
		
		setIdShort("Records");
		setSemanticId(Records.SEMANTIC_ID_REFERENCE);

		m_recordList = recordList;
	}
	
	/**
	 * 주어진 스키마와 레코드 리스트로 구성된 Records 객체를 생성한다.
	 *
	 * @param metadata		레코드 스키마.
	 * @param recordList	레코드 리스트.
	 * @throws IllegalArgumentException	{@code metadata} 또는 {@code recordList}가 {@code null}인 경우.
	 */
	public DefaultRecords(RecordMetadata metadata, List<? extends DefaultRecord> recordList) {
		Preconditions.checkNotNullArgument(metadata, "RecordMetadata should not be null");
		Preconditions.checkNotNullArgument(recordList, "'recordList' should not be null");
		
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
	
	/**
	 * 레코드 리스트를 설정한다.
	 *
	 * @param recList	레코드 리스트.
	 * @throws IllegalArgumentException	{@code recList}가 {@code null}인 경우.
	 */
	public void setRecordList(List<? extends DefaultRecord> recList) {
		Preconditions.checkNotNullArgument(recList, "'recordList' should not be null");

		this.m_recordList = recList;
	}
	
	/**
	 * 레코드 개수를 반환한다.
	 *
	 * @return	레코드 개수.
	 */
	public int size() {
		return m_recordList.size();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 스키마가 지정되지 않은 경우에는 첫번째 하위 SubmodelElementCollection으로부터 스키마를 유추한 뒤,
	 * 모든 하위 SubmodelElementCollection을 그 스키마의 {@link DefaultRecord}로 읽는다.
	 *
	 * @throws IllegalStateException	스키마가 지정되지 않았는데 레코드가 하나도 없는 경우.
	 */
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
