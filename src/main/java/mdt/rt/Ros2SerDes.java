package mdt.rt;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import utils.json.JsonSerDe;
import utils.stream.KeyValueFStream;

import mdt.model.MDTModelSerDe;
import mdt.model.sm.value.ElementCollectionValue;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;
import mdt.model.sm.value.PropertyValue;
import mdt.model.sm.value.PropertyValue.DoublePropertyValue;
import mdt.model.sm.value.PropertyValue.FloatPropertyValue;
import mdt.model.sm.value.PropertyValue.LongPropertyValue;
import mdt.model.sm.value.PropertyValue.StringPropertyValue;
import mdt.rt.Ros2Types.Header;
import mdt.rt.Ros2Types.JointStates;
import mdt.rt.Ros2Types.Pose;
import mdt.rt.Ros2Types.PoseStamped;
import mdt.rt.Ros2Types.Position;

/**
 * ROS2 메시지의 JSON 표현과 {@link Ros2Types}의 record 사이를 변환하는 {@link JsonSerDe} 모음.
 * <p>
 * JSON 표현은 로봇 트윈(RESTful gateway)이 전달하는 ROS2 메시지 형식을 그대로 따른다.
 * 실수 값의 경우 로봇 트윈이 {@code NaN}/{@code Infinity}를 {@code null}로 변환하여 보내기 때문에,
 * 역직렬화 시 {@code null}은 {@link Double#NaN}으로 읽고 직렬화 시 유한하지 않은 값은 다시
 * {@code null}로 기록한다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Ros2SerDes {
	private static final long NANOS_PER_SEC = 1_000_000_000L;
	
	public static final HeaderSerDe HEADER_SERDE = new HeaderSerDe();
	public static final PoseSerDe POSE_SERDE = new PoseSerDe();
	public static final PoseStampedSerDe POSE_STAMPED_SERDE = new PoseStampedSerDe();
	public static final JointsSerDe JOINTS_SERDE = new JointsSerDe();
	public static final JointStatesSerDe JOINT_STATES_SERDE = new JointStatesSerDe();

	private Ros2SerDes() {
		throw new AssertionError("Should not be called: " + getClass());
	}

	/**
	 * ROS2 {@code std_msgs/Header} 메시지의 JSON 표현과 {@link Ros2Types.Header} 사이의 변환기.
	 * <p>
	 * JSON 표현은 ROS2 형식을 그대로 따른다. 즉, 시각({@code stamp})은 {@code sec}와 {@code nanosec}
	 * 두 필드로 구성된 객체이다.
	 * <pre>{@code
	 * { "stamp": { "sec": 1730000000, "nanosec": 123456789 }, "frame_id": "base_link" }
	 * }</pre>
	 * 반면 {@link Ros2Types.Header#stamp()}는 이 둘을 합쳐 epoch 기준 나노초 하나로 표현한 값이다.
	 */
	public static final class HeaderSerDe implements JsonSerDe<Ros2Types.Header>,
																ElementValueSerDe<Ros2Types.Header> {
		/**
		 * ROS2 {@code std_msgs/Header} JSON을 {@link Ros2Types.Header}로 변환한다.
		 *
		 * @param json	변환할 JSON. {@code stamp} 필드는 필수이고 {@code frame_id}는 생략 가능하다.
		 * @return	변환된 {@link Ros2Types.Header}.
		 * @throws IOException	{@code json}이 객체가 아니거나 {@code stamp} 필드의 구성이
		 * 						ROS2 형식과 다른 경우.
		 */
		@Override
		public Ros2Types.Header deserialize(JsonNode json) throws IOException {
			return parseHeader(json, "Header");
		}

		/**
		 * {@link Ros2Types.Header}를 ROS2 {@code std_msgs/Header} JSON으로 변환한다.
		 * <p>
		 * {@code stamp}는 {@code sec}와 {@code nanosec}로 나뉘어 기록되며, {@code nanosec}는
		 * ROS2 정의({@code uint32})에 맞춰 항상 0 이상의 값이 된다.
		 *
		 * @param obj	변환할 header.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Ros2Types.Header obj) throws IOException {
			return toJsonNode(obj);
		}

		@Override
		public ElementCollectionValue serializeToElementValue(Header hdr) throws IOException {
			LinkedHashMap<String,ElementValue> valueMap = new LinkedHashMap<>();
			valueMap.put("Stamp", PropertyValue.LONG(hdr.stamp()));
			valueMap.put("FrameId", PropertyValue.STRING(hdr.frame_id()));	
			return new ElementCollectionValue(valueMap);
		}

		@Override
		public Header deserializeFromElementValue(ElementValue elem) throws IOException {
			if ( elem instanceof ElementCollectionValue smcv ) {
				Long stamp = ((LongPropertyValue)smcv.getField("Stamp")).toValueObject();
				String frameId = ((StringPropertyValue)smcv.getField("FrameId")).toValueObject();
				
				return new Ros2Types.Header(stamp, frameId);
			}
			else {
				throw new IOException(String.format("invalid Header (not ElementCollectionValue): %s", elem));
			}
		}
	}

	/**
	 * ROS2 {@code geometry_msgs/Point} 메시지의 JSON 표현과 {@link Ros2Types.Position} 사이의 변환기.
	 * <pre>{@code
	 * { "x": 1.0, "y": 2.0, "z": 3.0 }
	 * }</pre>
	 */
	public static final class PositionSerDe implements JsonSerDe<Ros2Types.Position>,
														ElementValueSerDe<Ros2Types.Position> {
		/**
		 * ROS2 {@code geometry_msgs/Point} JSON을 {@link Ros2Types.Position}으로 변환한다.
		 *
		 * @param json	변환할 JSON. {@code x}, {@code y}, {@code z} 필드가 모두 필요하다.
		 * @return	변환된 {@link Ros2Types.Position}.
		 * @throws IOException	{@code json}이 객체가 아니거나 필요한 필드가 없거나 숫자가 아닌 경우.
		 */
		@Override
		public Ros2Types.Position deserialize(JsonNode json) throws IOException {
			return parsePosition(json, "Position");
		}

		/**
		 * {@link Ros2Types.Position}을 ROS2 {@code geometry_msgs/Point} JSON으로 변환한다.
		 *
		 * @param obj	변환할 position.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Ros2Types.Position obj) throws IOException {
			return toJsonNode(obj);
		}

		@Override
		public ElementValue serializeToElementValue(Position elem) throws IOException {
			return toElementValue(elem);
		}

		@Override
		public Position deserializeFromElementValue(ElementValue elem) throws IOException {
			return parsePosition(elem, "Position");
		}
	}

	/**
	 * ROS2 {@code geometry_msgs/Quaternion} 메시지의 JSON 표현과 {@link Ros2Types.Orientation}
	 * 사이의 변환기.
	 * <pre>{@code
	 * { "x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0 }
	 * }</pre>
	 */
	public static final class OrientationSerDe implements JsonSerDe<Ros2Types.Orientation>,
														ElementValueSerDe<Ros2Types.Orientation> {
		/**
		 * ROS2 {@code geometry_msgs/Quaternion} JSON을 {@link Ros2Types.Orientation}으로 변환한다.
		 *
		 * @param json	변환할 JSON. {@code x}, {@code y}, {@code z}, {@code w} 필드가 모두 필요하다.
		 * @return	변환된 {@link Ros2Types.Orientation}.
		 * @throws IOException	{@code json}이 객체가 아니거나 필요한 필드가 없거나 숫자가 아닌 경우.
		 */
		@Override
		public Ros2Types.Orientation deserialize(JsonNode json) throws IOException {
			return parseOrientation(json, "Orientation");
		}

		/**
		 * {@link Ros2Types.Orientation}을 ROS2 {@code geometry_msgs/Quaternion} JSON으로 변환한다.
		 *
		 * @param obj	변환할 orientation.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Ros2Types.Orientation obj) throws IOException {
			return toJsonNode(obj);
		}

		@Override
		public ElementValue serializeToElementValue(Ros2Types.Orientation elem) throws IOException {
			return toElementValue(elem);
		}

		@Override
		public Ros2Types.Orientation deserializeFromElementValue(ElementValue elem) throws IOException {
			return parseOrientation(elem, "Position");
		}
	}

	/**
	 * ROS2 {@code geometry_msgs/Pose} 메시지의 JSON 표현과 {@link Ros2Types.Pose} 사이의 변환기.
	 * <pre>{@code
	 * {
	 *   "position": { "x": 1.0, "y": 2.0, "z": 3.0 },
	 *   "orientation": { "x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0 }
	 * }
	 * }</pre>
	 */
	public static final class PoseSerDe implements JsonSerDe<Ros2Types.Pose>,
													ElementValueSerDe<Ros2Types.Pose> {
		/**
		 * ROS2 {@code geometry_msgs/Pose} JSON을 {@link Ros2Types.Pose}로 변환한다.
		 *
		 * @param json	변환할 JSON. {@code position}과 {@code orientation} 필드가 모두 필요하다.
		 * @return	변환된 {@link Ros2Types.Pose}.
		 * @throws IOException	{@code json}이 객체가 아니거나 하위 메시지의 구성이 ROS2 형식과
		 * 						다른 경우.
		 */
		@Override
		public Ros2Types.Pose deserialize(JsonNode json) throws IOException {
			return parsePose(json, "Pose");
		}

		/**
		 * {@link Ros2Types.Pose}를 ROS2 {@code geometry_msgs/Pose} JSON으로 변환한다.
		 *
		 * @param obj	변환할 pose.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Ros2Types.Pose obj) throws IOException {
			return Ros2SerDes.toJsonNode(obj);
		}

		@Override
		public ElementValue serializeToElementValue(Ros2Types.Pose elem) throws IOException {
			return toElementValue(elem);
		}

		@Override
		public Ros2Types.Pose deserializeFromElementValue(ElementValue elem) throws IOException {
			return parsePose(elem, "Pose");
		}
		
		public JsonNode toJsonNode(SubmodelElement sme) throws IOException {
			var smev = ElementValues.getValue(sme);
			var pose = deserializeFromElementValue(smev);
			return serialize(pose);
		}
		
		public void updateSubmodelElement(SubmodelElement sme, JsonNode jnode) throws IOException {
			Pose pose = deserialize(jnode);
			ElementValue value = serializeToElementValue(pose);
			ElementValues.update(sme, value);
		}
	}

	/**
	 * ROS2 {@code geometry_msgs/PoseStamped} 메시지의 JSON 표현과 {@link Ros2Types.PoseStamped}
	 * 사이의 변환기.
	 * <pre>{@code
	 * {
	 *   "header": { "stamp": { "sec": 1730000000, "nanosec": 0 }, "frame_id": "map" },
	 *   "pose": {
	 *     "position": { "x": 1.0, "y": 2.0, "z": 3.0 },
	 *     "orientation": { "x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0 }
	 *   }
	 * }
	 * }</pre>
	 */
	public static final class PoseStampedSerDe implements JsonSerDe<Ros2Types.PoseStamped>,
																	ElementValueSerDe<Ros2Types.PoseStamped> {
		/**
		 * ROS2 {@code geometry_msgs/PoseStamped} JSON을 {@link Ros2Types.PoseStamped}로 변환한다.
		 *
		 * @param json	변환할 JSON. {@code header}와 {@code pose} 필드가 모두 필요하다.
		 * @return	변환된 {@link Ros2Types.PoseStamped}.
		 * @throws IOException	{@code json}이 객체가 아니거나 하위 메시지의 구성이 ROS2 형식과
		 * 						다른 경우.
		 */
		@Override
		public Ros2Types.PoseStamped deserialize(JsonNode json) throws IOException {
			JsonNode node = asObject(json, "PoseStamped");
			return new Ros2Types.PoseStamped(parseHeader(node.get("header"), "PoseStamped.header"),
											parsePose(node.get("pose"), "PoseStamped.pose"));
		}

		/**
		 * {@link Ros2Types.PoseStamped}를 ROS2 {@code geometry_msgs/PoseStamped} JSON으로 변환한다.
		 *
		 * @param obj	변환할 pose.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Ros2Types.PoseStamped obj) throws IOException {
			ObjectNode node = newObjectNode();
			node.set("header", Ros2SerDes.toJsonNode(obj.header()));
			node.set("pose", Ros2SerDes.toJsonNode(obj.pose()));

			return node;
		}

		@Override
		public ElementCollectionValue serializeToElementValue(PoseStamped elem) throws IOException {
			LinkedHashMap<String,ElementValue> valueMap = new LinkedHashMap<>();
			valueMap.put("Pose", POSE_SERDE.serializeToElementValue(elem.pose()));
			valueMap.put("Header", HEADER_SERDE.serializeToElementValue(elem.header()));
			return new ElementCollectionValue(valueMap);
		}

		@Override
		public PoseStamped deserializeFromElementValue(ElementValue elem) throws IOException {
			if ( elem instanceof ElementCollectionValue smcv ) {
				Header header = HEADER_SERDE.deserializeFromElementValue(smcv.getField("Header"));
				Ros2Types.Pose pose = POSE_SERDE.deserializeFromElementValue(smcv.getField("Pose"));
				return new PoseStamped(header, pose);
			}
			else {
				throw new IOException(String.format("invalid PoseStamped (not ElementCollectionValue): %s", elem));
			}
		}
		
		public JsonNode toJsonNode(SubmodelElement sme) throws IOException {
			ElementValue smev = ElementValues.getValue(sme);
			PoseStamped poseStamped = deserializeFromElementValue(smev);
			return serialize(poseStamped);
		}
		
		public void updateSubmodelElement(SubmodelElement sme, JsonNode jnode) throws IOException {
			PoseStamped pose = deserialize(jnode);
			ElementValue value = serializeToElementValue(pose);
			ElementValues.update(sme, value);
		}
	}

	/**
	 * 로봇 관절 상태의 JSON 표현과 {@link Ros2Types.JointStates} 사이의 변환기.
	 * <p>
	 * ROS2 {@code sensor_msgs/JointState}는 관절 이름과 값을 별도의 배열로 갖지만, 로봇 트윈은
	 * 이를 관절 이름을 키로 하는 객체로 변환하여 전달한다.
	 * <pre>{@code
	 * {
	 *   "position": { "panda_joint1": 0.0, "panda_joint2": -0.785 },
	 *   "velocity": { "panda_joint1": 0.0, "panda_joint2": 0.0 },
	 *   "effort": { "panda_joint1": 0.1, "panda_joint2": -0.2 }
	 * }
	 * }</pre>
	 */
	public static final class JointStatesSerDe implements JsonSerDe<Ros2Types.JointStates>,
															ElementValueSerDe<Ros2Types.JointStates> {
		/**
		 * 로봇 관절 상태 JSON을 {@link Ros2Types.JointStates}로 변환한다.
		 * <p>
		 * 값이 {@code null}인 관절은 사용할 수 없는 값이므로 결과 맵에서 제외된다.
		 *
		 * @param json	변환할 JSON. {@code position} 필드는 필수이고 {@code velocity}와
		 * 				{@code effort}는 생략 가능하다.
		 * @return	변환된 {@link Ros2Types.JointStates}.
		 * @throws IOException	{@code json}이 객체가 아니거나, {@code position}이 없거나,
		 * 						각 필드가 관절 이름 → 숫자 형태의 객체가 아닌 경우.
		 */
		@Override
		public Ros2Types.JointStates deserialize(JsonNode json) throws IOException {
			JsonNode node = asObject(json, "JointStates");
			return new Ros2Types.JointStates(
							parseJointValues(getObjectField(node, "JointStates", "position"),
											"JointStates.position"),
							parseOptionalJointValues(node, "JointStates", "velocity"),
							parseOptionalJointValues(node, "JointStates", "effort"));
		}

		/**
		 * {@link Ros2Types.JointStates}를 로봇 관절 상태 JSON으로 변환한다.
		 * <p>
		 * {@code velocity}와 {@code effort}는 비어 있더라도 빈 객체로 기록된다.
		 *
		 * @param obj	변환할 관절 상태.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Ros2Types.JointStates obj) throws IOException {
			ObjectNode node = newObjectNode();
			node.set("position", Ros2SerDes.toJsonNode(obj.position()));
			node.set("velocity", Ros2SerDes.toJsonNode(obj.velocity()));
			node.set("effort", Ros2SerDes.toJsonNode(obj.effort()));

			return node;
		}

		@Override
		public ElementCollectionValue serializeToElementValue(JointStates jointStates) throws IOException {
			Map<String,Float> postions = jointStates.position();
			if ( postions == null ) {
				throw new IOException("invalid jointStates (position is null): " + jointStates);
			}
			
			var valueMap = KeyValueFStream.from(postions)
											.filterValue(v -> v != null)
											.mapValue((k,v) -> (ElementValue)PropertyValue.FLOAT(v))
											.toMap(new LinkedHashMap<>());
			return new ElementCollectionValue(valueMap);
		}

		@Override
		public JointStates deserializeFromElementValue(ElementValue elem) throws IOException {
			if ( elem instanceof ElementCollectionValue smcv ) {
				Map<String,Float> positions = KeyValueFStream.from(smcv.getFieldMap())
																.filterValue(v -> v instanceof FloatPropertyValue)
																.mapValue((k,v) -> ((FloatPropertyValue)v).toValueObject())
																.toMap(new LinkedHashMap<>());
				return new JointStates(positions, Map.of(), Map.of());
			}
			
			throw new IOException(String.format("invalid JointStates (not ElementCollectionValue): %s", elem));
		}
		
		public JsonNode toJsonNode(SubmodelElement sme) throws IOException {
			ElementValue smev = ElementValues.getValue(sme);
			JointStates joints = deserializeFromElementValue(smev);
			return serialize(joints);
		}
		
		public void updateSubmodelElement(SubmodelElement sme, JsonNode jnode) throws IOException {
			JointStates joints = deserialize(jnode);
			ElementValue value = serializeToElementValue(joints);
			ElementValues.update(sme, value);
		}
	}

	public static final class JointsSerDe implements JsonSerDe<Map<String,Float>>,
															ElementValueSerDe<Map<String,Float>> {
		/**
		 * 로봇 관절 상태 JSON을 {@link Ros2Types.JointStates}로 변환한다.
		 * <p>
		 * 값이 {@code null}인 관절은 사용할 수 없는 값이므로 결과 맵에서 제외된다.
		 *
		 * @param json	변환할 JSON. {@code position} 필드는 필수이고 {@code velocity}와
		 * 				{@code effort}는 생략 가능하다.
		 * @return	변환된 {@link Ros2Types.JointStates}.
		 * @throws IOException	{@code json}이 객체가 아니거나, {@code position}이 없거나,
		 * 						각 필드가 관절 이름 → 숫자 형태의 객체가 아닌 경우.
		 */
		@Override
		public Map<String,Float> deserialize(JsonNode jnode) throws IOException {
			jnode = asObject(jnode, "Joints");
			return parseJointValues(jnode, "Joints");
		}

		/**
		 * {@link Ros2Types.JointStates}를 로봇 관절 상태 JSON으로 변환한다.
		 * <p>
		 * {@code velocity}와 {@code effort}는 비어 있더라도 빈 객체로 기록된다.
		 *
		 * @param obj	변환할 관절 상태.
		 * @return	변환된 JSON.
		 */
		@Override
		public JsonNode serialize(Map<String,Float> obj) throws IOException {
			return Ros2SerDes.toJsonNode(obj);
		}

		@Override
		public ElementCollectionValue serializeToElementValue(Map<String,Float> joints) throws IOException {
			var valueMap = KeyValueFStream.from(joints)
											.filterValue(v -> v != null)
											.mapValue((k,v) -> (ElementValue)PropertyValue.FLOAT(v))
											.toMap(new LinkedHashMap<>());
			return new ElementCollectionValue(valueMap);
		}

		@Override
		public Map<String,Float> deserializeFromElementValue(ElementValue elem) throws IOException {
			if ( elem instanceof ElementCollectionValue smcv ) {
				Map<String,Float> positions = KeyValueFStream.from(smcv.getFieldMap())
																.filterValue(v -> v instanceof FloatPropertyValue)
																.mapValue((k,v) -> ((FloatPropertyValue)v).toValueObject())
																.toMap(new LinkedHashMap<>());
				return positions;
			}
			
			throw new IOException(String.format("invalid JointStates (not ElementCollectionValue): %s", elem));
		}
		
		public JsonNode toJsonNode(SubmodelElement sme) throws IOException {
			return toJsonNode(ElementValues.getValue(sme));
		}
		public JsonNode toJsonNode(ElementValue smev) throws IOException {
			Map<String,Float> joints = deserializeFromElementValue(smev);
			return serialize(joints);
		}
		
		public void updateSubmodelElement(SubmodelElement sme, JsonNode jnode) throws IOException {
			Map<String,Float> joints = deserialize(jnode);
			ElementValue value = serializeToElementValue(joints);
			ElementValues.update(sme, value);
		}
	}
	
	private static Ros2Types.Header parseHeader(JsonNode json, String name) throws IOException {
		JsonNode node = asObject(json, name);

		JsonNode stampNode = getObjectField(node, name, "stamp");
		long stamp = getLongField(stampNode, name + ".stamp", "sec") * NANOS_PER_SEC
					+ getLongField(stampNode, name + ".stamp", "nanosec");

		// frame_id는 생략되거나 null일 수 있다.
		JsonNode frameIdNode = node.get("frame_id");
		String frameId = ( frameIdNode != null && !frameIdNode.isNull() ) ? frameIdNode.asText() : null;

		return new Ros2Types.Header(stamp, frameId);
	}

	private static Ros2Types.Pose parsePose(JsonNode json, String name) throws IOException {
		JsonNode node = asObject(json, name);
		return new Ros2Types.Pose(parsePosition(node.get("position"), name + ".position"),
									parseOrientation(node.get("orientation"), name + ".orientation"));
	}
	private static Ros2Types.Pose parsePose(ElementValue smev, String name) throws IOException {
		if ( smev instanceof ElementCollectionValue smcv ) {
			Ros2Types.Position pos = parsePosition(smcv.getField("Position"), name);
			Ros2Types.Orientation ort = parseOrientation(smcv.getField("Orientation"), name);
			return new Ros2Types.Pose(pos, ort);
		}
		
		throw new IOException(String.format("invalid %s (not ElementCollectionValue): %s", name, smev));
	}

	private static Ros2Types.Position parsePosition(JsonNode json, String name) throws IOException {
		JsonNode node = asObject(json, name);
		return new Ros2Types.Position(getFloatField(node, name, "x"),
										getFloatField(node, name, "y"),
										getFloatField(node, name, "z"));
	}
	private static Ros2Types.Position parsePosition(ElementValue smev, String name) throws IOException {
		if ( smev instanceof ElementCollectionValue smcv ) {
			Float x = getFloatField(smcv.getField("x"), "Position.x");
			Float y = getFloatField(smcv.getField("y"), "Position.y");
			Float z = getFloatField(smcv.getField("z"), "Position.z");
			if ( x == null || y == null || z == null ) {
				throw new IOException(String.format("invalid %s (some fields are null): %s", name, smev));
			}
			
			return new Ros2Types.Position(x, y, z);
		}
		
		throw new IOException(String.format("invalid %s (not ElementCollectionValue): %s", name, smev));
	}

	private static Ros2Types.Orientation parseOrientation(JsonNode json, String name) throws IOException {
		JsonNode node = asObject(json, name);
		return new Ros2Types.Orientation(getFloatField(node, name, "x"),
										getFloatField(node, name, "y"),
										getFloatField(node, name, "z"),
										getFloatField(node, name, "w"));
	}
	private static Ros2Types.Orientation parseOrientation(ElementValue smev, String name) throws IOException {
		if ( smev instanceof ElementCollectionValue smcv ) {
			Float x = getFloatField(smcv.getField("x"), "Orientation.x");
			Float y = getFloatField(smcv.getField("y"), "Orientation.y");
			Float z = getFloatField(smcv.getField("z"), "Orientation.z");
			Float w = getFloatField(smcv.getField("w"), "Orientation.w");
			
			if ( x == null || y == null || z == null || w == null ) {
				throw new IOException(String.format("invalid %s (some fields are null): %s", name, smev));
			}
			return new Ros2Types.Orientation(x, y, z, w);
		}
		
		throw new IOException(String.format("invalid %s (not ElementCollectionValue): %s", name, smev));
	}

	/**
	 * 관절 이름을 키로 하는 JSON 객체를 관절 이름 → 값의 맵으로 변환한다.
	 * <p>
	 * 로봇 트윈은 {@code NaN}/{@code Infinity}를 {@code null}로 변환하여 보내기 때문에,
	 * 값이 {@code null}인 관절은 사용할 수 없는 값으로 보고 결과에서 제외한다.
	 *
	 * @param node	변환할 JSON 객체.
	 * @param name	오류 메시지에 사용할 {@code node}의 이름.
	 * @return	관절 이름 → 값. (JSON에 기록된 순서를 유지한다)
	 * @throws IOException	{@code null}도 숫자도 아닌 값을 갖는 관절이 있는 경우.
	 */
	private static Map<String,Float> parseJointValues(JsonNode node, String name) throws IOException {
		Map<String,Float> values = new LinkedHashMap<>();
		for ( Map.Entry<String,JsonNode> joint: node.properties() ) {
			JsonNode value = joint.getValue();
			if ( value.isNull() ) {
				continue;
			}
			if ( !value.isNumber() ) {
				throw new IOException(String.format("invalid %s.%s (not a number): %s",
													name, joint.getKey(), value));
			}
			values.put(joint.getKey(), (float)value.asDouble());
		}

		return Collections.unmodifiableMap(values);
	}

	/**
	 * 생략 가능한 관절 값 필드를 읽는다.
	 *
	 * @param node		대상 JSON 객체.
	 * @param name		오류 메시지에 사용할 {@code node}의 이름.
	 * @param fieldName	읽을 필드의 이름.
	 * @return	관절 이름 → 값. 해당 필드가 없거나 {@code null}이면 빈 맵.
	 * @throws IOException	해당 필드가 관절 이름 → 숫자 형태의 객체가 아닌 경우.
	 */
	private static Map<String,Float> parseOptionalJointValues(JsonNode node, String name,
																String fieldName) throws IOException {
		JsonNode field = node.get(fieldName);
		if ( field == null || field.isNull() ) {
			return Map.of();
		}

		String fullName = name + "." + fieldName;
		return parseJointValues(asObject(field, fullName), fullName);
	}

	private static ObjectNode toJsonNode(Map<String,Float> jointValues) {
		ObjectNode node = newObjectNode();
		for ( Map.Entry<String,Float> joint: jointValues.entrySet() ) {
			Float value = joint.getValue();
			putDouble(node, joint.getKey(), (value != null) ? value : Float.NaN);
		}

		return node;
	}

	private static ObjectNode toJsonNode(Ros2Types.Header header) {
		ObjectNode stampNode = newObjectNode();
		stampNode.put("sec", Math.floorDiv(header.stamp(), NANOS_PER_SEC));
		stampNode.put("nanosec", Math.floorMod(header.stamp(), NANOS_PER_SEC));

		ObjectNode node = newObjectNode();
		node.set("stamp", stampNode);
		node.put("frame_id", header.frame_id());

		return node;
	}

	private static ObjectNode toJsonNode(Ros2Types.Pose pose) {
		ObjectNode node = newObjectNode();
		node.set("position", toJsonNode(pose.position()));
		node.set("orientation", toJsonNode(pose.orientation()));

		return node;
	}
	private static ElementCollectionValue toElementValue(Ros2Types.Pose pos) {
		LinkedHashMap<String,ElementValue> map = new LinkedHashMap<>();
		map.put("Position", toElementValue(pos.position()));
		map.put("Orientation", toElementValue(pos.orientation()));
		
		return new ElementCollectionValue(map);
	}

	private static ObjectNode toJsonNode(Ros2Types.Position pos) {
		ObjectNode node = newObjectNode();
		putFloat(node, "x", pos.x());
		putFloat(node, "y", pos.y());
		putFloat(node, "z", pos.z());

		return node;
	}
	private static ElementCollectionValue toElementValue(Ros2Types.Position pos) {
		LinkedHashMap<String,ElementValue> map = new LinkedHashMap<>();
		map.put("x", PropertyValue.FLOAT(pos.x()));
		map.put("y", PropertyValue.FLOAT(pos.y()));
		map.put("z", PropertyValue.FLOAT(pos.z()));
		
		return new ElementCollectionValue(map);
	}

	private static ObjectNode toJsonNode(Ros2Types.Orientation ort) {
		ObjectNode node = newObjectNode();
		putFloat(node, "x", ort.x());
		putFloat(node, "y", ort.y());
		putFloat(node, "z", ort.z());
		putFloat(node, "w", ort.w());

		return node;
	}
	private static ElementCollectionValue toElementValue(Ros2Types.Orientation ort) {
		LinkedHashMap<String,ElementValue> map = new LinkedHashMap<>();
		map.put("x", PropertyValue.FLOAT(ort.x()));
		map.put("y", PropertyValue.FLOAT(ort.y()));
		map.put("z", PropertyValue.FLOAT(ort.z()));
		map.put("w", PropertyValue.FLOAT(ort.w()));
		
		return new ElementCollectionValue(map);
	}

	private static ObjectNode newObjectNode() {
		return MDTModelSerDe.getJsonMapper().createObjectNode();
	}

	/**
	 * 주어진 JSON이 객체인지 확인한다.
	 *
	 * @param json	확인할 JSON.
	 * @param name	오류 메시지에 사용할 메시지 이름.
	 * @return	{@code json} 자신.
	 * @throws IOException	{@code json}이 객체가 아닌 경우.
	 */
	private static JsonNode asObject(JsonNode json, String name) throws IOException {
		if ( json == null || !json.isObject() ) {
			throw new IOException(String.format("invalid %s (not object): %s", name, json));
		}
		return json;
	}

	/**
	 * 주어진 JSON 객체에서 객체 필드를 읽는다.
	 *
	 * @param node		대상 JSON 객체.
	 * @param name		오류 메시지에 사용할 {@code node}의 이름.
	 * @param fieldName	읽을 필드의 이름.
	 * @return	필드의 값.
	 * @throws IOException	해당 필드가 없거나 객체가 아닌 경우.
	 */
	private static JsonNode getObjectField(JsonNode node, String name, String fieldName)
		throws IOException {
		return asObject(node.get(fieldName), name + "." + fieldName);
	}
	
	private static String getStringField(ElementCollectionValue smev, String name, String fieldName)
		throws IOException {
		ElementValue field = smev.getField(fieldName);
		if ( field == null ) {
			return null;
		}
		
		if ( field instanceof StringPropertyValue pv ) {
			return pv.toValueObject();
		}
		throw new IOException(String.format("invalid %s.%s (not a Long): %s", name, fieldName, field));
	}

	/**
	 * 주어진 JSON 객체에서 정수 필드의 값을 읽는다.
	 *
	 * @param node		대상 JSON 객체.
	 * @param name		오류 메시지에 사용할 {@code node}의 이름.
	 * @param fieldName	읽을 필드의 이름.
	 * @return	필드의 값.
	 * @throws IOException	해당 필드가 없거나 숫자가 아닌 경우.
	 */
	private static long getLongField(JsonNode node, String name, String fieldName) throws IOException {
		JsonNode field = getRequiredField(node, name, fieldName);
		if ( !field.isNumber() ) {
			throw new IOException(String.format("invalid %s.%s (not a number): %s",
												name, fieldName, field));
		}
		return field.asLong();
	}
	private static Long getRequiredLongField(ElementCollectionValue smev, String name, String fieldName) throws IOException {
		ElementValue field = getRequiredField(smev, name, fieldName);
		if ( field instanceof LongPropertyValue lpv ) {
			return lpv.toValueObject();
		}
		throw new IOException(String.format("invalid %s.%s (not a Long): %s", name, fieldName, field));
	}
	
	private static float getFloatField(JsonNode node, String name, String fieldName) throws IOException {
		JsonNode field = getRequiredField(node, name, fieldName);
		if ( field.isNull() ) {
			return Float.NaN;
		}
		if ( !field.isNumber() ) {
			throw new IOException(String.format("invalid %s.%s (not a number): %s",
												name, fieldName, field));
		}
		return (float)field.asDouble();
	}

	/**
	 * 주어진 JSON 객체에서 실수 필드의 값을 읽는다.
	 * <p>
	 * 로봇 트윈은 {@code NaN}/{@code Infinity}를 {@code null}로 변환하여 보내기 때문에
	 * {@code null} 값은 {@link Double#NaN}으로 읽는다.
	 *
	 * @param node		대상 JSON 객체.
	 * @param name		오류 메시지에 사용할 {@code node}의 이름.
	 * @param fieldName	읽을 필드의 이름.
	 * @return	필드의 값. 필드 값이 {@code null}인 경우는 {@link Double#NaN}.
	 * @throws IOException	해당 필드가 없거나, {@code null}도 숫자도 아닌 경우.
	 */
	private static double getDoubleField(JsonNode node, String name, String fieldName)
		throws IOException {
		JsonNode field = getRequiredField(node, name, fieldName);
		if ( field.isNull() ) {
			return Double.NaN;
		}
		if ( !field.isNumber() ) {
			throw new IOException(String.format("invalid %s.%s (not a number): %s",
												name, fieldName, field));
		}
		return field.asDouble();
	}

	private static JsonNode getRequiredField(JsonNode node, String name, String fieldName)
		throws IOException {
		JsonNode field = node.get(fieldName);
		if ( field == null ) {
			throw new IOException(String.format("missing %s.%s", name, fieldName));
		}
		return field;
	}
	private static ElementValue getRequiredField(ElementCollectionValue ecv, String name, String fieldName)
		throws IOException {
		ElementValue field = ecv.getField(fieldName);
		if ( field == null ) {
			throw new IOException(String.format("missing %s.%s", name, fieldName));
		}
		return field;
	}
	
	private static void putFloat(ObjectNode node, String fieldName, float value) {
		if ( Float.isFinite(value) ) {
			node.put(fieldName, value);
		}
		else {
			node.putNull(fieldName);
		}
	}

	/**
	 * 주어진 JSON 객체에 실수 필드를 기록한다.
	 * <p>
	 * 로봇 트윈의 표현 방식에 맞춰 유한하지 않은 값({@code NaN}/{@code Infinity})은
	 * {@code null}로 기록한다.
	 *
	 * @param node		대상 JSON 객체.
	 * @param fieldName	기록할 필드의 이름.
	 * @param value		기록할 값.
	 */
	private static void putDouble(ObjectNode node, String fieldName, double value) {
		if ( Double.isFinite(value) ) {
			node.put(fieldName, value);
		}
		else {
			node.putNull(fieldName);
		}
	}

	private static Float getFloatField(ElementValue smev, String fieldName) throws IOException {
		if ( smev instanceof FloatPropertyValue dpv ) {
			return dpv.toValueObject();
		}
		
		throw new IOException(String.format("invalid %s (not FloatPropertyValue): %s", fieldName, smev));
	}

	private static Double getDoubleField(ElementValue smev, String fieldName) throws IOException {
		if ( smev instanceof DoublePropertyValue dpv ) {
			return dpv.toValueObject();
		}
		
		throw new IOException(String.format("invalid %s (not DoublePropertyValue): %s", fieldName, smev));
	}
}
