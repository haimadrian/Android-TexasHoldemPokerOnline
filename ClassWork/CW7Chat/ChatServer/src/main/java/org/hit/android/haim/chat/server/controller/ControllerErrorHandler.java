package org.hit.android.haim.chat.server.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Log4j2
public class ControllerErrorHandler {
    public static ResponseEntity<?> returnError(Throwable t) {
        log.info("Returning error to client: " + t.getMessage(), t);

        if (t instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(new TextNode(t.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TextNode("Unexpected error has occurred. Reason: " + t.getMessage()));
    }
}

