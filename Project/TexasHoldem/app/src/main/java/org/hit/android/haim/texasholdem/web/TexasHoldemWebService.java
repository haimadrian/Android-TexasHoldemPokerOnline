package org.hit.android.haim.texasholdem.web;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.web.services.ChatService;
import org.hit.android.haim.texasholdem.web.services.GameService;
import org.hit.android.haim.texasholdem.web.services.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.tls.HandshakeCertificates;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * This singleton class responsible for the communication with Texas Holdem backend.<br/>
 * Remember to call {@link #init(Context)} before using his class, so we will configure security.
 * @author Haim Adrian
 * @since 23-Mar-21
 */
public class TexasHoldemWebService {
    private static final String LOGGER = TexasHoldemWebService.class.getSimpleName();

    //private static final String BACKEND_URL = "https://vm-h-ds.westeurope.cloudapp.azure.com:8443";
    private static final String BACKEND_URL = "https://192.168.0.5:8443";

    /** @see UserService */
    @Getter
    private UserService userService;

    /** @see GameService */
    @Getter
    private GameService gameService;

    /** @see ChatService */
    @Getter
    private ChatService chatService;

    /**
     * Keep a reference to Jackson {@link ObjectMapper} so we will be able to expose utilities for
     * serializing/deserializing json to bean and vice versa
     */
    @Getter
    private ObjectMapper objectMapper;

    /**
     * When user is signed in, we get a jwt token from backend and keep it so it will be added automatically
     * as Authorization header to every request.<br/>
     * This way the server can authorize us after signing in, and we keep it transparent for user.
     * @see AuthorizationHeaderInterceptor
     */
    @Getter
    @Setter
    private String jwtToken;

    /**
     * What user is currently logged in, so we will be able to show details of this user
     * wherever we need in the app.
     */
    @Getter
    @Setter
    private String loggedInUserId;

    // Hide creation of this class as it is a singleton
    private TexasHoldemWebService() { }

    /**
     * @return The unique instance of {@link TexasHoldemWebService}
     */
    public static TexasHoldemWebService getInstance() {
        return TexasHoldemWebServiceHolder.INSTANCE;
    }

    /**
     * Call this method from application class to initialize Texas Holdem service.<br/>
     * We will read the certificate resource and initialize the http client with it.
     * @param context Context to get resource from
     */
    public void init(Context context) {
        // Make sure we do this once.
        if (userService == null) {
            objectMapper = new ObjectMapper();

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient httpClient;
            try {
                httpClient = createSecuredHttpClient(context, interceptor);
            } catch (Throwable t) {
                Log.d(LOGGER, "Error has occurred while trying to add custom trust.", t);
                httpClient = new OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .addInterceptor(new AuthorizationHeaderInterceptor())
                        .build();
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BACKEND_URL)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .client(httpClient)
                    .build();

            userService = retrofit.create(UserService.class);
            gameService = retrofit.create(GameService.class);
            chatService = retrofit.create(ChatService.class);
        }
    }

    @NonNull
    private OkHttpClient createSecuredHttpClient(Context context, HttpLoggingInterceptor interceptor) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream serverCert = context.getResources().openRawResource(R.raw.server);
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificates(serverCert).iterator().next();

        HandshakeCertificates certificates = new HandshakeCertificates.Builder()
                .addTrustedCertificate(cert)
                .addPlatformTrustedCertificates()
                .build();

        return new OkHttpClient.Builder()
                .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
                .hostnameVerifier(((hostname, session) -> true)) // Trust all hosts.
                .addInterceptor(interceptor)
                .addInterceptor(new AuthorizationHeaderInterceptor())
                .build();
    }

    /**
     * Extract error body from an http response
     * @param response The response to get error body from
     */
    public String readHttpErrorResponse(retrofit2.Response<?> response) {
        String errorMessage;

        try {
            ResponseBody responseBody = response.errorBody();
            if (responseBody == null) {
                errorMessage = response.message();
            } else {
                String errorBody = responseBody.string();
                try {
                    // Server returns an error as json in this format: {"timestamp":"2021-05-04T22:11:13.029+00:00","status":401,"error":"Unauthorized","message":"","path":"/user/haim/info"}
                    JsonNode jsonNode = TexasHoldemWebService.getInstance().getObjectMapper().readValue(errorBody, JsonNode.class);
                    int status = jsonNode.get("status").intValue();
                    String error = jsonNode.get("error").asText();
                    errorMessage = status + " " + error;
                    if (jsonNode.has("message")) {
                        String messageText = jsonNode.get("message").asText();
                        if (!messageText.isEmpty()) {
                            errorMessage += " - " + messageText;
                        }
                    }
                } catch (Exception e) {
                    errorMessage = errorBody;
                }
            }
        } catch (Exception e) {
            Log.w(LOGGER, "Error has occurred while reading response error as string: " + e);
            errorMessage = response.message();
        }

        return errorMessage;
    }

    /**
     * Interceptor to add the authorization header to all requests, automatically.<br/>
     * This way we do not have to add the jwtToken as a parameter to all of the methods in the
     * service classes.<br/>
     * Note that the Authorization header won't be added if token refers to null.
     */
    private class AuthorizationHeaderInterceptor implements Interceptor {
        private static final String AUTH_HEADER = "Authorization";
        private static final String BEARER_FORMAT = "Bearer %s";

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request.Builder requestBuilder  = chain.request().newBuilder();
            if ((jwtToken != null) && !jwtToken.isEmpty()) {
                requestBuilder.addHeader(AUTH_HEADER, String.format(BEARER_FORMAT, jwtToken));
            }

            return chain.proceed(requestBuilder.build());
        }
    }

    private static class TexasHoldemWebServiceHolder {
        private static final TexasHoldemWebService INSTANCE = new TexasHoldemWebService();
    }
}
