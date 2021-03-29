package org.hit.android.haim.texasholdem.web;

/**
 * As we use an IO thread to perform HTTP requests, we need a consumer to let users of
 * {@link TexasHoldemWebService} to get notified upon request success/failure.<br/>
 * Implement this interface to get notified.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public interface VoidResponseConsumer {
    /**
     * This method is invoked by {@link TexasHoldemWebService} once a request is successfully finished.<br/>
     * Implement this method to continue doing other tasks when the request is done
     */
    void onSuccess();

    /**
     * This method is invoked by {@link TexasHoldemWebService} once a request is finished with an error.<br/>
     * Implement this method to respond to failures
     * @param thrown The error
     */
    void onError(Throwable thrown);
}
