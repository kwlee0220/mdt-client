package mdt.model;

/**
 * 모델 객체 생성 과정에서 오류가 발생한 경우 발생되는 예외이다.
 * <p>
 * 예를 들어 {@link mdt.aas.DataTypes}에서 알 수 없는 데이터 타입 이름이 주어진 경우에 발생된다.
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class ModelGenerationException extends MDTException {
    private static final long serialVersionUID = 1L;

    /**
     * 주어진 메시지로 예외 객체를 생성한다.
     *
     * @param message	예외 메시지.
     */
    public ModelGenerationException(final String message) {
        super(message);
    }
}
