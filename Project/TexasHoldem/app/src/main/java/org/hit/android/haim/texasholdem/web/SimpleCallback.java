package org.hit.android.haim.texasholdem.web;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * Implement a simple callback such that we do not have to implement the {@link #onFailure(Call, Throwable)} over and over.<br/>
 * This lets us log the error, and handle the {@link #onResponse(Call, Response)} only.
 * @author Haim Adrian
 * @since 01-May-21
 */
public abstract class SimpleCallback<T> implements Callback<T> {
    private static final String LOGGER = SimpleCallback.class.getSimpleName();

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        Log.e(LOGGER, "Error has occurred while sending HTTP request: " + call.request().toString(), t);
    }
}
