package de.tum.in.www1.exerciseapp.domain.umlresult;

import java.util.ArrayList;
import java.util.List;

/**
 * Workaround to build the json structure expected by the web front-end
 */
public class TestResultDetailsDTO {
    private String methodName;
    private ErrorsWrapper errors;

    public String getMethodName() {
        return methodName;
    }

    public ErrorsWrapper getErrors() {
        return errors;
    }

    public static TestResultDetailsDTO create(String methodName, String errorMessage) {
        TestResultDetailsDTO dto = new TestResultDetailsDTO();
        dto.methodName = methodName;
        dto.errors = new ErrorsWrapper();
        dto.errors.error = new ArrayList<>(1);
        ErrorMessage msg = new ErrorMessage();
        msg.message = errorMessage;
        dto.errors.error.add(msg);
        return dto;
    }

    public static class ErrorsWrapper {
        private List<ErrorMessage> error;

        public List<ErrorMessage> getError() {
            return error;
        }
    }


    public static class ErrorMessage {
        private String message;

        public String getMessage() {
            return message;
        }
    }
}
