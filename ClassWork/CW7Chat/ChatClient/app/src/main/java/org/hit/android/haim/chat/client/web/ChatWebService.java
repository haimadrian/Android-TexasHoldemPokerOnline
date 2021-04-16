package org.hit.android.haim.chat.client.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Haim Adrian
 * @since 14-Apr-21
 */
public class ChatWebService {
    private static final ChatWebService instance = new ChatWebService();

    private final APIInterface api;
    private final ObjectMapper objectMapper;

    private ChatWebService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        objectMapper = new ObjectMapper();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.10:8080")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(client)
                .build();

        api = retrofit.create(APIInterface.class);
    }

    public static ChatWebService getInstance() {
        return instance;
    }

    public APIInterface getApi() {
        return api;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
