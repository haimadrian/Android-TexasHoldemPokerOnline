package com.example.calc.server;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.calc.action.ActionType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 11-Apr-21
 */
public class CalculatorWebService {
    private static final CalculatorWebService instance = new CalculatorWebService();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private Socket clientSocket;
    private BufferedWriter outToServer;
    private BufferedReader inFromServer;
    private final Gson gson;

    private CalculatorWebService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient(); // Set it to free-style so we will be able to get raw strings from server. (e.g. "Bye" when signing out, instead of { "msg" : "Bye" } )
        gson = gsonBuilder.create();
    }

    public static CalculatorWebService getInstance() {
        return instance;
    }

    public void start() {
        executor.submit(() -> {
            try {
                clientSocket = new Socket("192.168.0.10", 1234);
                clientSocket.setSoTimeout((int)TimeUnit.SECONDS.toMillis(10));
                outToServer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (Exception e) {
                Log.e("Web", "Error has occurred: " + e.toString(), e);
            }
        });
    }

    public void stop() {
        // Hold the references because they will be closed "later", and it might be that the start method will be invoked. (when we change phone orientation)
        Socket clientSocket = this.clientSocket;
        BufferedWriter outToServer = this.outToServer;
        BufferedReader inFromServer = this.inFromServer;
        executeRequest(new Request(ActionType.DISCONNECT, null, null), response -> {
            try {
                inFromServer.close();
            } catch (IOException ignore) {
            }

            try {
                outToServer.close();
            } catch (IOException ignore) {
            }

            try {
                clientSocket.close();
            } catch (IOException ignore) {
            }
        });
    }

    public void executeCalculatorAction(double value, double lastVal, ActionType actionType, Consumer<Response> responseConsumer) {
        executeRequest(new Request(actionType, value, lastVal), responseConsumer);
    }

    private void executeRequest(Request request, Consumer<Response> responseConsumer) {
        Looper looper = Looper.myLooper();
        executor.submit(() -> {
            try {
                String requestJson = gson.toJson(request);
                Log.d("Web", "Sending request: " + requestJson);

                outToServer.write(requestJson + '\n');
                outToServer.flush();

                Response response;

                try {
                    String responseLine = inFromServer.readLine();
                    Log.d("Web", "Response: " + responseLine);
                    response = gson.fromJson(responseLine, Response.class);
                } catch (SocketTimeoutException e) {
                    response = new Response(408, request.getValue(), "408 TIME OUT");
                }

                if (responseConsumer != null) {
                    Response finalResponse = response;
                    new Handler(looper).post(() -> responseConsumer.accept(finalResponse));
                }
            } catch (Exception e) {
                Log.e("Web", "Error has occurred: " + e.toString() + ". Request=" + request, e);
            }
        });
    }
}
