package org.hit.android.haim.texasholdem.server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * A class to be used as utility class for creating error response for any unexpected error in the controllers
 *
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@Log4j2
public class ControllerErrorHandler {
    public static ResponseEntity<?> returnInternalServerError(Throwable t) {
        log.error("Unexpected error has occurred.", t);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error has occurred. Reason: " + t.getMessage());
    }
}

