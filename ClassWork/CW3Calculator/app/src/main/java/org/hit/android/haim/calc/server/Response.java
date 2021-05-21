package org.hit.android.haim.calc.server;

/**
 * @author Haim Adrian
 * @since 13-Apr-21
 */
public class Response {
    /**
     * HTTP status code, to support OK / ERROR responses
     */
    private int status;

    /**
     * An action response value (calculation)
     */
    private String value;

    /**
     * Optional error message, when there is an error response
     */
    private String errorMessage;

    public Response() {
    }

    public Response(int status, String value, String errorMessage) {
        this.status = status;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", value=" + value +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

