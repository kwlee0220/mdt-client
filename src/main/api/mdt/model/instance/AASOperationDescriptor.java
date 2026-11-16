package mdt.model.instance;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;

import utils.Preconditions;


/**
 * MDTInstance의 서브모델에 정의된 AAS {@link Operation} SubmodelElement 하나의 정보를 담는 기술자.
 *
 * @param idShort		Operation SubmodelElement의 idShort ({@code SubmodelElementList}의 원소인 경우는 {@code null}).
 * @param displayName	Operation SubmodelElement의 displayName. 여러 개인 경우는 첫번째 값이고,
 * 						없는 경우는 {@code null}.
 * @param submodel		Operation이 포함된 Submodel의 idShort.
 * @param path			Submodel 내에서 Operation SubmodelElement의 idShortPath.
 */
public record AASOperationDescriptor(String idShort, String displayName, String submodel, String path) {
    public AASOperationDescriptor {
        Preconditions.checkNotNull(submodel, "submodel must not be null");
        Preconditions.checkNotNull(path, "path must not be null");
    }

    /**
     * 주어진 Operation SubmodelElement로부터 기술자를 생성한다.
     *
     * @param submodelIdShort	Operation이 포함된 Submodel의 idShort.
     * @param path				Submodel 내에서 Operation SubmodelElement의 idShortPath.
     * @param op				대상 Operation SubmodelElement.
     * @return	생성된 기술자.
     */
    public static AASOperationDescriptor from(String submodelIdShort, String path, Operation op) {
        Preconditions.checkNotNullArgument(op, "Operation is null");

        List<LangStringNameType> displayNames = op.getDisplayName();
        String displayName = (displayNames != null && !displayNames.isEmpty())
                            ? displayNames.get(0).getText() : null;
        return new AASOperationDescriptor(op.getIdShort(), displayName, submodelIdShort, path);
    }
}
