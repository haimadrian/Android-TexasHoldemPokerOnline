package org.hit.android.haim.calc.server;

import android.net.Uri;

/**
 * @author Haim Adrian
 * @since 21-May-21
 */
public class User {
    private String email;
    private String displayName;
    private Uri photoUrl;

    public User() {
    }

    public User(String email, String displayName, Uri photoUrl) {
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }
}
