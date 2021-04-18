package org.hit.android.haim.calc.server.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.action.ArithmeticAction;
import org.hit.android.haim.calc.server.common.ClientInfo;
import org.hit.android.haim.calc.server.common.HttpStatus;
import org.hit.android.haim.calc.server.common.RequestHandler;
import org.hit.android.haim.calc.server.common.exception.FavIconException;
import org.hit.android.haim.calc.server.common.exception.WebException;

import java.io.IOException;
import java.util.function.Consumer;

import static org.hit.android.haim.calc.server.common.ClientHandler.*;

/**
 * @author Haim Adrian
 * @since 13-Apr-21
 */
public class CalculatorClientHandler implements RequestHandler {
    public static final String VALUE_QUERY_PARAM = "value";
    public static final String LAST_VALUE_QUERY_PARAM = "lastValue";
    /**
     * Jackson object mapper to convert json string to bean and vice versa
     */
    private final ObjectMapper objectMapper;

    public CalculatorClientHandler() {
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

    @Override
    public String onRequest(ClientInfo client, String requestString, Consumer<Boolean> stopCommunication) throws IOException {
        Boolean stopCommunicating = Boolean.TRUE;
        Response response;
        Request request = null;

        if (!requestString.startsWith("{")) {
            request = readHttpRequest(requestString);
        } else {
            try {
                request = objectMapper.readValue(requestString, Request.class);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        if ((request != null) && (request.getActionType() != ActionType.CONNECT) && (request.getActionType() != ActionType.DISCONNECT)) {
            stopCommunicating = Boolean.FALSE;

            if (request.getActionType() == null) {
                response = Response.badRequest("actionType is mandatory");
            } else {
                response = Response.ok(doAction(request));
            }
        } else {
            response = Response.ok();
        }

        stopCommunication.accept(stopCommunicating);
        return responseToString(response, request.isHttpRequest());
    }

    @Override
    public String onError(ClientInfo client, Throwable thrown) throws IOException {
        Response response;

        if (thrown instanceof IllegalArgumentException) {
            response = Response.badRequest(thrown.getMessage());
        } else if (thrown instanceof WebException) {
            response = Response.error(((WebException) thrown).getHttpStatus().getCode(), thrown.getMessage());
        } else {
            response = Response.error(thrown.getMessage());
        }

        return objectMapper.writeValueAsString(response);
    }

    private String responseToString(Response response, boolean httpRequest) throws IOException {
        String responseString = objectMapper.writeValueAsString(response);

        if (httpRequest) {
            String body = String.format(HTTP_BODY, responseString);
            responseString = String.format(HTTP_HEADERS, response.getStatus(), HttpStatus.valueOf(response.getStatus()).name(), "text/html", body.length()) + END_OF_HEADERS + body;
        }

        return responseString;
    }

    /**
     * Execute an action based on operator or special function.
     *
     * @param value The value to execute an action on
     * @param lastVal For operators that take two arguments, this is the right argument
     * @param actionText Action text to execute. See {@link ActionType#valueOfActionText(String)}
     * @return Result of the calculation.
     * @see ActionType
     */
    private static double doAction(Request request) {
        double result = request.getValue();

        ActionType actionType = request.getActionType();
        ArithmeticAction action = actionType.newActionInstance();

        result = action.executeAsDouble(new ActionContext(request.getLastValue(), request.getValue()));
        if (!Double.isNaN(result) && Double.isFinite(result) && (result < 1)) {
            // Round until the 15th digit right to the floating point, in order to
            // round sin(pi) to 0, and not -1.2246467991473532E-16. (Because Math.PI keeps 16 digits only)
            result = Math.rint(1e15 * result) / 1e15;
        }

        return result;
    }

    private Request readHttpRequest(String request) throws IOException {
        String requestLower = request.toLowerCase();
        if (!requestLower.contains("get")) {
            throw new WebException(HttpStatus.METHOD_NOT_ALLOWED, "Use GET only");
        }

        if (requestLower.contains("favicon.ico")) {
            throw new FavIconException(); // ClientHandler will write favicon.ico file
        }

        // Input first line should look like: GET /calc?value=5&lastValue=2&action=MINUS HTTP/1.1
        String requestLine = request;
        int indexOfNewLine = request.indexOf('\n');
        if (indexOfNewLine > 0) {
            requestLine = request.substring(0, indexOfNewLine).trim();
        }
        String[] queryParams = requestLine.split(" ");
        queryParams = queryParams[1].split("\\?");
        if (queryParams.length != 2) {
            throw new WebException(HttpStatus.BAD_REQUEST, "Missing query parameters. Was: " + request);
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

            if (nameAndValue[0].equalsIgnoreCase(VALUE_QUERY_PARAM)) {
                value = getDouble(nameAndValue[1], VALUE_QUERY_PARAM);
            } else if (nameAndValue[0].equalsIgnoreCase(LAST_VALUE_QUERY_PARAM)) {
                lastValue = getDouble(nameAndValue[1], LAST_VALUE_QUERY_PARAM);
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

    private double getDouble(String valueToParse, String paramName) throws WebException {
        double value;
        try {
            value = Double.parseDouble(valueToParse);
        } catch (NumberFormatException e) {
            throw new WebException(HttpStatus.BAD_REQUEST, "Illegal query parameter. '" + paramName + "' must be of type double. Was: " + valueToParse);
        }
        return value;
    }
}

