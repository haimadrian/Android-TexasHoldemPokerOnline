package org.hit.android.haim.calc.server.common;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class responsible for server socket management.<br/>
 * We have a thread pool with configurable boundaries to serve requests from other edges.<br/>
 * Every request should be populated by implementing a {@link RequestHandler}
 *
 * @author Haim Adrian
 * @since 11-Apr-21
 */
@Log4j2
public class TCPServer {
    /**
     * Use an atomic counter so we can count instances of {@link TCPServer} and give them meaningful name.
     */
    private static final AtomicInteger serverIdCounter = new AtomicInteger();

    /**
     * Use an atomic counter so we can count threads (client handlers in current tcp server) and give them meaningful name.
     */
    private final AtomicInteger workerThreadIdCounter;

    /**
     * An identifier used to identify servers. We use it as server thread name
     */
    private final int serverId;

    /**
     * The port we are listening on
     */
    private final int port;

    /**
     * Minimum amount of threads that will serve client requests in parallel
     */
    private final int corePoolSize;

    /**
     * Maximum amount of threads that will serve client requests in parallel.
     * If there are more requests than maximum workers, the requests will be rejected.
     */
    private final int maxPoolSize;

    /**
     * A {@link RequestHandler} which we will send requests to, and return its response
     */
    private final RequestHandler requestHandler;

    /**
     * A helper boolean that determines whether server is running or not, to support ordinary shutdown of the server
     */
    private final AtomicBoolean isRunning;

    /**
     * A single thread pool used to launch the server, so the user of this class can continue
     */
    private ExecutorService serverExecutor;

    /**
     * A thread pool with boundaries specified at this class creation
     */
    private ExecutorService workersExecutor;

    /**
     * Hold references to request handlers so we can inform them to stop when server is required to stop
     */
    private List<ClientHandler> handlers;

    /**
     * Constructs a new {@link TCPServer}
     *
     * @param port The port we are listening on
     * @param corePoolSize Minimum amount of threads that will serve client requests in parallel
     * @param maxPoolSize Maximum amount of threads that will serve client requests in parallel. If there are more requests
     * than maximum workers, the requests will be rejected.
     * @param requestHandler A request handler to use for handling client requests
     */
    public TCPServer(int port, int corePoolSize, int maxPoolSize, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        workerThreadIdCounter = new AtomicInteger();

        serverId = serverIdCounter.incrementAndGet();
        isRunning = new AtomicBoolean();
    }

    private ThreadPoolExecutor initializeWorkersThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), // Do not cache tasks. Create a new thread to handle it or reject the task.
            this::workerThreadFactory);

        // So core threads will also time-out when no requests are arrived,
        // and we will be able to shutdown server ordinary
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * Start the server so it will listen on the configured port and accept connections
     */
    public void start() {
        // Atomic check and update, so only single thread can start a server
        if (!isRunning.getAndSet(true)) {
            serverExecutor = Executors.newSingleThreadExecutor(this::serverThreadFactory);
            workersExecutor = initializeWorkersThreadPool();
            handlers = new ArrayList<>();

            serverExecutor.submit(() -> {
                log.info("Server is listening on port: " + port);
                try (ServerSocket serverSocket = new ServerSocket(port, maxPoolSize)) {
                    // Set socket timeout so we will be able to stop server instead of getting blocked at serverSocket.accept()
                    serverSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(2));

                    while (isRunning.get()) {
                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();

                            // As the "accept" call above can wait up to 2 seconds, where the server
                            // might have been stopped during those two seconds, we would like to ignore this request.
                            if (!isRunning.get()) {
                                safeCloseSocket(socket);
                            } else {
                                // Set socket timeout so we will be able to stop server instead of getting blocked at clientInput.read()
                                socket.setSoTimeout((int) TimeUnit.MILLISECONDS.toMillis(100));
                                onSocketAccepted(socket);
                            }
                        } catch (Throwable t) {
                            onSocketHandlerError(socket, t);
                        }
                    }

                    log.info("Server exited its message loop");
                } catch (Exception e) {
                    log.error("Error has occurred while launching server: " + e, e);
                } finally {
                    isRunning.set(false);
                    log.info("Server was terminated");
                }
            });
        }
    }

    /**
     * Stop the server from whatever it is doing right now.<br/>
     * Please note that this call might be blocked for 10 seconds maximum in case server is waiting for a new connection to arrive.
     */
    public void stop() {
        if (isRunning.getAndSet(false)) {
            log.info("Stopping workers");
            handlers.forEach(ClientHandler::stop);

            log.info("Shutting down thread pools...");
            workersExecutor.shutdown();
            serverExecutor.shutdown();

            try {
                // Wait for server to stop.
                log.info("Waiting for workers to shutdown...");
                if (!workersExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Time out has occurred while waiting for workers to stop.");
                }
            } catch (Exception ignore) {
            } finally {
                try {
                    // Wait for server to stop.
                    log.info("End waiting for workers. Waiting for tcp server...");
                    if (!serverExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("Time out has occurred while waiting for server to stop.");
                    }
                } catch (Exception ignore) {
                } finally {
                    log.info("End waiting for server");
                }
            }
        }
    }

    /**
     * Occurs when new client socket is accepted.<br/>
     * We will pass the streams of that socket to {@link ClientHandler} in order to handle requests and responses.<br/>
     * Handling of this socket will occur on a different thread, so we can server other clients asynchronously
     * @param socket The socket to handle
     */
    private void onSocketAccepted(Socket socket) {
        workersExecutor.submit(() -> {
            ClientInfo client = ClientInfo.from(socket);
            log.info("Communication with client started. Client=" + client);

            ClientHandler clientHandler = null;
            try {
                clientHandler = new ClientHandler(requestHandler);
                handlers.add(clientHandler);
                clientHandler.handle(client, socket.getInputStream(), socket.getOutputStream());
            } catch (Throwable t) {
                log.error("Error has occurred while setting up connection with client. Error: " + t, t);
            } finally {
                log.info("Communication with client ended. Client=" + client);
                safeCloseSocket(socket);
                handlers.remove(clientHandler);
            }
        });
    }

    /**
     * Handle exceptions occurred while handling client sockets.<br/>
     * Ignore SocketTimeoutException as those might occur frequently in case no-one communicates with the server
     * @param socket The socket to write error response to
     * @param e The exception
     */
    private void onSocketHandlerError(Socket socket, Throwable e) {
        // Ignore it. We have a timeout set so we can shutdown the server ordinary.
        if (!(e instanceof SocketTimeoutException)) {
            log.error("Error has occurred while accepting client socket: " + e, e);

            // We might get RejectExecutionException when there are too many requests, so handle this as error that can be sent back to client.
            try {
                String errorResponse = requestHandler.onError(ClientInfo.from(socket), e);
                if (socket != null) {
                    BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    new ClientHandler(requestHandler).writeResponse(clientOutput, errorResponse);
                }
            } catch (Exception ignore) {
            }

            safeCloseSocket(socket);
        }
    }

    private void safeCloseSocket(Socket socket) {
        if (socket != null) {
            try { socket.getOutputStream().close(); } catch (IOException ignore) { }
            try { socket.getInputStream().close(); } catch (IOException ignore) { }
            try { socket.close(); } catch (IOException ignore) { }
        }
    }

    private Thread workerThreadFactory(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setName("Server-" + serverId + "-Worker-" + workerThreadIdCounter.incrementAndGet());
        return t;
    }

    private Thread serverThreadFactory(Runnable r) {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setName("ServerThread-" + serverId);
        t.setDaemon(false); // Keep the JVM running
        return t;
    }
}

