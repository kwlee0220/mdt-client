package mdt.model.sm.data;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link Line} 인터페이스의 기본 구현 클래스이다.
 * <p>
 * 각 속성은 SubmodelElementCollection의 Property에 대응되며, 각 Property의 idShort는
 * 인터페이스의 접근자 문서에 명시되어 있다.
 * SubmodelElementCollection의 idShort는 {@code Line}이다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultLine extends SubmodelElementCollectionEntity implements Line {
	@PropertyField(idShort="LineID") private String lineID;
	@PropertyField(idShort="LineName") private String lineName;
	@PropertyField(idShort="LineType") private String lineType;
	@PropertyField(idShort="UseIndicator") private String useIndicator;
	@PropertyField(idShort="LineStatus") private String lineStatus;
	
//	@SMLField(idShort="BOMs", elementClass=DefaultBOM.class)
//	private List<BOM> BOMs = Lists.newArrayList();
//	
//	@SMLField(idShort="ItemMasters", elementClass=DefaultItemMaster.class)
//	private List<ItemMaster> itemMasters = Lists.newArrayList();
//	
//	@SMLField(idShort="Routings", elementClass=DefaultRouting.class)
//	private List<Routing> routings = Lists.newArrayList();
	
	/**
	 * idShort가 {@code Line}인 빈 {@code DefaultLine} 객체를 생성한다.
	 */
	public DefaultLine() {
		setIdShort("Line");
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getLineID());
	}
}
