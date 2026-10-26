package mdt.rt;

import java.util.Map;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Ros2Types {
	private Ros2Types() {
		throw new AssertionError("Should not be called: " + getClass());
	}
	
	public static final record Header(long stamp, String frame_id) {}
	public static final record Position(float x, float y, float z) {}
	public static final record Orientation(float x, float y, float z, float w) {}
	public final record Pose(Position position, Orientation orientation) { }
	public static final record PoseStamped(Header header, Pose pose) {}

	/**
	 * 로봇 관절들의 상태. (ROS2 {@code sensor_msgs/JointState}에 해당)
	 * <p>
	 * ROS2 원본 메시지는 관절 이름과 값을 각각 별도의 배열({@code name}/{@code position}/…)로 갖지만,
	 * 로봇 트윈(RESTful gateway)은 이를 <b>관절 이름 → 값</b>의 맵으로 변환하여 전달하기 때문에
	 * 여기서도 맵으로 표현한다.
	 * <p>
	 * 값을 사용할 수 없는 관절({@code null}로 전달된 관절)은 맵에 포함되지 않는다. 즉 맵에 있는
	 * 관절은 항상 사용 가능한 값을 갖는다.
	 *
	 * @param position 관절 이름 → 위치.
	 * @param velocity 관절 이름 → 속도. 로봇이 제공하지 않으면 빈 맵.
	 * @param effort   관절 이름 → 힘. 로봇이 제공하지 않으면 빈 맵.
	 */
	public static final record JointStates(Map<String,Float> position, Map<String,Float> velocity,
											Map<String,Float> effort) {}
}
