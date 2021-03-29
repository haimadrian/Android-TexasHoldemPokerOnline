package org.hit.android.haim.texasholdem.view.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A class that represents the logged in user.<br/>
 * This class is exposed to the UI, so the UI will not be aware of the model {@link org.hit.android.haim.texasholdem.model.User}
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class LoggedInUserView implements Serializable {
    private final String userId;
    private String nickName;
    private long coins;
    private LocalDate dateOfBirth;

    /**
     * Constructs a new {@link LoggedInUserView}
     * @param userId The user identifier
     * @param nickName A display name of the user, to show it
     * @param dateOfBirth Birth date of user
     * @param coins How many coins the user has
     */
    public LoggedInUserView(String userId, String nickName, LocalDate dateOfBirth, long coins) {
        this.userId = userId;
        this.nickName = nickName;
        this.dateOfBirth = dateOfBirth;
        this.coins = coins;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}