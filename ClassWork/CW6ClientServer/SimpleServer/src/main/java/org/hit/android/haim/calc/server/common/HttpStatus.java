package org.hit.android.haim.calc.server.common;

/**
 * @author Haim Adrian
 * @since 17-Apr-21
 */
public enum HttpStatus {
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpStatus(int code) {
        this.code = code;
    }

    public static HttpStatus valueOf(int httpStatus) {
        HttpStatus result = null;

        for (int i = 0; (i < values().length) && (result == null); i++) {
            if (httpStatus == values()[i].code) {
                result = values()[i];
            }
        }

        return result == null ? INTERNAL_SERVER_ERROR : result;
    }

    public int getCode() {
        return code;
    }
}

