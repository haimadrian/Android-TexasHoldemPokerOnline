package org.hit.android.haim.calc.server.common;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 11-Apr-21
 */
public interface RequestHandler {
    /**
     * Occurs right after server accepted a new request, and before reading the input of that request, to let user to filter requests.
     *
     * @param client The accepted client
     * @return Whether to populate this request or not
     */
    default boolean onBeforeRequest(@SuppressWarnings("unused") ClientInfo client) {
        return true;
    }

    /**
     * Occurs when a request was accepted, and its input was read by server.<br/>
     * This lets implementor to handle the input and return a response that the server will send back to the client.
     *
     * @param client The accepted client
     * @param request The body of a request. Can never be null.
     * @param stopCommunication A consumer to let implementor to stop the communication based on the request
     * @return A response to send back to the client
     */
    String onRequest(ClientInfo client, String request, Consumer<Boolean> stopCommunication) throws IOException;

    /**
     * Occurs when there was any unexpected error while accepting a client request.<br/>
     * client is nullable in case there was an error when accepting a request. Otherwise, it will refer to the failed client.<br/>
     * The result of this method will be written to the client error stream.
     *
     * @param client The accepted client
     * @param thrown The error
     * @return Error response to return to the client, in case the error happened after accepting a client
     */
    String onError(ClientInfo client, Throwable thrown) throws IOException;
}

