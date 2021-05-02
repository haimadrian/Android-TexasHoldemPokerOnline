package org.hit.android.haim.calc.server.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hit.android.haim.calc.server.common.HttpStatus;

/**
 * @author Haim Adrian
 * @since 13-Apr-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Response {
    /**
     * HTTP status code, to support OK / ERROR responses
     */
    private int status;

    public static Response ok() {
        return ok(HttpStatus.OK.getCode(), null);
    }

    public static Response ok(String value) {
        return ok(HttpStatus.OK.getCode(), value);
    }

    public static Response ok(int status, String value) {
        return new SimpleResponse(status, value);
    }

    public static Response error() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "INTERNAL_SERVER_ERROR");
    }

    public static Response error(String errorMessage) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), errorMessage);
    }

    public static Response error(int status, String errorMessage) {
        return new ErrorResponse(status, errorMessage);
    }

    public static Response badRequest() {
        return badRequest(HttpStatus.BAD_REQUEST.getCode(), "BAD_REQUEST");
    }

    public static Response badRequest(String errorMessage) {
        return badRequest(HttpStatus.BAD_REQUEST.getCode(), errorMessage);
    }

    public static Response badRequest(int status, String errorMessage) {
        return new ErrorResponse(status, errorMessage);
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class SimpleResponse extends Response {
        /**
         * An action response value (calculation)
         */
        private String value;

        public SimpleResponse(int status, String value) {
            super(status);
            this.value = value;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ErrorResponse extends Response {
        /**
         * Optional error message, when there is an error response
         */
        private String errorMessage;

        public ErrorResponse(int status, String errorMessage) {
            super(status);
            this.errorMessage = errorMessage;
        }
    }
}

