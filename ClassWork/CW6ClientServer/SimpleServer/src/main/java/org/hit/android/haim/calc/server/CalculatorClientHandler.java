package org.hit.android.haim.calc.server;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.action.ArithmeticAction;

import java.net.InetAddress;
import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 13-Apr-21
 */
public class CalculatorClientHandler implements RequestHandler {
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

    @Override
    public Response onRequest(InetAddress client, Request request, Consumer<Boolean> stopCommunication) {
        Response response;
        Boolean stopCommunicating = Boolean.TRUE;

        if ((request != null) && (request.getActionType() != ActionType.DISCONNECT)) {
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

        return response;
    }
}

