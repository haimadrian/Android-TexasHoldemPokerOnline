package org.hit.android.haim.calc.server.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hit.android.haim.calc.server.common.HttpStatus;

import java.io.IOException;

/**
 * @author Haim Adrian
 * @since 17-Apr-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WebException extends IOException {
    private final HttpStatus httpStatus;

    public WebException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return httpStatus.getCode() + " " + httpStatus.name() + ": " + message;
    }
}

