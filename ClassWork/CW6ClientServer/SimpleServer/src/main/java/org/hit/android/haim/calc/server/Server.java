package org.hit.android.haim.calc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.calc.action.ActionType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class responsible for server socket management.<br/>
 * We have a thread pool with configurable boundaries to server requests from another edges.<br/>
 * Every request should be populated by implementing a {@link RequestHandler}
 *
 * @author Haim Adrian
 * @since 11-Apr-21
 */
@Log4j2
public class Server {
    private static final String HTTP_BODY = "<html><head><title>King Profus</title></head><body><p><b>Response from server:</b></p><p>%s</p></body></html>";
    private static final String HTTP_HEADERS = "HTTP/1.1 %d %s\r\nContent-Type: %s\nContent-Length: %d";
    private static final String END_OF_HEADERS = "\r\n\r\n";

    /**
     * The port we are listening on
     */
    private final int port;

    /**
     * A {@link RequestHandler} which we will send requests to, and return its response
     */
    private final RequestHandler requestHandler;

    /**
     * A single thread pool used to launch the server, so the user of this class can continue
     */
    private final ExecutorService serverExecutor;

    /**
     * A thread pool with boundaries specified at this class creation
     */
    private final ExecutorService workersExecutor;

    /**
     * Give meaningful names to threads. We use some counter to name the workers
     */
    private final AtomicInteger threadId = new AtomicInteger();

    /**
     * A helper boolean that determines whether server is running or not, to support ordinary stop of the server
     */
    private final AtomicBoolean isRunning = new AtomicBoolean();

    /**
     * A locker we use in order to support ordinary shutdown of the server.<br/>
     * We use it along with {@link #acceptingSocketCondition} to block a call to {@link #stop()} until the server loop is stopped.
     */
    private final Lock locker = new ReentrantLock(true);

    /**
     * See {@link #locker}
     */
    private final Condition acceptingSocketCondition = locker.newCondition();

    /**
     * Jackson object mapper to convert json string to bean and vice versa
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@link Server}
     *
     * @param port The port we are listening on
     * @param corePoolSize Minimum amount of threads that will serve client requests in parallel
     * @param maxPoolSize Maximum amount of threads that will server client requests in parallel. If there are more requests
     * than maximum workers, the requests will be rejected.
     * @param requestHandler A request handler to use for handling client requests
     */
    public Server(int port, int corePoolSize, int maxPoolSize, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), // Do not cache tasks. Create a new thread to handle it or reject the task.
            r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setName("ServerWorker-" + threadId.incrementAndGet());
                return t;
            });
        executor.allowCoreThreadTimeOut(true);
        this.workersExecutor = executor;

        serverExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName("ServerThread");
            t.setDaemon(false); // Keep the JVM running
            return t;
        });

        objectMapper = initializeObjectMapper();
    }

    private ObjectMapper initializeObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        // Do not enable standard indentation ("pretty-printing"), cause the client depends on the new line character
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Allow serialization of "empty" POJOs (no properties to serialize)
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;
    }

    /**
     * Start the server so it will listen on the configured port and accept connections
     */
    public void start() {
        if (!isRunning.get()) {
            serverExecutor.submit(() -> {
                log.info("Server is listening on port: " + port);
                isRunning.set(true);

                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    // Set socket timeout so we will be able to stop server instead of getting blocked at serverSocket.accept()
                    serverSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(5));

                    while (isRunning.get()) {
                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();

                            // Set socket timeout so we will be able to stop server instead of getting blocked at clientInput.read()
                            socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(5));
                            handleNewSocket(socket);
                        } catch (Exception e) {
                            // Ignore it. We have a timeout set so we can shutdown the server ordinary.
                            if (!(e instanceof SocketTimeoutException)) {
                                log.error("Error has occurred while accepting client socket: " + e.toString(), e);

                                // We might get RejectExecutionException when there are too many requests, so handle this as error that can be sent back to client.
                                Response errorResponse = requestHandler.onError(socket != null ? socket.getInetAddress() : null, e);
                                if (socket != null) {
                                    try (BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                                        writeResponse(clientOutput, errorResponse, false);
                                    } catch (Exception ignore) {
                                    }
                                }
                            }

                            safeCloseSocket(socket);
                        }
                    }

                    log.info("Server exited its message loop");
                } catch (Exception e) {
                    log.error("Error has occurred while launching server: " + e.toString(), e);
                } finally {
                    isRunning.set(false);
                    log.info("Server was terminated");

                    locker.lock();
                    try {
                        // Signal that a socket has been accepted/timed-out, so in case someone called #stop() they will resume.
                        acceptingSocketCondition.signal();
                    } catch (Exception ignore) {
                    } finally {
                        locker.unlock();
                    }
                }
            });
        }
    }

    /**
     * Stop the server from whatever it is doing right now.<br/>
     * Please note that this call might be blocked for 10 seconds maximum in case server is waiting for a new connection to arrive.
     */
    public void stop() {
        if (isRunning.get()) {
            log.info("Terminating server");

            isRunning.set(false);
            workersExecutor.shutdown();
            serverExecutor.shutdown();
            locker.lock();
            try {
                // Wait for server to stop.
                acceptingSocketCondition.await(10, TimeUnit.SECONDS);
            } catch (Exception ignore) {

            } finally {
                locker.unlock();
            }
        }
    }

    private void handleNewSocket(Socket socket) {
        workersExecutor.submit(() -> {
            try (BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                log.info("Communication has been started. Client=" + socket);
                AtomicBoolean communicating = new AtomicBoolean(true);
                while (communicating.get() && isRunning.get()) {
                    Request request = null;
                    try {
                        if (requestHandler.onBeforeRequest(socket.getInetAddress())) {
                            request = readRequest(clientInput);

                            if (request == null) {
                                log.info("Request was null");
                                communicating.set(false);
                            } else if (request.getActionType() == ActionType.UNKNOWN) {
                                writeResponse(clientOutput, Response.ok(), request.isHttpRequest());
                            } else {
                                Response response = requestHandler.onRequest(socket.getInetAddress(), request, shouldStop -> communicating.set(!shouldStop.booleanValue()));
                                writeResponse(clientOutput, response, request.isHttpRequest());
                            }
                        } else {
                            log.info("Request was denied");
                            communicating.set(false);
                        }
                    } catch (FavIconException e) {
                        writeFavIcon(socket.getOutputStream());
                    } catch (IOException e) {
                        // Ignore it. We have a timeout set so we can shutdown the server ordinary.
                        if (!(e instanceof SocketTimeoutException)) {
                            log.error("Error has occurred while communicating with client: " + e.toString(), e);
                            Response errorResponse = requestHandler.onError(socket.getInetAddress(), e);
                            writeResponse(clientOutput, errorResponse, e instanceof WebException);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Error has occurred while communicating with client: " + e.toString(), e);
                        writeResponse(clientOutput, Response.badRequest(e.getMessage()), false);
                    }
                }
            } catch (IOException e) {
                log.error("Error has occurred while setting up connection with client: " + e.toString(), e);
            } finally {
                safeCloseSocket(socket);
            }
        });
    }

    private Request readRequest(BufferedReader clientInput) throws IOException {
        String inputLine = clientInput.readLine();
        log.info("Received: " + inputLine);

        if (inputLine == null) {
            return null;
        }

        String inputLineLower = inputLine.toLowerCase();
        if (inputLineLower.contains("http") || !inputLineLower.contains("actiontype")) {
            return readHttpRequest(inputLine, clientInput);
        }

        try {
            return objectMapper.readValue(inputLine, Request.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void writeResponse(BufferedWriter clientOutput, Response response, boolean isHttpRequest) throws IOException {
        String responseAsString;

        if (isHttpRequest) {
            String body = String.format(HTTP_BODY, objectMapper.writeValueAsString(response));
            responseAsString = String.format(HTTP_HEADERS, response.getStatus(), HttpStatus.valueOf(response.getStatus()).name(), "text/html", body.length()) + END_OF_HEADERS + body;
            clientOutput.write(responseAsString);
        } else {
            responseAsString = objectMapper.writeValueAsString(response);
            clientOutput.write(responseAsString + "\n");
        }

        clientOutput.flush();
        log.info("Sent: " + responseAsString.replaceAll("\\\\r\\\\n", System.lineSeparator()));
    }

    private void writeFavIcon(OutputStream clientOutput) {
        try {
            byte[] content = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("favicon.ico").toURI()));

            clientOutput.write((String.format(HTTP_HEADERS, 200, HttpStatus.OK.name(), "image/ico", content.length) + END_OF_HEADERS).getBytes(StandardCharsets.UTF_8));
            clientOutput.write(content);
            clientOutput.flush();
            log.info("Sent: favicon.ico");
        } catch (Exception e) {
            log.error("Error has occurred: " + e.toString(), e);
        }
    }

    private void safeCloseSocket(Socket socket) {
        if (socket != null) {
            try {
                log.info("Communication ended. Client=" + socket);
                socket.close();
            } catch (IOException e) {
                log.error("Something went wrong while closing client socket: " + e.toString(), e);
            }
        }
    }

    private Request readHttpRequest(String inputLine, BufferedReader clientInput) throws IOException {
        String inputLineLower = inputLine.trim().toLowerCase();
        if (inputLineLower.startsWith("host") || inputLineLower.startsWith("connection")) {
            return new Request(ActionType.UNKNOWN, 0.0, 0.0, true);
        }

        if (!inputLineLower.contains("get")) {
            throw new WebException(HttpStatus.METHOD_NOT_ALLOWED, "Use GET only");
        }

        StringBuilder requestToDump = new StringBuilder(inputLine).append(System.lineSeparator());
        String line;
        while (((line = clientInput.readLine()) != null) && !line.isBlank()) {
            requestToDump.append(line).append(System.lineSeparator());
        }

        log.info("HTTP Request: " + requestToDump.toString().trim());

        if (inputLineLower.contains("favicon.ico")) {
            throw new FavIconException();
        }

        // Input line should look like: GET /calc?value=5&lastValue=2&action=MINUS HTTP/1.1
        String[] queryParams = inputLine.split(" ");
        queryParams = queryParams[1].split("\\?");
        if (queryParams.length != 2) {
            throw new WebException(HttpStatus.BAD_REQUEST, "Missing query parameters. Was: " + inputLine);
        }

        double value = 0;
        double lastValue = 0;
        ActionType actionType = null;

        queryParams = queryParams[1].split("&");
        for (String queryParam : queryParams) {
            String[] nameAndValue = queryParam.split("=");
            if (nameAndValue.length != 2) {
                throw new WebException(HttpStatus.BAD_REQUEST, "Illegal query parameter. Was: " + nameAndValue[0]);
            }

            if (nameAndValue[0].equalsIgnoreCase("value")) {
                try {
                    value = Double.parseDouble(nameAndValue[1]);
                } catch (NumberFormatException e) {
                    throw new WebException(HttpStatus.BAD_REQUEST, "Illegal query parameter. Value must be of type double. Was: " + nameAndValue[1]);
                }
            } else if (nameAndValue[0].equalsIgnoreCase("lastValue")) {
                try {
                    lastValue = Double.parseDouble(nameAndValue[1]);
                } catch (NumberFormatException e) {
                    throw new WebException(HttpStatus.BAD_REQUEST, "Illegal query parameter. lastValue must be of type double. Was: " + nameAndValue[1]);
                }
            } else if (nameAndValue[0].equalsIgnoreCase("actionType")) {
                try {
                    actionType = ActionType.valueOf(nameAndValue[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new WebException(HttpStatus.BAD_REQUEST, "Illegal query parameter. Unknown action. Was: " + nameAndValue[1]);
                }
            }
        }

        if (actionType == null) {
            throw new WebException(HttpStatus.BAD_REQUEST, "Missing query parameter. actionType is mandatory");
        }

        return new Request(actionType, value, lastValue, true);
    }

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

        static HttpStatus valueOf(int httpStatus) {
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

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FavIconException extends IOException {

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class WebException extends IOException {
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
}

