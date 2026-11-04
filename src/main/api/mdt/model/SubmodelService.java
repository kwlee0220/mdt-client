package mdt.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.Preconditions;
import utils.http.RESTfulRemoteException;
import utils.io.TempFile;
import utils.stream.FStream;

import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.FileValue;


/**
 * Submodel 객체를 다루는 기능을 제공하는 서비스를 정의한 인터페이스.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelService {
	/**
	 * Submodel 또는 SubmodelElement 조회 시 반환할 내용의 범위를 지정하는 modifier.
	 * <p>
	 * AAS API의 content modifier({@code $metadata}, {@code $value})에 대응한다.
	 */
	public static enum Modifier {
		/** Modifier를 적용하지 않고 전체 객체를 조회한다. */
		NONE,
		/** 값을 제외한 메타데이터만 조회한다 ({@code $metadata}). */
		METADATA,
		/** 값만 조회한다 ({@code $value}). 구현에 따라 지원되지 않을 수 있다. */
		VALUE
	};
	
	/**
	 * 주어진 modifier를 적용하여 본 서비스가 제공하는 {@link Submodel} 객체를 반환한다.
	 *
	 * @param modifier	조회시 적용할 {@link Modifier}.
	 * @return	{@link Submodel} 객체.
	 */
	public Submodel getSubmodel(Modifier modifier);
	
	/**
	 * 본 서비스를 통해 제공하는 {@link Submodel} 객체를 반환한다.
	 * <p>
	 * {@code getSubmodel(Modifier.NONE)}과 동일하다.
	 *
	 * @return	{@link Submodel} 객체
	 */
	public default Submodel getSubmodel() {
		return getSubmodel(Modifier.NONE);
	}
	
	/**
	 * 본 서비스를 통해 제공하는 {@link Submodel} 객체를 주어진 것으로 대체한다.
	 *
	 * @param submodel {@link Submodel} 객체
	 * @return 설정된 {@link Submodel} 객체
	 */
	public Submodel putSubmodel(Submodel submodel);
	
	/**
	 * 본 서비스를 통해 제공하는 Submodel 객체에 포함된 모든 SubmodelElement 리스트를 반환한다.
	 *
	 * @return	{@link SubmodelElement} 객체 리스트
	 */
	public List<SubmodelElement> getAllSubmodelElements();
	
	/**
	 * 주어진 idShortPath에 해당하는 SubmodelElement 객체를 반환한다.
	 *
	 * @param idShortPath    SubmodelElement 객체의 idShortPath.
	 * @param modifier       SubmodelElement 조회시 적용할 Modifier.
	 * @return {@link SubmodelElement} 객체
	 * @throws ResourceNotFoundException 해당 idShortPath에 해당하는 SubmodelElement 객체가 존재하지
	 *                                   않는 경우
	 */
	public SubmodelElement getSubmodelElementByPath(String idShortPath, Modifier modifier)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 idShortPath에 해당하는 SubmodelElement 객체를 반환한다.
	 * <p>
	 * {@code getSubmodelElementByPath(idShortPath, Modifier.NONE)}과 동일하다.
	 *
	 * @param idShortPath	SubmodelElement 객체의 idShortPath.
	 * @return {@link SubmodelElement} 객체.
	 * @throws ResourceNotFoundException	해당 idShortPath에 해당하는 SubmodelElement 객체가 존재하지 않는 경우.
	 */
	public default SubmodelElement getSubmodelElementByPath(String idShortPath) throws ResourceNotFoundException {
		return getSubmodelElementByPath(idShortPath, Modifier.NONE);
	}
	
	/**
	 * 주어진 idShortPath에 해당하는 SubmodelElement의 값({@link ElementValue})을 반환한다.
	 * <p>
	 * {@code prototype}은 반환된 값을 해석하는 데 사용할 SubmodelElement의 원형(구조)이다.
	 *
	 * @param idShortPath	SubmodelElement 객체의 idShortPath.
	 * @param prototype	값 해석에 사용할 원형 SubmodelElement.
	 * @return	SubmodelElement의 값.
	 * @throws ResourceNotFoundException	해당 idShortPath에 해당하는 SubmodelElement 객체가 존재하지 않는 경우.
	 */
	public ElementValue getSubmodelElementValueByPath(String idShortPath, SubmodelElement prototype)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 SubmodelElement를 Submodel 객체 하위로 새로 추가한다.
	 * <p>
	 * 추가할 SubmodelElement 객체는 'idShort' 속성이 반드시 설정되어 있어야 한다.
	 *
	 * @param element 추가할 SubmodelElement 객체
	 * @return 추가된 SubmodelElement 객체
	 */
	public SubmodelElement addSubmodelElement(SubmodelElement element);
	
	/**
	 * 주어진 SubmodelElement 객체를 주어진 idShortPath 위치에 새로 추가한다.
	 * <p>
	 * 추가할 SubmodelElement 객체는 'idShort' 속성이 반드시 설정되어 있어야 한다.
	 *
	 * @param idShortPath 추가될 위치(부모 SubmodelElement)의 idShortPath.
	 * @param element     추가할 SubmodelElement 객체
	 * @return 추가된 SubmodelElement 객체
	 */
	public SubmodelElement addSubmodelElementByPath(String idShortPath, SubmodelElement element);
	
	/**
	 * 주어진 idShortPath에 해당하는 SubmodelElement 객체를 주어진 element로 대체시킨다.
	 * 
	 * @param idShortPath Submodel내의 대체 대상 idShortPath.
	 * @param element     새 SubmodelElement 객체.
	 * @return 변경된 SubmodelElement 객체.
	 * @throws ResourceNotFoundException	해당 idShortPath에 해당하는 SubmodelElement 객체가 존재하지 않는 경우
	 */
	public SubmodelElement setSubmodelElementByPath(String idShortPath, SubmodelElement element);
	
	/**
	 * 주어진 idShortPath에 해당하는 SubmodelElement 객체의 값을
	 * 주어진 element의 값으로 변경한다.
	 * 
	 * @param idShortPath	Submodel내의 변경 대상 idShortPath.
	 * @param element	    변경할 값을 가진 SubmodelElement 객체.
	 * @throws ResourceNotFoundException	해당 idShortPath에 해당하는 SubmodelElement 객체가 존재하지 않는 경우.
	 */
	public void updateSubmodelElementByPath(String idShortPath, SubmodelElement element)
		throws ResourceNotFoundException;

	/**
	 * 주어진 value json 문자열을 이용하여 idShortPath에 해당하는 SubmodelElement 객체의 값을 변경한다.
	 *
	 * @param idShortPath		Submodel내의 변경 대상 idShortPath.
	 * @param valueJsonString	변경할 값의 value-only json 문자열.
	 * @throws ResourceNotFoundException	해당 idShortPath에 해당하는 SubmodelElement 객체가 존재하지 않는 경우.
	 */
	public void updateSubmodelElementValueByPath(String idShortPath, String valueJsonString)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 idShortPath에 해당하는 SubmodelElement 객체를 삭제한다.
	 *
	 * @param idShortPath 삭제할 SubmodelElement
	 * @throws ResourceNotFoundException 해당 idShortPath에 해당하는 SubmodelElement 객체가
	 *                                   존재하지 않는 경우
	 */
	public void deleteSubmodelElementByPath(String idShortPath) throws ResourceNotFoundException;
	
	/**
	 * 주어진 idShortPath에 해당하는 File SubmodelElement 객체에 해당하는 파일의 내용을
	 * 주어진 출력 스트림에 기록한다.
	 *
	 * @param idShortPath File SubmodelElement 객체의 idShortPath.
	 * @param output  		파일 내용을 저장할 OutputStream 객체.
	 * @throws ResourceNotFoundException 해당 idShortPath에 해당하는 SubmodelElement 객체가
	 *                                   존재하지 않는 경우.
	 * @throws IOException	파일 입출력 오류 발생한 경우.
	 */
	public void readAttachmentByPath(String idShortPath, OutputStream output)
		throws ResourceNotFoundException, IOException;
	
	/**
	 * 주어진 idShortPath에 해당하는 File SubmodelElement 객체에 해당하는 파일 내용을
	 * 임시 파일로 생성하여 반환한다.
	 *
	 * @param idShortPath	File SubmodelElement 객체의 idShortPath.
	 * @return	파일 내용이 저장된 임시 파일 객체.
	 * @throws ResourceNotFoundException	해당 idShortPath에 해당하는 SubmodelElement 객체가
	 * 								 존재하지 않는 경우.
	 * @throws IOException	파일 입출력 오류 발생한 경우.
	 */
	public default TempFile readAttachmentToTempFile(String idShortPath) throws ResourceNotFoundException,
																		IOException {
		TempFile tempFile = TempFile.create();
		readAttachmentByPath(idShortPath, tempFile.getOutputStream());
		
		return tempFile;
	}
	
	/**
	 * 주어진 idShortPath에 해당하는 File SubmodelElement 객체에 해당하는 파일의 내용을 변경한다.
	 *
	 * @param idShortPath	File SubmodelElement 객체의 idShortPath.
	 * @param aasFile     	대상 File SubmodelElement의 값 (파일 경로 및 content type).
	 * @param attachment	새 파일 내용을 읽을 입력 스트림.
	 * @return	변경된 파일 크기.
	 * @throws ResourceNotFoundException 해당 idShortPath에 해당하는 SubmodelElement 객체가
	 *                                   존재하지 않는 경우.
	 */
	public long putAttachmentByPath(String idShortPath, FileValue aasFile, InputStream attachment)
		throws ResourceNotFoundException;
	
	/**
	 * 주어진 idShortPath에 해당하는 File SubmodelElement 객체에 해당하는 파일의 내용을
	 * 주어진 로컬 파일의 내용으로 변경한다.
	 * <p>
	 * {@code attachment} 파일을 열어 {@link #putAttachmentByPath(String, FileValue, InputStream)}을
	 * 호출한다.
	 *
	 * @param idShortPath 	File SubmodelElement 객체의 idShortPath.
	 * @param aasFile     	대상 File SubmodelElement의 값 (파일 경로 및 content type).
	 * @param attachment	새 파일 내용을 담은 로컬 파일.
	 * @return	변경된 파일 크기.
	 * @throws ResourceNotFoundException 해당 idShortPath에 해당하는 SubmodelElement 객체가
	 *                                   존재하지 않는 경우.
	 * @throws IOException	파일 입출력 오류 발생한 경우.
	 */
	public default long putAttachmentByPath(String idShortPath, FileValue aasFile, File attachment)
		throws ResourceNotFoundException, IOException {
		try ( InputStream is = java.nio.file.Files.newInputStream(attachment.toPath()) ) {
			return putAttachmentByPath(idShortPath, aasFile, is);
		}
	}
	
	/**
	 * 주어진 idShortPath에 해당하는 File SubmodelElement 객체에 해당하는 파일의 내용을 삭제한다.
	 *
	 * @param idShortPath File SubmodelElement 객체의 idShortPath.
	 * @throws ResourceNotFoundException 해당 idShortPath에 해당하는 SubmodelElement 객체가
	 *                                   존재하지 않는 경우.
	 */
	public void deleteAttachmentByPath(String idShortPath) throws ResourceNotFoundException;
	
	/**
	 * idShortPath에 해당하는 SubmodelElement 객체의 Operation을 동기적으로 실행한다.
	 *
	 * @param idShortPath		호출 대상 Operation SubmodelElement 객체의 idShortPath.
	 * @param inputArguments	입력 인자 값 목록
	 * @param inoutputArguments	입출력 인자 값 목록
	 * @param timeout			연산 실행 제한 시간
	 * @return		Operation 실행 결과. 실행 결과는 출력 인자 값 목록과 입출력 인자 값 목록으로 구성된다.
	 */
	public OperationResult invokeOperationSync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments,
												javax.xml.datatype.Duration timeout);
	
	/**
	 * idShortPath에 해당하는 SubmodelElement 객체의 Operation을 비동기적으로 실행한다.
	 * <p>
	 * 실행이 비동기적으로 수행되기 때문에, 즉시 반환되며, 실행 상태나 실행 결과는
	 * {@link #getOperationAsyncStatus(OperationHandle)}를 통해 확인할 수 있다.
	 *
	 * @param idShortPath		호출 대상 Operation SubmodelElement 객체의 idShortPath.
	 * @param inputArguments	입력 인자 값 목록
	 * @param inoutputArguments	입출력 인자 값 목록
	 * @param timeout			연산 실행 제한 시간
	 * @return        Operation 실행 핸들.
	 *				  이후 상태 및 결과 조회({@link #getOperationAsyncResult(OperationHandle)})에 사용된다.
	 */
	public OperationHandle invokeOperationAsync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments,
												javax.xml.datatype.Duration timeout);
	
	/**
	 * 주어진 OperationHandle에 해당하는 Operation의 실행 결과를 반환한다.
	 * <p>
	 * 인자로 사용하는 OperationHandle은
	 * {@link #invokeOperationAsync(String, List, List, javax.xml.datatype.Duration)} 호출 결과로
	 * 얻은 Operation 실행 핸들이다.
	 *
	 * @param handleId	Operation 실행 핸들.
	 * @return	Operation 실행 결과.
	 */
	public OperationResult getOperationAsyncResult(OperationHandle handleId);
	
	/**
	 * 주어진 OperationHandle에 해당하는 Operation의 실행 상태를 반환한다.
	 * <p>
	 * 인자로 사용하는 OperationHandle은
	 * {@link #invokeOperationAsync(String, List, List, javax.xml.datatype.Duration)}
	 * 호출 결과로 얻은 Operation 실행 핸들이다.
	 *
	 * @param handleId Operation 실행 핸들.
	 * @return Operation 실행 상태.
	 */
	public BaseOperationResult getOperationAsyncStatus(OperationHandle handleId);

	/**
	 * idShortPath에 해당하는 SubmodelElement 객체의 Operation을 동기적으로 실행한다.
	 * <p>
	 * 본 메소드는 {@link #invokeOperationAsync(String, List, List, javax.xml.datatype.Duration)}로
	 * 연산을 시작시키고, {@code pollInterval} 간격으로 연산 실행 상태를 polling하며 연산이
	 * 종료되면 그 결과를 반환한다. 연산이 완료될 때까지 호출 쓰레드는 대기한다.
	 *
	 * @param operationPath		호출 대상 Operation SubmodelElement 객체의 idShortPath.
	 * @param inputArguments	입력 인자 값 목록
	 * @param inoutputArguments	입출력 인자 값 목록
	 * @param timeout			연산 실행 제한 시간. {@code null}인 경우는 7일로 설정된다.
	 * @param pollInterval		연산 실행 상태 polling 간격.
	 * @return	Operation 실행 결과. 실행 결과는 출력 인자 값 목록과 입출력 인자 값 목록으로 구성된다.
	 * @throws IllegalArgumentException	{@code pollInterval}이 {@code null}인 경우.
	 * @throws InterruptedException	연산 실행 중 polling 중에 인터럽트가 발생한 경우
	 * @throws CancellationException	연산 상태가 {@code CANCELED}로 보고된 경우.
	 * @throws TimeoutException		연산 상태가 {@code TIMEOUT}으로 보고된 경우.
	 * @throws ExecutionException	연산 상태가 {@code FAILED}로 보고된 경우. 원인은 서버가 보낸 메시지들을 담은
	 * 								{@link RESTfulRemoteException}이다.
	 */
	public default OperationResult runOperation(String operationPath,
												List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments,
												Duration timeout, Duration pollInterval)
		throws InterruptedException, CancellationException, TimeoutException, ExecutionException {
		Preconditions.checkNotNullArgument(pollInterval, "pollInterval is null");
		
		javax.xml.datatype.Duration jtimeout = (timeout != null)
											? AASUtils.DATATYPE_FACTORY.newDuration(timeout.toMillis())
											: AASUtils.DATATYPE_FACTORY.newDuration("P7D");
		
		OperationHandle handle = invokeOperationAsync(operationPath, inputArguments, inoutputArguments, jtimeout);
		boolean finished = false;
		while ( !finished ) {
			TimeUnit.MILLISECONDS.sleep(pollInterval.toMillis());
			finished = checkAsyncOpFinished(handle);
		}
		return getOperationAsyncResult(handle);
	}
	
	private boolean checkAsyncOpFinished(OperationHandle handle)
		throws TimeoutException, CancellationException, ExecutionException {
		BaseOperationResult opStatus = getOperationAsyncStatus(handle);
		switch ( opStatus.getExecutionState() ) {
			case RUNNING:
			case INITIATED:
				return false;
			case COMPLETED:
				return true;
			case FAILED:
				String fullMsg = (opStatus.getMessages() != null)
								? FStream.from(opStatus.getMessages())
										.map(msg -> msg.getText())
										.join(System.lineSeparator())
								: "";
				throw new ExecutionException(new RESTfulRemoteException("Operation failed: msg=" + fullMsg));
			case TIMEOUT:
				throw new TimeoutException();
			case CANCELED:
				throw new CancellationException();
			default:
				throw new AssertionError("Unknown OperationStatus: " + opStatus.getExecutionState());
		}
	}
}
