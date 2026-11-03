package mdt.workflow.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import utils.Preconditions;


/**
 * task 기술자({@link TaskDescriptor})에 붙는 이름-값 쌍의 옵션.
 * <p>
 * JSON으로는 {@code {"name": ..., "value": ...}} 형식으로 기록된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
@JsonPropertyOrder({"name", "value"})
public class Option {
	private final String m_name;
	private final String m_value;

	/**
	 * 옵션 객체를 만든다.
	 *
	 * @param name	옵션 이름.
	 * @param value	옵션 값. {@code null} 가능.
	 * @throws IllegalArgumentException	{@code name}이 {@code null}인 경우.
	 */
	public Option(@JsonProperty("name") String name, @JsonProperty("value") String value) {
		Preconditions.checkNotNullArgument(name, "name is null");
		
		m_name = name;
		m_value = value;
	}

	/**
	 * 옵션 이름을 반환한다.
	 *
	 * @return	옵션 이름.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * 옵션 값을 반환한다.
	 *
	 * @return	옵션 값. 지정되지 않은 경우 {@code null}.
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * 옵션을 명령행 인자 형식 {@code ["--<name>", "<value>"]}으로 변환한다.
	 *
	 * @return	명령행 인자 목록 (두 원소).
	 * @throws NullPointerException	옵션 값이 {@code null}인 경우.
	 */
	public List<String> toCommandOptionSpec() {
		return List.of(String.format("--%s", getName()), getValue());
	}
	
	@Override
	public String toString() {
		return String.format("%s=%s", getName(), getValue());
	}
}
