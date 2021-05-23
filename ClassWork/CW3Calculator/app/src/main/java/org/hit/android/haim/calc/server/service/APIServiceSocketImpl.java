package org.hit.android.haim.calc.server.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.server.Request;
import org.hit.android.haim.calc.server.Response;
import org.hit.android.haim.calc.server.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 21-May-21
 */
public class APIServiceSocketImpl implements APIService {
    private Socket clientSocket;
    private BufferedWriter outToServer;
    private BufferedReader inFromServer;
    private final Gson gson;
    private User user;

    public APIServiceSocketImpl() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient(); // Set it to free-style so we will be able to get raw strings from server. (e.g. "Bye" when signing out, instead of { "msg" : "Bye" } )
        gson = gsonBuilder.create();
    }

    @Override
    public void signIn(String email, String pwd, Consumer<Response> responseConsumer) {
        user = new User(email, email, null, null);
        executeDynamicAction(email + "##" + pwd, ActionType.CONNECT, responseConsumer);
    }

    @Override
    public void signUp(User user, String pwd, Consumer<Response> responseConsumer) {
        executeDynamicAction(user.getEmail() + "##" + pwd, ActionType.REGISTER, responseConsumer);
    }

    @Override
    public void executeCalculatorAction(double value, double lastVal, ActionType actionType, Consumer<Response> responseConsumer) {
        executeRequest(new Request(actionType, value, lastVal), responseConsumer);
    }

    private void connect() {
        try {
            String ip = "192.168.0.8";
            int port = 1234;
            Log.d("Web", "Connecting to server at: " + ip + ":" + port);

            clientSocket = new Socket(ip, port);
            clientSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(2));
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (Exception e) {
            Log.e("Web", "Error has occurred: " + e.toString(), e);
        }
    }

    @Override
    public void disconnect() {
        Log.d("Web", "Disconnecting from server.");
        executeRequest(new Request(ActionType.DISCONNECT, null, null), response -> closeStreams());
    }

    @Override
    public User getCurrentUser() {
        return user;
    }

    private void closeStreams() {
        Log.d("Web", "Closing communication with server.");

        try {
            if (outToServer != null) {
                outToServer.close();
                outToServer = null;
            }
        } catch (IOException ignore) {
        }

        try {
            if (inFromServer != null) {
                inFromServer.close();
                inFromServer = null;
            }
        } catch (IOException ignore) {
        }

        try {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
        } catch (IOException ignore) {
        }
    }

    public void executeDynamicAction(String dynamicValue, ActionType actionType, Consumer<Response> responseConsumer) {
        executeRequest(new Request(actionType, dynamicValue), responseConsumer);
    }

    private void executeRequest(Request request, Consumer<Response> responseConsumer) {
        try {
            if (outToServer == null) {
                connect();
            }

            Response response;
            if (outToServer != null) {
                String requestJson = gson.toJson(request);
                Log.d("Web", "Sending request: " + requestJson);

                outToServer.write(requestJson + "\n\n");
                outToServer.flush();

                try {
                    String responseLine = inFromServer.readLine();
                    Log.d("Web", "Response: " + responseLine);

                    if ((responseLine == null) || !responseLine.trim().startsWith("{")) {
                        disconnect();
                        response = new Response(500, String.valueOf(request.getValue()), "500 INTERNAL SERVER ERROR");
                    } else {
                        response = gson.fromJson(responseLine, Response.class);
                    }
                } catch (SocketTimeoutException e) {
                    closeStreams();
                    response = new Response(408, String.valueOf(request.getValue()), "408 TIME OUT");
                }
            } else {
                response = new Response(503, String.valueOf(request.getValue()), "503 SERVICE UNAVAILABLE");
                Log.w("Web", "Not connected. Unable to send requests.");
            }

            if (responseConsumer != null) {
                Response finalResponse = response;
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        responseConsumer.accept(finalResponse);
                    } catch (Exception e) {
                        Log.e("WebResponseHandler", "Error has occurred while handling web response.", e);
                    }
                });
            }
        } catch (Exception e) {
            closeStreams();
            Log.e("Web", "Error has occurred: " + e.toString() + ". Request=" + request, e);
        }
    }
}
