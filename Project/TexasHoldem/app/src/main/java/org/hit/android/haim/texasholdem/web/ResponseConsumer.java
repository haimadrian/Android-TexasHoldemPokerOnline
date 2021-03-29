package org.hit.android.haim.texasholdem.web;

import java.util.function.Consumer;

/**
 * As we use an IO thread to perform HTTP requests, we need a consumer to let users of
 * {@link TexasHoldemWebService} to get the response (either success or failure) asynchronously.<br/>
 * Implement this interface to get the response.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public interface ResponseConsumer<T> extends Consumer<T> {
    /**
     * This method is invoked by {@link TexasHoldemWebService} once a request is successfully finished.<br/>
     * Implement this method to continue doing other tasks when the request is done
     */
    void onSuccess(T response);

    @Override
    default void accept(T t) {
        onSuccess(t);
    }

    /**
     * This method is invoked by {@link TexasHoldemWebService} once a request is finished with an error.<br/>
     * Implement this method to respond to failures
     * @param thrown The error
     */
    void onError(Throwable thrown);
}
