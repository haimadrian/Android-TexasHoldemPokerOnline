package org.hit.android.haim.calc.server.common;

import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.calc.server.common.exception.FavIconException;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A general handler used by {@link TCPServer} when a new socket is accepted
 * @author Haim Adrian
 * @since 17-Apr-21
 */
@Log4j2
public class ClientHandler {
    // When the request was an HTTP one, the response will be an HTML, as we assume the request arrived from browser
    public static final String HTTP_BODY = "<html><head><title>King Profus</title></head><body><p><b>Response from server:</b></p><p>%s</p></body></html>";
    public static final String HTTP_HEADERS = "HTTP/1.1 %d %s\r\nContent-Type: %s\nContent-Length: %d";
    public static final String END_OF_HEADERS = "\r\n\r\n";

    /**
     * Use a marker in order to identify when we received an empty input and need to close socket,
     * or received empty input due to socket-timeout. When there is a socket timeout we need to try
     * and read again, until we receive empty input or DISCONNECT.
     */
    private static final String TRY_AGAIN = "~AGAIN~";

    /**
     * A {@link RequestHandler} which we will send requests to, and return its response
     */
    private final RequestHandler requestHandler;

    /**
     * The name of the thread executing this handler. We use it when stopping a handler, so we can log
     * an informational message about what thread is stopping.
     */
    private final String executingThread;

    /**
     * A thread safe flag used to tell whether this handler is running or not.
     */
    private AtomicBoolean isRunning;

    /**
     * Constructs a new {@link ClientHandler}
     * @param requestHandler A {@link RequestHandler} which we will send requests to, and return its response
     */
    public ClientHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.executingThread = Thread.currentThread().getName();
    }

    /**
     * This method is called from {@link TCPServer} when a new socket is accepted, by using a separate thread.<br/>
     * Here we handle client streams in order to read its request, and send a response to.
     * @param client {@link ClientInfo} to identify the client
     * @param clientInput Client input stream, to read requests from
     * @param clientOutput Client output stream, to send responses to
     */
    public void handle(ClientInfo client, InputStream clientInput, OutputStream clientOutput) {
        try {
            isRunning = new AtomicBoolean(true);

            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientInput));
            BufferedWriter clientWriter = new BufferedWriter(new OutputStreamWriter(clientOutput));

            // Handle requests as long as we are active
            while (isRunning.get()) {
                String request;
                try {
                    if (requestHandler.onBeforeRequest(client)) {
                        do {
                            // In order to support reading a full HTTP request, we read all lines
                            // from input stream, and might need to try reading again in case of time-out.
                            request = readRequest(clientReader);
                        } while (isRunning.get() && TRY_AGAIN.equals(request));

                        if (request.isEmpty()) {
                            log.info("Request was empty. Ending communication");
                            isRunning.set(false);
                        } else if (!TRY_AGAIN.equals(request)) {
                            String response = requestHandler.onRequest(client, request, shouldStop -> isRunning.set(!shouldStop));
                            writeResponse(clientWriter, response);
                        }
                    } else {
                        log.info("Request was denied by handler. Ending communication");
                        isRunning.set(false);
                    }
                } catch (FavIconException e) {
                    writeFavIcon(clientOutput);
                } catch (IOException | IllegalArgumentException e) {
                    log.error("Error has occurred while communicating with client: " + client + ". Error: " + e, e);
                    String errorResponse = requestHandler.onError(client, e);
                    writeResponse(clientWriter, errorResponse);
                }
            }
        } catch (IOException e) {
            log.error("Error has occurred while setting up connection with client: " + client + ". Error: " + e, e);
        }
    }

    /**
     * Stop this handler so we will accept nothing more from client
     */
    public void stop() {
        log.info("Stopping worker: " + executingThread);
        isRunning.set(false);
    }

    /**
     * A utility method used to read (fully) request from client input.<br/>
     * As we support reading HTTP requests, we read all lines until end of request (null or empty line).
     * @param clientInput A reader to read lines from
     * @return The request content
     * @throws IOException In case we have failed reading input from the specified reader
     */
    String readRequest(BufferedReader clientInput) throws IOException {
        StringBuilder request = new StringBuilder();
        String inputLine;
        try {
            while (((inputLine = clientInput.readLine()) != null) && !inputLine.isBlank()) {
                request.append(inputLine).append(System.lineSeparator());
            }
        } catch (SocketTimeoutException e) {
            // When there was a timeout and the input is empty, ask caller to try again,
            // as long as the server is still running
            if (request.length() == 0) {
                return TRY_AGAIN;
            }
        }

        String requestAsString = request.toString().trim();
        logRequest(requestAsString);
        return requestAsString;
    }

    /**
     * A utility method used to write (fully) response to client output.
     * @param clientOutput A writer to write response to
     * @param response The response to write
     * @throws IOException In case we have failed writing output to the specified writer
     */
    void writeResponse(BufferedWriter clientOutput, String response) throws IOException {
        // We let the RequestHandler to return null from onRequest, to avoid of returning anything back to client.
        if (response != null) {
            clientOutput.write(response.endsWith("\n") ? response : response + '\n');
            clientOutput.flush();

            String responseToLog = response.replaceAll("\\\\r\\\\n", System.lineSeparator());
            if (!response.contains("HTTP")) {
                log.info("Sent: " + responseToLog);
            } else {
                log.info("Sent: HTTP Response details below" + System.lineSeparator() +
                    "######################### Begin #########################" + System.lineSeparator() +
                    responseToLog + System.lineSeparator() +
                    "########################## End ##########################");
            }
        }
    }

    private void writeFavIcon(OutputStream clientOutput) {
        try {
            URL favIcon = getClass().getClassLoader().getResource("favicon.ico");
            if (favIcon == null) {
                log.warn("No favicon.ico could be found. Returning error to client.");
                clientOutput.write((String.format(HTTP_HEADERS, HttpStatus.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND.name(), "image/ico", 0) + END_OF_HEADERS).getBytes(StandardCharsets.UTF_8));
                clientOutput.flush();
            } else {
                byte[] content = Files.readAllBytes(Paths.get(favIcon.toURI()));
                clientOutput.write((String.format(HTTP_HEADERS, HttpStatus.OK.getCode(), HttpStatus.OK.name(), "image/ico", content.length) + END_OF_HEADERS).getBytes(StandardCharsets.UTF_8));
                clientOutput.write(content);
                clientOutput.flush();
                log.info("Sent: favicon.ico");
            }
        } catch (Exception e) {
            log.error("Error has occurred: " + e, e);
        }
    }

    private void logRequest(String requestAsString) {
        if (!requestAsString.contains("HTTP")) {
            log.info("Received: " + requestAsString);
        } else {
            log.info("Received: HTTP Request details below" + System.lineSeparator() +
                "######################### Begin #########################" + System.lineSeparator() +
                requestAsString + System.lineSeparator() +
                "########################## End ##########################");
        }
    }
}

