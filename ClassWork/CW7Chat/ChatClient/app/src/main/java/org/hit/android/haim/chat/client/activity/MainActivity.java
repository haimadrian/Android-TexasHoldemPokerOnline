package org.hit.android.haim.chat.client.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.chat.client.R;
import org.hit.android.haim.chat.client.bean.Channel;
import org.hit.android.haim.chat.client.bean.User;
import org.hit.android.haim.chat.client.fragment.FragmentChannel;
import org.hit.android.haim.chat.client.fragment.FragmentConnect;
import org.hit.android.haim.chat.client.fragment.FragmentLobby;
import org.hit.android.haim.chat.client.web.ChatWebService;

import java.io.IOException;
import java.time.LocalDate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES_NAME = "chat-shared";
    private static final String STORED_EMAIL_KEY = "chat-email";
    private static final String STORED_NAME_KEY = "chat-name";
    private static final String STORED_DATE_KEY = "chat-date";
    private static final String STORED_GENDER_KEY = "chat-gender";

    private FragmentManager fragmentManager;

    private ImageView imageViewDisconnect;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure it to allow the main thread to perform network operations.
        // We connect/disconnect using main thread to block it.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        imageViewDisconnect = findViewById(R.id.imageViewDisconnect);
        imageViewDisconnect.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString(STORED_EMAIL_KEY, "");
        String name = sharedPreferences.getString(STORED_NAME_KEY, "");
        String dateOfBirth = sharedPreferences.getString(STORED_DATE_KEY, "");
        User.Gender gender = User.Gender.valueOf(sharedPreferences.getString(STORED_GENDER_KEY, "Male"));

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentConnect fragmentConnect = new FragmentConnect(email, name, dateOfBirth, gender);
        fragmentTransaction.add(R.id.contentLayout, fragmentConnect).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (user != null) {
            storeUserToSharedPref();

            doDisconnect();
        }
    }

    private void storeUserToSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
        sharedPrefsEditor.putString(STORED_EMAIL_KEY, user.getId());
        sharedPrefsEditor.putString(STORED_NAME_KEY, user.getName());
        sharedPrefsEditor.putString(STORED_GENDER_KEY, user.getGender().name());
        sharedPrefsEditor.putString(STORED_DATE_KEY, user.getDateOfBirth().toString());
        sharedPrefsEditor.apply();
    }

    public User getUser() {
        return user;
    }

    public void onHomeClicked(View view) {
        if (user != null) {
            // Make sure we do not replace Lobby with itself
            Fragment lobbyFragment = fragmentManager.findFragmentByTag("LobbyFragment");
            if ((lobbyFragment == null) || !lobbyFragment.isVisible()) {
                doDisconnect();

                FragmentLobby fragmentLobby = new FragmentLobby();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.contentLayout, fragmentLobby, "LobbyFragment").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                Toast.makeText(this, "Welcome to Lobby, " + user.getName() + "!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onDisconnectClicked(View view) {
        imageViewDisconnect.setVisibility(View.GONE);
        FragmentConnect fragmentConnect;

        if (user != null) {
            fragmentConnect = new FragmentConnect(user.getId(), user.getName(), user.getDateOfBirth().toString(), user.getGender());
            user = null;
        } else {
            fragmentConnect = new FragmentConnect();
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contentLayout, fragmentConnect).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    public void doConnect(String email, String name, String dateOfBirth, User.Gender gender) {
        user = new User(email, name, LocalDate.parse(dateOfBirth), gender, new Channel());
        storeUserToSharedPref();

        ChatWebService.getInstance().getApi().connect(user).enqueue(new Callback<JsonNode>() {
            @Override
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    serverErrorHandler(MainActivity.this.findViewById(R.id.mainContent), response);
                } else {
                    try {
                        user = ChatWebService.getInstance().getObjectMapper().readValue(response.body().toString(), User.class);
                        imageViewDisconnect.setVisibility(View.VISIBLE);
                        onHomeClicked(null);
                    } catch (JsonProcessingException e) {
                        Log.e("Connect", "Error has occurred while trying to connect", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Log.e("Connect", "Error has occurred while trying to create", t);
                Snackbar.make(MainActivity.this.findViewById(R.id.mainContent), "Something went wrong while connecting " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void doReconnect() {
        if (user != null) {
            try {
                Response<JsonNode> response = ChatWebService.getInstance().getApi().connect(user).execute();
                user = ChatWebService.getInstance().getObjectMapper().readValue(response.body().toString(), User.class);
            } catch (IOException e) {
                Log.e("Reconnect", e.getMessage(), e);
            }
        }
    }

    public void doDisconnect() {
        if (user != null) {
            // TODO: Use save instance state so we can reconnect later
            try {
                ChatWebService.getInstance().getApi().disconnect(user.getId()).execute();
            } catch (Exception e) {
                Log.e("Disconnect", e.getMessage(), e);
            }
        }
    }

    public void joinToChannel(Channel channel) {
        user.getChannel().setName(channel.getName());
        ChatWebService.getInstance().getApi().connect(user).enqueue(new Callback<JsonNode>() {
            @Override
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    serverErrorHandler(MainActivity.this.findViewById(R.id.mainContent), response);
                } else {
                    try {
                        user = ChatWebService.getInstance().getObjectMapper().readValue(response.body().toString(), User.class);

                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.contentLayout, new FragmentChannel(channel.getName())).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    } catch (JsonProcessingException e) {
                        Log.e("Connect", "Error has occurred while trying to connect", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Log.e("Connect", "Error has occurred while trying to create", t);
                Snackbar.make(MainActivity.this.findViewById(R.id.mainContent), "Something went wrong while connecting " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void serverErrorHandler(View view, Response<JsonNode> response) {
        try {
            String error = response.errorBody().string();
            Snackbar.make(view, error, Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("Error", "Error has occurred while reading response error as string", e);
        }
    }
}