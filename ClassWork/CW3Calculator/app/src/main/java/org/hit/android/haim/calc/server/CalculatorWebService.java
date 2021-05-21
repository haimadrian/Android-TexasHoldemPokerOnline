package org.hit.android.haim.calc.server;

import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.server.service.APIService;
import org.hit.android.haim.calc.server.service.APIServiceFirebaseImpl;
import org.hit.android.haim.calc.server.service.APIServiceSocketImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Web service singleton, that delegates calls to the underlying implementation,
 * which can be the local server socket, or a Firebase app.
 * @author Haim Adrian
 * @since 11-Apr-21
 * @see APIServiceFirebaseImpl
 * @see APIServiceSocketImpl
 */
public class CalculatorWebService implements APIService {
    private static final CalculatorWebService instance = new CalculatorWebService();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final APIService service;

    private CalculatorWebService() {
        //service = new APIServiceSocketImpl(); // Work with local server socket
        service = new APIServiceFirebaseImpl(); // Work with Firebase
    }

    public static CalculatorWebService getInstance() {
        return instance;
    }

    @Override
    public void signIn(String email, String pwd, Consumer<Response> responseConsumer) {
        executor.submit(() -> service.signIn(email, pwd, responseConsumer));
    }

    @Override
    public void signUp(User user, String pwd, Consumer<Response> responseConsumer) {
        executor.submit(() -> service.signUp(user, pwd, responseConsumer));
    }

    @Override
    public void executeCalculatorAction(double value, double lastVal, ActionType actionType, Consumer<Response> responseConsumer) {
        executor.submit(() -> service.executeCalculatorAction(value, lastVal, actionType, responseConsumer));
    }

    @Override
    public void disconnect() {
        executor.submit(service::disconnect);
    }

    @Override
    public User getCurrentUser() {
        return service.getCurrentUser();
    }
}
