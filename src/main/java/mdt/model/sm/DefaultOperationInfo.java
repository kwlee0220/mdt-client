package mdt.model.sm;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

import mdt.model.DefaultInput;
import mdt.model.DefaultOutput;
import mdt.model.Input;
import mdt.model.Output;
import mdt.model.sm.entity.SMListField;
import mdt.model.sm.entity.SubmodelElementCollectionEntity;


/**
 * {@link OperationEntity}의 기본 구현으로, 입출력 인자 목록을 갖는 SubmodelElementCollection 엔티티.
 * <p>
 * 입력 인자 목록은 idShort가 {@code Inputs}인 SubmodelElementList({@link DefaultInput} 원소)에,
 * 출력 인자 목록은 {@code Outputs}인 SubmodelElementList({@link DefaultOutput} 원소)에 대응된다.
 * 접근자는 Lombok {@code @Getter}/{@code @Setter}로 생성된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Getter @Setter
public class DefaultOperationInfo extends SubmodelElementCollectionEntity
										implements OperationEntity {
	@SMListField(idShort="Inputs", elementClass=DefaultInput.class)
	private List<Input> inputs = Lists.newArrayList();
	
	@SMListField(idShort="Outputs", elementClass=DefaultOutput.class)
	private List<Output> outputs = Lists.newArrayList();
	
	/**
	 * 주어진 idShort를 갖는 빈 연산 정보 엔티티를 생성한다.
	 *
	 * @param idShort	SubmodelElementCollection의 idShort.
	 */
	protected DefaultOperationInfo(String idShort) {
		setIdShort(idShort);
	}
}
