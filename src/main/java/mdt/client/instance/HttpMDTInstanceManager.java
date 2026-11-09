package mdt.client.instance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import utils.Preconditions;
import utils.http.HttpRESTfulClient;
import utils.http.JacksonErrorEntityDeserializer;
import utils.http.RESTfulErrorEntity;
import utils.http.RESTfulRemoteException;
import utils.io.ZipFile;
import utils.stream.FStream;
import utils.thread.Guard;

import mdt.client.MDTRESTfulClients;
import mdt.client.HttpMDTManager;
import mdt.client.HttpMDTServiceProxy;
import mdt.client.MDTManagerMqttMessage;
import mdt.model.InvalidResourceStatusException;
import mdt.model.MDTModelSerDe;
import mdt.model.ResourceAlreadyExistsException;
import mdt.model.ResourceNotFoundException;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceStatusChangeEvent;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.sm.ref.DefaultElementReference;


/**
 * {@code HttpMDTInstanceManager}는 HTTP REST API를 통해 원격 MDT 매니저의
 * {@link MDTInstanceManager} 기능을 사용하기 위한 클라이언트 구현이다.
 * <p>
 * 객체는 {@link #builder()}로 생성하며, 생성 시 {@link HttpMDTManager#register(Object)}를 통해
 * MDT 매니저의 MQTT 메시지 이벤트 버스에 구독자로 등록된다.
 * 각 MDTInstance 조회/제어 요청은 매니저의 {@code /instances} 엔드포인트로 전달되고,
 * 그 결과는 {@link HttpMDTInstanceClient} 객체로 반환된다.
 * <p>
 * 서버가 반환한 오류는 {@link MDTRESTfulClients}에 등록된 MDT 예외
 * (예: {@link ResourceNotFoundException})로 복원되어 던져진다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceManager implements MDTInstanceManager, HttpMDTServiceProxy {
	private static final JsonMapper MAPPER = MDTModelSerDe.MAPPER;
	private static final String TOPIC_PREFIX = "/mdt/manager/instances/";
	private static final int TOPIC_PREFIX_LENGTH = TOPIC_PREFIX.length();
	/**
	 * MDTInstance 상태 변경 이벤트를 전파하기 위한 이벤트 버스.
	 * <p>
	 * {@link MDTInstanceStatusSubscriber}가 수신한 MQTT 상태 변경 이벤트를 이 버스에 게시한다.
	 */
	public static EventBus EVENT_BUS = new EventBus();
	
	private final String m_endpoint;
	private final HttpRESTfulClient m_restfulClient;
	
	private final Guard m_guard = Guard.create();
	@GuardedBy("m_guard") private final Map<String,HttpMDTInstanceClient> m_instances = Maps.newHashMap();
	
	private HttpMDTInstanceManager(Builder builder) {
		m_endpoint = builder.m_endpoint;
		m_restfulClient = MDTRESTfulClients.builder()
										.httpClient(builder.m_httpClient)
										.jsonMapper(MDTModelSerDe.getJsonMapper())
										.errorEntityDeserializer(new JacksonErrorEntityDeserializer(MDTModelSerDe.MAPPER))
										.build();
		
		HttpMDTManager.register(this);
	}
	
	/**
	 * MDT 매니저로부터 수신된 MQTT 메시지를 처리한다.
	 * <p>
	 * 토픽이 {@code /mdt/manager/instances/<id>} 형태이고, 해당 식별자의 MDTInstance가
	 * 본 매니저를 통해 추가된 것인 경우에만 메시지를 {@link InstanceStatusChangeEvent}로 파싱한다.
	 * 현재는 파싱된 이벤트에 대해 별도 처리를 하지 않으며, 처리 중 발생한 예외는 모두 무시된다.
	 *
	 * @param msg	수신된 MQTT 메시지.
	 */
	@Subscribe
	public void instanceStatusChanged(MDTManagerMqttMessage msg) {
		String topic = msg.getTopic();
		if ( !topic.startsWith(TOPIC_PREFIX) ) {
			return;
		}
		
		String instId = topic.substring(TOPIC_PREFIX_LENGTH);
		m_guard.lock();
		try {
			HttpMDTInstanceClient inst = m_instances.get(instId);
			if ( inst == null ) {
				return;
			}

			String payload = new String(msg.getMessage().getPayload(), StandardCharsets.UTF_8);
			InstanceStatusChangeEvent event = MAPPER.readValue(payload, InstanceStatusChangeEvent.class);
			switch ( event.getInstanceStatus() ) {
				case RUNNING:
				case STOPPED:
					// 일단 아무런 처리를 하지 않음.
					break;
				default:
					break;
			}
		}
		catch ( Throwable ignored ) { }
		finally {
			m_guard.unlock();
		}
	}

	@Override
	public OkHttpClient getHttpClient() {
		return m_restfulClient.getHttpClient();
	}

	@Override
	public String getEndpoint() {
		return m_endpoint;
	}

	/**
	 * 주어진 식별자의 MDTInstance에 대한 {@link InstanceDescriptor}를 원격 매니저로부터 조회한다.
	 * <p>
	 * 호출할 때마다 원격 매니저에 GET 요청을 보낸다.
	 *
	 * @param id	조회할 MDTInstance의 식별자.
	 * @return	조회된 {@link InstanceDescriptor}.
	 * @throws ResourceNotFoundException	주어진 식별자의 MDTInstance가 존재하지 않는 경우.
	 */
	public InstanceDescriptor getInstanceDescriptor(String id) {
		String url = String.format("%s/instances/%s", getEndpoint(), id);
		return m_restfulClient.get(url, MDTModelSerDes.INSTANCE_DESC_RESP);
	}

	@Override
	public HttpMDTInstanceClient getInstance(String id) throws MDTInstanceManagerException {
		InstanceDescriptor desc = getInstanceDescriptor(id);
		return new HttpMDTInstanceClient(this, desc);
	}

	@Override
	public List<MDTInstance> getInstanceAll() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		return toInstances(m_restfulClient.get(url, MDTModelSerDes.INSTANCE_DESC_LIST));
	}

	@Override
	public List<MDTInstance> getInstanceAllByFilter(String filter) {
		String url = String.format("%s/instances", getEndpoint());
		HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
						 		.addQueryParameter("filter", filter)
					 			.build();
		return toInstances(m_restfulClient.get(httpUrl, MDTModelSerDes.INSTANCE_DESC_LIST));
	}
	
	@Override
	public HttpMDTInstanceClient getInstanceByAasId(String aasId) throws ResourceNotFoundException {
		String filter = "instance.aasId = " + MDTInstanceManager.toFilterLiteral(aasId);
		List<MDTInstance> instList = getInstanceAllByFilter(filter);
		if ( instList.size() == 0 ) {
			throw new ResourceNotFoundException("MDTInstance", "aasId=" + aasId);
		}
		else {
			return (HttpMDTInstanceClient)instList.get(0);
		}
	}
	
	@Override
	public List<MDTInstance> getInstanceAllByAasIdShort(String aasIdShort) {
		String filter = "instance.aasIdShort = " + MDTInstanceManager.toFilterLiteral(aasIdShort);
		return getInstanceAllByFilter(filter);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 원격 매니저의 {@code GET /instances/$count}를 부른다. 2026-11-05 이전에는 서버에 없는
	 * {@code GET /instances?aggregate=count}를 불러, 서버가 돌려준 목록 JSON을 숫자로 읽다 실패했다.
	 */
	@Override
	public long countInstances() {
		String url = String.format("%s/instances/$count", getEndpoint());
		String respBody = m_restfulClient.get(url);
		return Long.parseLong(respBody);
	}
	
	private static final MediaType ZIP_TYPE = MediaType.parse("application/zip");

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@code instanceFile}이 디렉토리인 경우에는 그 부모 디렉토리에 {@code <id>.zip} 파일로 임시 압축하여
	 * 업로드하고, 업로드 후 임시 zip 파일을 삭제한다. 디렉토리가 아닌 경우에는 확장자가 {@code zip}이어야 한다.
	 * {@code port}가 0 이하이면 포트 정보를 전송하지 않는다.
	 * 서버가 오류를 반환한 경우에는 {@link MDTInstanceManagerException}으로 감싸서 던진다.
	 *
	 * @throws ResourceAlreadyExistsException	원격 매니저에 동일 식별자의 MDTInstance가 이미 등록된 경우.
	 */
	@Override
	public HttpMDTInstanceClient addInstance(String id, int port, File instanceFile) throws MDTInstanceManagerException {
		Preconditions.checkNotNullArgument(id, "MDTInstance id must not be null");
		Preconditions.checkNotNullArgument(instanceFile, "MDTInstance file must not be null");
		
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id);
		
		if ( port > 0 ) {
			builder = builder.addFormDataPart("port", ""+port);
		}
		
		File zippedFile = instanceFile;
		try {
			if ( instanceFile.isDirectory() ) {
				try {
					// Instance 파일이 디렉토리인 경우, zip 파일로 압축한다.
					zippedFile = new File(instanceFile.getParentFile(), id + ".zip");
					ZipFile.zipDirectory(zippedFile.toPath(), instanceFile.toPath());
				}
				catch ( IOException e ) {
					throw new MDTInstanceManagerException("Failed to add an instance: dir=" + instanceFile, e);
				}
			}
			else if ( !Files.getFileExtension(zippedFile.getAbsolutePath()).equals("zip") ) {
				throw new MDTInstanceManagerException("MDTInstance file must be a zip file: " + zippedFile);
			}
			
			builder.addFormDataPart("bundle", id + ".zip", RequestBody.create(zippedFile, ZIP_TYPE));

			String url = String.format("%s/instances", getEndpoint());
			RequestBody reqBody = builder.build();
			InstanceDescriptor desc = m_restfulClient.post(url, reqBody, MDTModelSerDes.INSTANCE_DESC_RESP);
			
			return createInstance(desc);
		}
		catch ( RESTfulRemoteException e ) {
			RESTfulErrorEntity errorEntity = e.getRemoteErrorEntity();
			
			throw new MDTInstanceManagerException("Failed to add an instance: id=" + id, e);
		}
		finally {
			if ( zippedFile != instanceFile ) {
				// 임시로 생성한 zip 파일을 삭제한다.
				// (디렉토리인 경우에만 zip 파일로 압축하고, 압축한 후에는 삭제한다.)
				zippedFile.delete();
			}
		}
	}
	
	/**
	 * 이미 zip 파일로 압축된 MDTInstance 번들을 업로드하여 새 MDTInstance를 추가한다.
	 *
	 * @param id	추가할 MDTInstance의 식별자.
	 * @param port	MDTInstance에 할당할 포트 번호. 값에 관계없이 항상 전송된다.
	 * @param zippedInstanceDir	MDTInstance 디렉토리를 압축한 zip 파일.
	 * @return	추가된 MDTInstance에 대한 클라이언트 객체.
	 * @throws IllegalArgumentException	{@code id} 또는 {@code zippedInstanceDir}가 {@code null}이거나,
	 * 									파일 확장자가 {@code zip}이 아닌 경우.
	 * @throws ResourceAlreadyExistsException	원격 매니저에 동일 식별자의 MDTInstance가 이미 등록된 경우.
	 */
	public HttpMDTInstanceClient addZippedInstance(String id, int port, File zippedInstanceDir) {
		Preconditions.checkNotNullArgument(id, "MDTInstance id must not be null");
		Preconditions.checkNotNullArgument(zippedInstanceDir, "Zipped MDTInstance file must not be null");
		Preconditions.checkArgument(Files.getFileExtension(zippedInstanceDir.getAbsolutePath()).equals("zip"),
									"Zipped MDTInstance file must be a zip file: " + zippedInstanceDir);
		
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id)
											.addFormDataPart("port", ""+port);
		
		// 주어진 디렉토리를 zip 파일로 만들어서 upload 시킨다.
		builder.addFormDataPart("bundle", id + ".zip", RequestBody.create(zippedInstanceDir, ZIP_TYPE));

		String url = String.format("%s/instances", getEndpoint());
		RequestBody reqBody = builder.build();
		InstanceDescriptor desc = m_restfulClient.post(url, reqBody, MDTModelSerDes.INSTANCE_DESC_RESP);
		
		return createInstance(desc);
	}
	
	private HttpMDTInstanceClient createInstance(InstanceDescriptor desc) {
		// 중복 식별자는 원격 매니저가 거부하므로, 여기까지 왔으면 서버에 새로 등록된 것이다.
		// 이전에 같은 식별자로 추가했다가 다른 경로로 제거된 항목이 남아 있을 수 있으므로 덮어쓴다.
		return m_guard.get(() -> {
			HttpMDTInstanceClient inst = new HttpMDTInstanceClient(this, desc);
			m_instances.put(desc.getId(), inst);
			return inst;
		});
	}

    // @DeleteMapping("/instances/{id}")
	@Override
	public void removeInstance(String id) throws MDTInstanceManagerException {
		String url = String.format("%s/instances/%s", getEndpoint(), id);
		
		m_restfulClient.delete(url);
		m_guard.run(() -> m_instances.remove(id));
	}

	/**
	 * 주어진 MDTInstance를 원격 매니저에서 제거한다.
	 * <p>
	 * {@code removeInstance(inst.getId())}와 동일하다.
	 *
	 * @param inst	제거할 MDTInstance.
	 * @throws MDTInstanceManagerException	MDTInstance 제거에 실패한 경우.
	 */
	public void removeInstance(MDTInstance inst) throws MDTInstanceManagerException {
		removeInstance(inst.getId());
	}

    // @DeleteMapping("/instances")
	@Override
	public void removeInstanceAll() throws MDTInstanceManagerException {
		String url = String.format("%s/instances", getEndpoint());
		
		m_restfulClient.delete(url);
		m_guard.run(m_instances::clear);
	}

	/**
	 * 모델 {@link Reference}에 해당하는 {@link SubmodelElement} 객체를 반환한다.
	 * 
	 * <p>
	 * 주어진 Reference를 {@link DefaultElementReference}로 변환하고 본 매니저로 활성화한 뒤
	 * 해당 SubmodelElement를 읽어 반환한다.
	 *
	 * @param ref	접근하고자 하는 Reference 객체.
	 * @return	{@link SubmodelElement} 객체.
	 * @throws ResourceNotFoundException	Reference에 해당하는 SubmodelElement가 존재하지 않는 경우.
	 * @throws InvalidResourceStatusException	Reference에 해당하는 SubmodelElement를 포함한
	 * 					MDTInstance가 RUNNING 상태가 아닌 경우.
	 * @throws IOException	SubmodelElement 읽기 중 입출력 오류가 발생한 경우.
	 */
	public SubmodelElement getSubmodelElementByReference(Reference ref)
		throws ResourceNotFoundException, InvalidResourceStatusException, IOException {
		DefaultElementReference elmRef = DefaultElementReference.newInstance(ref);
		elmRef.activate(this);
		return elmRef.read();
	}

	/**
	 * 주어진 참조 문자열을 해당 요소에 접근할 수 있는 URL로 변환한다.
	 * <p>
	 * 변환은 원격 매니저의 {@code /references/$url} 엔드포인트에 요청하여 수행된다.
	 *
	 * @param ref	변환할 참조 문자열.
	 * @return	참조 대상 요소의 URL.
	 */
	public String resolveReferenceToUrl(String ref) {
		String url = String.format("%s/references/$url", getEndpoint());
		HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
						 		.addQueryParameter("ref", ref)
					 			.build();
		return m_restfulClient.get(httpUrl, HttpRESTfulClient.STRING_DESER);
	}
	
	@Override
	public String toString() {
		return "HttpMDTInstanceManager[" + m_endpoint + "]";
	}

	/**
	 * {@link HttpMDTInstanceManager} 객체를 생성하기 위한 빌더를 반환한다.
	 *
	 * @return	새 {@link Builder} 객체.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * {@link HttpMDTInstanceManager} 객체 생성을 위한 빌더.
	 * <p>
	 * {@link #endpoint(String)}와 {@link #httpClient(OkHttpClient)}는 반드시 설정되어야 한다.
	 */
	public static class Builder {
		private OkHttpClient m_httpClient;
		private String m_endpoint;
		
		private Builder() { }
		
		/**
		 * 설정된 값으로 {@link HttpMDTInstanceManager} 객체를 생성한다.
		 *
		 * @return	생성된 {@link HttpMDTInstanceManager} 객체.
		 * @throws IllegalStateException	endpoint 또는 HTTP 클라이언트가 설정되지 않은 경우.
		 */
		public HttpMDTInstanceManager build() {
			Preconditions.checkState(m_endpoint != null, "MDTInstanceManager endpoint has not been set");
			Preconditions.checkState(m_httpClient != null, "HTTP client has not been set");
			
			return new HttpMDTInstanceManager(this);
		}
		
		/**
		 * 원격 매니저 접근에 사용할 HTTP 클라이언트를 설정한다.
		 *
		 * @param client	HTTP 클라이언트.
		 * @return	본 빌더 객체.
		 */
		public Builder httpClient(OkHttpClient client) {
			m_httpClient = client;
			return this;
		}
		
		/**
		 * 원격 MDT 매니저의 endpoint URL을 설정한다.
		 *
		 * @param endpoint	MDT 매니저 endpoint URL.
		 * @return	본 빌더 객체.
		 */
		public Builder endpoint(String endpoint) {
			m_endpoint = endpoint;
			return this;
		}
	}
	
	// 변경할 수 없는 목록을 돌려준다(Stream.toList).
	private List<MDTInstance> toInstances(List<InstanceDescriptor> descList) {
		return descList.stream()
						.<MDTInstance>map(desc -> new HttpMDTInstanceClient(HttpMDTInstanceManager.this, desc))
						.toList();
	}
}
