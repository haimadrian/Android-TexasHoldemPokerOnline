package com.example.calc.server;

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
    private Double value;

    /**
     * Optional error message, when there is an error response
     */
    private String errorMessage;

    public Response() {
    }

    public Response(int status, Double value, String errorMessage) {
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
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

