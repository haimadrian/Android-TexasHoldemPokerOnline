package org.hit.android.haim.texasholdem.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A user model that we use for communicating with server.
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "pwd", "image" })
@Builder(toBuilder = true)
public class User {
   /**
    * A unique user identifier. (email address)<br/>
    * Mandatory for both sign up and sign in
    */
   private String id;

   /**
    * Password to use for authorizing user.<br/>
    * Mandatory for both sign up and sign in
    */
   @Setter(AccessLevel.PRIVATE)
   @Getter(AccessLevel.PRIVATE)
   private char[] pwd;

   /**
    * A nickname of the user.<br/>
    * It is set when user sign up, and retrieved from server when signing in.
    */
   private String name;

   /**
    * Date of birth of a user.<br/>
    * It is set when user sign up, and retrieved from server when signing in.
    */
   @JsonDeserialize(using = LocalDateDeserializer.class) // Date format is: yyyy-MM-dd. e.g. 1995-08-30
   @JsonSerialize(using = LocalDateSerializer.class)
   private LocalDate dateOfBirth;

   /**
    * How much coins a user have.<br/>
    * Updated when user purchase coins or earn/lose them while playing.
    */
   private long coins;

   /**
    * Profile image of user
    */
   private byte[] image;

   /**
    * Constructs a new {@link User} using id and password<br/>
    * Use this ctor for signing in a user
    */
   public User(String id, char[] pwd) {
      this(id, pwd, null, null, 0, null);
   }

   /**
    * Use this overload to get the image scaled to the ImageView dimensions.<br/>
    * For example: <code>imageView.setImageBitmap(user.getImageScaled(imageView.getWidth(), imageView.getHeight()));</code>
    * @param width The width to scale to
    * @param height The height to scale to
    * @return A scaled bitmap
    */
   public Bitmap getImageScaled(int width, int height) {
      Bitmap bmp = getImageBitmap();
      if (bmp != null) {
         return Bitmap.createScaledBitmap(bmp, width, height, false);
      }

      return null;
   }

   /**
    * @see #getImageScaled(int, int)
    */
   public Bitmap getImageBitmap() {
      if (image != null) {
         return BitmapFactory.decodeByteArray(image, 0, image.length);
      }

      return null;
   }

   public void setImageBitmap(Bitmap bmp) {
      if (bmp != null) {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
         this.image = stream.toByteArray();
      }
   }
}

