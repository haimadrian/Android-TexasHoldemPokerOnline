package org.hit.android.haim.texasholdem.web;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.hit.android.haim.texasholdem.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.tls.HandshakeCertificates;

/**
 * This singleton class responsible for the communication with Map Stories backend.<br/>
 * Remember to call {@link #init(Context)} before using his class!
 * @author Haim Adrian
 * @since 23-Mar-21
 */
public class TexasHoldemWebService {
    private static final String BACKEND_URL = "https://vm-h-ds.westeurope.cloudapp.azure.com:8443/";
    private static final String AUTH_HEADER = "Authorization";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd";
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final TexasHoldemWebService instance = new TexasHoldemWebService();

    private final UserService userService;
    private final CoordinateService coordinateService;
    private final StoryService storyService;

    /**
     * Maintain a thread pool to perform requests asynchronously.<br/>
     * The thread pool will consist of a single IO thread to perform HTTP requests without blocking the main thread.
     */
    private final ExecutorService executor;

    private OkHttpClient httpClient;
    private final Gson gson;
    // TODO: Remove this when we have a sign in dialog.
    private String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWVzdCIsInVzZXJOYW1lIjoiR3Vlc3QifQ.DsZJPGIgnsX0H519JBi05dgsiMwPgcO4nCpaLzrJhDk";

    private TexasHoldemWebService() {
        userService = new UserService();
        coordinateService = new CoordinateService();
        storyService = new StoryService();

        // An IO thread
        executor = Executors.newSingleThreadExecutor();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(JSON_DATE_FORMAT);
        gsonBuilder.setLenient();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(byte[].class, new ByteArrayAdapter());
        gson = gsonBuilder.create();
    }

    /**
     * @return The unique instance of {@link TexasHoldemWebService}
     */
    public static TexasHoldemWebService getInstance() {
        return instance;
    }

    /**
     * Call this method from application class to initialize Map Stories service.<br/>
     * We will read the certificate resource and initialize the http client and gson.
     * @param context Context to get resource from
     */
    public void init(Context context) {
        // Make sure we do this once.
        if (httpClient == null) {
            try {
                InputStream serverCert = context.getResources().openRawResource(R.raw.server);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) certificateFactory.generateCertificates(serverCert).iterator().next();

                HandshakeCertificates certificates = new HandshakeCertificates.Builder()
                        .addTrustedCertificate(cert)
                        .addPlatformTrustedCertificates()
                        .build();

                httpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
                        .hostnameVerifier(((hostname, session) -> true)) // Trust all hosts.
                        .build();
            } catch (Throwable t) {
                Log.d("Security", "Error has occurred while trying to add custom trust.", t);
                httpClient = new OkHttpClient.Builder().build();
            }
        }
    }

    /**
     * @return {@link UserService}
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * @return {@link CoordinateService}
     */
    public CoordinateService getCoordinateService() {
        return coordinateService;
    }

    /**
     * @return {@link StoryService}
     */
    public StoryService getStoryService() {
        return storyService;
    }

    OkHttpClient getHttpClient() {
        return httpClient;
    }

    Gson getGson() {
        return gson;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    /**
     * A helper method used to create a URL path out of base backend address, with the specified path,
     * and optional query parameters.<br/>
     * You should pass query parameter as key and value pairs. For example:<br/>
     * <code>buildUrl("coordinate/dist", "lat", latValue, "lng", lngValue, "dist", distance);</code><br/>
     * Result is: <code>BACKEND_URL/coordinate/dist?lat={latValue}&lng={lngValue}&dist={distance}</code>
     * @param path The path to append to the backend url
     * @param queryParams Optional query parameters
     * @return A url as string
     */
    static String buildUrl(String path, Object[] queryParams) {
        StringBuilder url = new StringBuilder(BACKEND_URL).append(path);
        if ((queryParams != null) && (queryParams.length >= 2)) {
            url.append("?");
            for (int i = 0; i < queryParams.length; i+=2) {
                if (i > 0) {
                    url.append("&");
                }

                url.append(queryParams[i]).append("=").append(queryParams[i+1]);
            }
        }
        return url.toString();
    }

    /**
     * Helper method to execute a GET request
     * @param path The url path. e.g. "coordinate/dist"
     * @param isAuth Whether the request should use the authorization header or not
     * @param responseType The class to create out of the json response from server
     * @param responseConsumer A reference to consume the response
     * @param queryParams Optional query parameters. See {@link #buildUrl(String, Object[])}
     */
    <ResponseType> void get(String path, boolean isAuth, Class<ResponseType> responseType, ResponseConsumer<ResponseType> responseConsumer, Object... queryParams) {
        Request.Builder request = new Request.Builder().get().url(buildUrl(path, queryParams));
        if (isAuth) {
            request.addHeader(AUTH_HEADER, "Bearer " + jwtToken);
        }

        executeRequest(request, json -> TexasHoldemWebService.getInstance().getGson().fromJson(json, responseType), responseConsumer);
    }

    /**
     * Helper method to execute a GET request
     * @param path The url path. e.g. "coordinate/dist"
     * @param isAuth Whether the request should use the authorization header or not
     * @param responseType Type info, to support getting collection as a result
     * @param responseConsumer A reference to consume the response
     * @param queryParams Optional query parameters. See {@link #buildUrl(String, Object[])}
     */
    <ResponseType> void get(String path, boolean isAuth, Type responseType, ResponseConsumer<ResponseType> responseConsumer, Object... queryParams) {
        Request.Builder request = new Request.Builder().get().url(buildUrl(path, queryParams));
        if (isAuth) {
            request.addHeader(AUTH_HEADER, "Bearer " + jwtToken);
        }

        executeRequest(request, json -> TexasHoldemWebService.getInstance().getGson().fromJson(json, responseType), responseConsumer);
    }

    /**
     * Helper method to execute a PUT request
     * @param path The url path. e.g. "coordinate/dist"
     * @param isAuth Whether the request should use the authorization header or not
     * @param body Optional body for the PUT request
     * @param responseType The class to create out of the json response from server
     * @param responseConsumer A reference to consume the response
     * @param queryParams Optional query parameters. See {@link #buildUrl(String, Object[])}
     */
    <BodyType, ResponseType> void put(String path, boolean isAuth, BodyType body, Class<ResponseType> responseType, ResponseConsumer<ResponseType> responseConsumer, Object... queryParams) {
        RequestBody requestBody = RequestBody.create(body != null ? gson.toJson(body) : "", APPLICATION_JSON);
        Request.Builder request = new Request.Builder().put(requestBody).url(buildUrl(path, queryParams));
        if (isAuth) {
            request.addHeader(AUTH_HEADER, "Bearer " + jwtToken);
        }

        executeRequest(request, json -> TexasHoldemWebService.getInstance().getGson().fromJson(json, responseType), responseConsumer);
    }

    /**
     * Helper method to execute a PUT request
     * @param path The url path. e.g. "coordinate/dist"
     * @param isAuth Whether the request should use the authorization header or not
     * @param body Optional body for the PUT request
     * @param responseType Type info, to support getting collection as a result
     * @param responseConsumer A reference to consume the response
     * @param queryParams Optional query parameters. See {@link #buildUrl(String, Object[])}
     */
    <BodyType, ResponseType> void put(String path, boolean isAuth, BodyType body, Type responseType, ResponseConsumer<ResponseType> responseConsumer, Object... queryParams) {
        RequestBody requestBody = RequestBody.create(body != null ? gson.toJson(body) : "", APPLICATION_JSON);
        Request.Builder request = new Request.Builder().put(requestBody).url(buildUrl(path, queryParams));
        if (isAuth) {
            request.addHeader(AUTH_HEADER, "Bearer " + jwtToken);
        }

        executeRequest(request, json -> TexasHoldemWebService.getInstance().getGson().fromJson(json, responseType), responseConsumer);
    }

    /**
     * Helper method to execute a POST request
     * @param path The url path. e.g. "coordinate/dist"
     * @param isAuth Whether the request should use the authorization header or not
     * @param body Optional body for the POST request
     * @param responseType The class to create out of the json response from server
     * @param responseConsumer A reference to consume the response
     * @param queryParams Optional query parameters. See {@link #buildUrl(String, Object[])}
     */
    <BodyType, ResponseType> void post(String path, boolean isAuth, BodyType body, Type responseType, ResponseConsumer<ResponseType> responseConsumer, Object... queryParams) {
        RequestBody requestBody = RequestBody.create(body != null ? gson.toJson(body) : "", APPLICATION_JSON);
        Request.Builder request = new Request.Builder().post(requestBody).url(buildUrl(path, queryParams));
        if (isAuth) {
            request.addHeader(AUTH_HEADER, "Bearer " + jwtToken);
        }

        executeRequest(request, json -> TexasHoldemWebService.getInstance().getGson().fromJson(json, responseType), responseConsumer);
    }

    /**
     * Helper method to execute a POST request
     * @param path The url path. e.g. "coordinate/dist"
     * @param isAuth Whether the request should use the authorization header or not
     * @param body Optional body for the POST request
     * @param responseType Type info, to support getting collection as a result
     * @param responseConsumer A reference to consume the response
     * @param queryParams Optional query parameters. See {@link #buildUrl(String, Object[])}
     */
    <BodyType, ResponseType> void post(String path, boolean isAuth, BodyType body, Class<ResponseType> responseType, ResponseConsumer<ResponseType> responseConsumer, Object... queryParams) {
        RequestBody requestBody = RequestBody.create(body != null ? gson.toJson(body) : "", APPLICATION_JSON);
        Request.Builder request = new Request.Builder().post(requestBody).url(buildUrl(path, queryParams));
        if (isAuth) {
            request.addHeader(AUTH_HEADER, "Bearer " + jwtToken);
        }

        executeRequest(request, json -> TexasHoldemWebService.getInstance().getGson().fromJson(json, responseType), responseConsumer);
    }

    private <ResponseType> void executeRequest(Request.Builder request, Function<String, ResponseType> jsonConverter, ResponseConsumer<ResponseType> responseConsumer) {
        // Create a looper that will post the result to the caller thread.
        // The request is being executed using our IO thread, for HTTP.
        // The response will be handled by the calling thread.
        Looper currentLooper = Looper.myLooper();
        if (currentLooper == null) {
            currentLooper = Looper.getMainLooper();
        }
        Handler androidHandler = new Handler(currentLooper);

        executor.submit(() -> {
            ResponseType result = null;
            Throwable error = null;
            try (Response response = TexasHoldemWebService.getInstance().getHttpClient().newCall(request.build()).execute()) {
                if (!response.isSuccessful()) {
                    // When there is an unexpected state, the server will return the error message.
                    // Otherwise, just use the http status message
                    ResponseBody responseBody = response.body();
                    if (responseConsumer != null) {
                        error = new IOException(responseBody != null ? responseBody.string() : response.message());
                    }
                } else {
                    ResponseBody body = response.body();
                    if (body != null) {
                        result = jsonConverter.apply(body.string());
                    }
                }
            } catch (Throwable thrown) {
                error = thrown;
            }

            // If there is something to return to caller, handle it on the calling thread and not the IO thread.
            if ((responseConsumer != null) && ((error != null) || (result != null))) {
                androidHandler.post(new ResponseHandler<>(responseConsumer, result, error));
            }
        });
    }

    /**
     * A response handler to pass a response to a consumer using the calling looper.<br/>
     * As a result of performing HTTP requests using IO thread, such that we do not block a UI thread while we are working on IO,
     * we separate the task. The IO thread will make the HTTP call, get the response, and then let the UI thread consume it.<br/>
     * This way the IO thread does not block the UI thread while performing the request, and the UI thread does not block the IO
     * thread while consuming the response.
     * @param <ResponseType> Expected response type
     */
    private static class ResponseHandler<ResponseType> implements Runnable {
        private final ResponseType result;
        private final Throwable error;
        private final ResponseConsumer<ResponseType> responseConsumer;

        public ResponseHandler(@NonNull ResponseConsumer<ResponseType> responseConsumer, @Nullable ResponseType result, @Nullable Throwable error) {
            this.responseConsumer = responseConsumer;
            this.result = result;
            this.error = error;
        }

        @Override
        public void run() {
            try {
                if (error != null) {
                    responseConsumer.onError(error);
                } else if (result != null) {
                    responseConsumer.onSuccess(result);
                }
            } catch (Exception e) {
                Log.e("WEB-Response-Handler", "Error has occurred while consuming HTTP response: " + e.getMessage() + " [error=" + error + ", result=" + result + "]", e);
            }
        }
    }

    /**
     * An adapter to serialize/deserialize LocalDate field into/from json.<br/>
     * We register it to the Gson reference.<br/>
     * Server expects a string, while Gson default behavior is a compound object for LocalDate.
     */
    static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
            if (localDate == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(localDate.toString());
            }
        }

        @Override
        public LocalDate read(final JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            } else {
                return LocalDate.parse(jsonReader.nextString());
            }
        }
    }

    /**
     * An adapter to serialize/deserialize byte[] field into/from json.<br/>
     * We register it to the Gson reference.<br/>
     * Server expects a string, while Gson default behavior is a compound object for array.
     */
    static class ByteArrayAdapter extends TypeAdapter<byte[]> {
        @Override
        public void write(final JsonWriter jsonWriter, final byte[] byteArray) throws IOException {
            if (byteArray == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(new String(byteArray));
            }
        }

        @Override
        public byte[] read(final JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            } else {
                return jsonReader.nextString().getBytes();
            }
        }
    }
}
