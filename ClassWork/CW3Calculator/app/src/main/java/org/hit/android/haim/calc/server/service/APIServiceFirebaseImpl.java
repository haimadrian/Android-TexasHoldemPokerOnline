package org.hit.android.haim.calc.server.service;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.GsonBuilder;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.action.ArithmeticAction;
import org.hit.android.haim.calc.server.Response;
import org.hit.android.haim.calc.server.User;

import java.util.function.Consumer;

/**
 * @author Haim Adrian
 * @since 21-May-21
 */
public class APIServiceFirebaseImpl implements APIService {
    private final FirebaseAuth mAuth;


    public APIServiceFirebaseImpl() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient(); // Set it to free-style so we will be able to get raw strings from server. (e.g. "Bye" when signing out, instead of { "msg" : "Bye" } )
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
                            writeUserToDb(usr.getUid(), user);
                        } else {
                            Log.e("SignUp", "createUserWithEmail: currentUser is null");
                        }
                        responseConsumer.accept(new Response(200, "Welcome, " + user.getDisplayName(), null));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignUp", "createUserWithEmail:failure", task.getException());
                        responseConsumer.accept(new Response(500, "", "Authentication failed."));
                    }
                });
    }

    @Override
    public void executeCalculatorAction(double value, double lastVal, ActionType actionType, Consumer<Response> responseConsumer) {
        Log.d("Action", "Executing action locally, through Firebase impl");
        ArithmeticAction action = actionType.newActionInstance();
        double result = action.executeAsDouble(new ActionContext(lastVal, value));
        responseConsumer.accept(new Response(200, String.valueOf(result), null));
    }

    @Override
    public void disconnect() {

    }

    @Override
    public User getCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser == null ? null : new User(currentUser.getEmail(), currentUser.getDisplayName() == null ? currentUser.getEmail() : currentUser.getDisplayName(), null, currentUser.getPhotoUrl());
    }

    private void writeUserToDb(String uid, User user) {
        try {
            Log.d("DataWrite", "Writing user with id " + uid + ": " + user);
            FirebaseDatabase database = FirebaseDatabase.getInstance(); // The root
            DatabaseReference myRef = database.getReference("user"); // Child. (Table. e.g. User or History)
            myRef.child(uid).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("DataWrite", "createUserWithData:success");
                } else {
                    Log.w("DataWrite", "createUserWithData:failure", task.getException());
                }
            });

            final Handler handler = new Handler();
            handler.postDelayed(this::readUserFromDb, 2000);
            //database.getReference("testRef").child("aChild").setValue("aValue");
            //database.getReference("calculator-10996-default-rtdb").child("test").setValue("SomeValue");
        } catch (Exception e) {
            Log.e("DataWrite", "Error has occurred while trying to save to realtime database", e);
        }
    }

    private void readUserFromDb() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // The root
        DatabaseReference myRef = database.getReference("user"); // Child. (Table. e.g. User or History)

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                User value = dataSnapshot.getValue(User.class);
                Log.d("DataRead", "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("DataRead", "Failed to read value.", error.toException());
            }
        });
    }

    public void saveValueToDatabase(String value) {
        /*
        Example of data:
        root
          -user
            -id
              -name
              -email
              -phone
         */
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // The root
        DatabaseReference myRef = database.getReference("history"); // Child. (Table. e.g. User or History)
        myRef.setValue(value);
    }
}
