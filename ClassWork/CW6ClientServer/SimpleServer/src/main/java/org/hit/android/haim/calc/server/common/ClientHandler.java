package org.hit.android.haim.calc.server.common;

import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.calc.server.common.exception.FavIconException;

import java.io.*;
import java.net.SocketTimeoutException;
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
    private final String executingThread;

    private AtomicBoolean isRunning;

    public ClientHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.executingThread = Thread.currentThread().getName();
    }

    public void handle(ClientInfo client, InputStream clientInput, OutputStream clientOutput) {
        try {
            isRunning = new AtomicBoolean(true);

            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientInput));
            BufferedWriter clientWriter = new BufferedWriter(new OutputStreamWriter(clientOutput));

            while (isRunning.get()) {
                String request = null;
                try {
                    if (requestHandler.onBeforeRequest(client)) {
                        do {
                            request = readRequest(clientReader);
                        } while (isRunning.get() && TRY_AGAIN.equals(request));

                        if (request.isEmpty()) {
                            log.info("Request was empty. Ending communication");
                            isRunning.set(false);
                        } else if (!TRY_AGAIN.equals(request)) {
                            String response = requestHandler.onRequest(client, request, shouldStop -> isRunning.set(!shouldStop.booleanValue()));
                            writeResponse(clientWriter, response);
                        }
                    } else {
                        log.info("Request was denied by handler. Ending communication");
                        isRunning.set(false);
                    }
                } catch (FavIconException e) {
                    writeFavIcon(clientOutput);
                } catch (IOException | IllegalArgumentException e) {
                    log.error("Error has occurred while communicating with client: " + e, e);
                    String errorResponse = requestHandler.onError(client, e);
                    writeResponse(clientWriter, errorResponse);
                }
            }
        } catch (IOException e) {
            log.error("Error has occurred while setting up connection with client: " + e, e);
        }
    }

    public void stop() {
        log.info("Stopping worker: " + executingThread);
        isRunning.set(false);
    }

    String readRequest(BufferedReader clientInput) throws IOException {
        StringBuilder request = new StringBuilder();
        String inputLine = null;
        do {
            try {
                inputLine = clientInput.readLine();
                if (request.length() > 0) {
                    request.append(System.lineSeparator());
                }

                request.append(inputLine == null ? "" : inputLine);
            } catch (SocketTimeoutException e) {
                // When there was a timeout and the input is empty, ask caller to try again,
                // as long as the server is still running
                if (request.length() == 0) {
                    return TRY_AGAIN;
                }
            }
        } while ((inputLine != null) && !inputLine.isBlank());

        String requestAsString = request.toString().trim();
        logRequest(requestAsString);
        return requestAsString;
    }

    void writeResponse(BufferedWriter clientOutput, String response) throws IOException {
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

    private void writeFavIcon(OutputStream clientOutput) {
        try {
            byte[] content = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("favicon.ico").toURI()));

            clientOutput.write((String.format(HTTP_HEADERS, 200, HttpStatus.OK.name(), "image/ico", content.length) + END_OF_HEADERS).getBytes(StandardCharsets.UTF_8));
            clientOutput.write(content);
            clientOutput.flush();
            log.info("Sent: favicon.ico");
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

