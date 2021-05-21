package org.hit.android.haim.calc.server.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.server.Response;
import org.hit.android.haim.calc.server.User;

import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 21-May-21
 */
public class APIServiceFirebaseImpl implements APIService {
    private final Gson gson;
    private final FirebaseAuth mAuth;

    public APIServiceFirebaseImpl() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient(); // Set it to free-style so we will be able to get raw strings from server. (e.g. "Bye" when signing out, instead of { "msg" : "Bye" } )
        gson = gsonBuilder.create();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void signIn(String email, String pwd, Consumer<Response> responseConsumer) {
        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignIn", "signInWithEmail:success");

                        // Update UI
                        User user = getCurrentUser();
                        responseConsumer.accept(new Response(200, "Welcome, " + user.getDisplayName(), null));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignIn", "signInWithEmail:failure", task.getException());
                        responseConsumer.accept(new Response(500, "", "Authentication failed."));
                    }
                });
    }

    @Override
    public void signUp(User user, String pwd, Consumer<Response> responseConsumer) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignUp", "createUserWithEmail:success");

                        // Update UI
                        FirebaseUser usr = mAuth.getCurrentUser();
                        if (usr != null) {
                            // Now set display name and photo
                            usr.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(user.getDisplayName()).setPhotoUri(user.getPhotoUrl()).build());
                        } else {
                            Log.e("SignUp", "createUserWithEmail: currentUser is null");
                        }
                        responseConsumer.accept(new Response(200, "Welcome, " + getCurrentUser().getDisplayName(), null));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignUp", "createUserWithEmail:failure", task.getException());
                        responseConsumer.accept(new Response(500, "", "Authentication failed."));
                    }
                });
    }

    @Override
    public void executeCalculatorAction(double value, double lastVal, ActionType actionType, Consumer<Response> responseConsumer) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public User getCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser == null ? null : new User(currentUser.getEmail(), currentUser.getDisplayName() == null ? currentUser.getEmail() : currentUser.getDisplayName(), currentUser.getPhotoUrl());
    }
}
