package mdt.model;

import lombok.Getter;
import lombok.Setter;

import mdt.model.sm.entity.PropertyField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link SubModelInfo}의 기본 구현으로, 모델 메타 정보를 idShort가 {@code "SubModelInfo"}인
 * SubmodelElementCollection 엔티티로 표현한다.
 * <p>
 * 각 필드는 같은 이름의 하위 Property(Title, Creator, Type, Format, Identifier)에 매핑된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultSubModelInfo extends SubmodelElementCollectionEntity implements SubModelInfo {
	@PropertyField(idShort="Title") private String title;
	@PropertyField(idShort="Creator") private String creator;
	@PropertyField(idShort="Type") private String type;
	@PropertyField(idShort="Format") private String format;
	@PropertyField(idShort="Identifier") private String identifier;
	
	/** idShort가 {@code "SubModelInfo"}로 설정된 빈 객체를 생성한다. */
	public DefaultSubModelInfo() {
		setIdShort("SubModelInfo");
	}
	
	@Override
	public String toString() {
		return String.format("SubModelInfo[%s]", this.title);
	}
}
