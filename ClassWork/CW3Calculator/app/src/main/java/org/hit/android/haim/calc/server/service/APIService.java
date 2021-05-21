package org.hit.android.haim.calc.server.service;

import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.server.Response;
import org.hit.android.haim.calc.server.User;

import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 21-May-21
 */
public interface APIService {
    void signIn(String email, String pwd, Consumer<Response> responseConsumer);
    void signUp(User user, String pwd, Consumer<Response> responseConsumer);
    void executeCalculatorAction(double value, double lastVal, ActionType actionType, Consumer<Response> responseConsumer);
    void disconnect();
    User getCurrentUser();
}
