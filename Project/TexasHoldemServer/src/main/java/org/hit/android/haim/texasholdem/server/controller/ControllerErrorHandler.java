package org.hit.android.haim.texasholdem.server.controller;

import com.fasterxml.jackson.databind.node.TextNode;
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
    /**
     * Use this method to return an internal server error response, or bad request, depends on the type of the error.<br/>
     * In case the specified thrown is instance of IllegalArgumentException, this indicates a client error at the service layer,
     * hence we return a BAD REQUEST for that. Otherwise, this is an unexpected server error.
     * @param t A thrown to handle
     * @return A response entity with the exception message as body. (JsonNode, and not simple string)
     */
    public static ResponseEntity<?> handleServerError(Throwable t) {
        log.error("Unexpected error has occurred.", t);

        if (t instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(new TextNode(t.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TextNode("Unexpected error has occurred. Reason: " + t.getMessage()));
    }
}

