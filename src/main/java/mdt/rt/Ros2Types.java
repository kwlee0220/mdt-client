package mdt.rt;

import java.util.Map;

/**
 * MDT에서 사용하는 ROS2 메시지 타입들을 Java record로 정의한다.
 * <p>
 * 각 record와 JSON/{@link mdt.model.sm.value.ElementValue} 사이의 변환은 {@link Ros2SerDes}가 제공한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Ros2Types {
	private Ros2Types() {
		throw new AssertionError("Should not be called: " + getClass());
	}
	
	/**
	 * 메시지 header. (ROS2 {@code std_msgs/Header}에 해당)
	 *
	 * @param stamp		시각. epoch 기준 나노초.
	 * @param frame_id	좌표계(frame) 식별자. 지정되지 않은 경우 {@code null}.
	 */
	public static final record Header(long stamp, String frame_id) {}

	/**
	 * 3차원 위치. (ROS2 {@code geometry_msgs/Point}에 해당)
	 *
	 * @param x	x 좌표.
	 * @param y	y 좌표.
	 * @param z	z 좌표.
	 */
	public static final record Position(float x, float y, float z) {}

	/**
	 * 쿼터니언으로 표현된 방향. (ROS2 {@code geometry_msgs/Quaternion}에 해당)
	 *
	 * @param x	x 성분.
	 * @param y	y 성분.
	 * @param z	z 성분.
	 * @param w	w 성분.
	 */
	public static final record Orientation(float x, float y, float z, float w) {}

	/**
	 * 위치와 방향으로 구성된 자세. (ROS2 {@code geometry_msgs/Pose}에 해당)
	 *
	 * @param position		위치.
	 * @param orientation	방향.
	 */
	public final record Pose(Position position, Orientation orientation) { }

	/**
	 * header가 붙은 자세. (ROS2 {@code geometry_msgs/PoseStamped}에 해당)
	 *
	 * @param header	메시지 header.
	 * @param pose		자세.
	 */
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
