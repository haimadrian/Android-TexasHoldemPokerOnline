package org.hit.android.haim.texasholdem.web;

/**
 * @author Haim Adrian
 * @since 02-Jul-21
 */
public enum HttpStatus {
    OK(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
