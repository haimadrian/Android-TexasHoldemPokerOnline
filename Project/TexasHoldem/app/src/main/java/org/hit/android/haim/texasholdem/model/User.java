package org.hit.android.haim.texasholdem.model;

import java.time.LocalDate;

/**
 * A user model that we use for communicating with server.
 * @author Haim Adrian
 * @since 21-Mar-21
 */
public class User {
   private String id;

   private char[] pwd;

   private String name;

   private LocalDate dateOfBirth;

   private long coins;

   /**
    * Constructs a new {@link User}.
    */
   public User() {

   }

   /**
    * Constructs a new {@link User} using id
    */
   public User(String id) {
      this(id, null);
   }

   /**
    * Constructs a new {@link User} using id and password<br/>
    * Use this ctor for signing in a user
    */
   public User(String id, char[] pwd) {
      this(id, pwd, null, null, 0);
   }

   /**
    * Constructs a new {@link User} using id and password<br/>
    * Use this ctor for signing up a user
    */
   public User(String id, char[] pwd, String name, LocalDate dateOfBirth, long coins) {
      this.id = id;
      this.pwd = pwd;
      this.name = name;
      this.dateOfBirth = dateOfBirth;
      this.coins = coins;
   }

   public String getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public LocalDate getDateOfBirth() {
      return dateOfBirth;
   }

   public void setDateOfBirth(LocalDate dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
   }

   public long getCoins() {
      return coins;
   }

   public void setCoins(long coins) {
      this.coins = coins;
   }

   @Override
   public String toString() {
      return "User{" +
              "id='" + id + '\'' +
              ", name='" + name + '\'' +
              ", dateOfBirth=" + dateOfBirth +
              ", coins=" + coins +
              '}';
   }
}

