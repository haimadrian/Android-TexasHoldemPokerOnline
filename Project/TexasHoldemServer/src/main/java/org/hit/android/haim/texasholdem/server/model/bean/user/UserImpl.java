package org.hit.android.haim.texasholdem.server.model.bean.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * A user model that we use for communicating with client.
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"pwd", "image"})
@Builder
public class UserImpl implements User {
   /**
    * See {@link User#getId()}
    */
   @Getter
   @NonNull
   private String id;

   /**
    * The password of a user. We use it when signing/logging in a user.
    */
   @Getter
   @NonNull
   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   private char[] pwd;

   /**
    * See {@link User#getName()}
    */
   @Getter
   @NonNull
   private String name;

   /**
    * See {@link User#getDateOfBirth()}
    */
   @Getter
   @NonNull
   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   @JsonDeserialize(using = LocalDateDeserializer.class) // Date format is: yyyy-MM-dd. e.g. 1995-08-30
   @JsonSerialize(using = LocalDateSerializer.class)
   private LocalDate dateOfBirth;

   /**
    * See {@link User#getCoins()}
    */
   @Getter
   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   private long coins;

   /**
    * See {@link User#getCoins()}
    */
   @Getter
   @JsonInclude(JsonInclude.Include.NON_DEFAULT)
   private byte[] image;

   /**
    * Constructs a new {@link UserImpl}
    * @param id
    * @param name
    */
   public UserImpl(String id, String name) {
      this.id = id;
      this.name = name;
   }

   /**
    * Constructs a new {@link UserImpl}, copying all fields out of another user.<br/>
    * We use a copy constructor in order to convert client model to server model and vice versa
    * @param user The user to get fields from
    */
   public UserImpl(User user) {
      this.id = user.getId();
      this.name = user.getName();
      this.coins = user.getCoins();
      this.dateOfBirth = user.getDateOfBirth();
      this.image = user.getImage();
      // Do not copy key, so we will not return it to callers
   }

   /**
    * Build a token out of this user
    * @return A token to be used by authentication filter with JWT
    */
   public UsernamePasswordAuthenticationToken toAuthToken() {
      return new UsernamePasswordAuthenticationToken(id, new String(pwd), new ArrayList<>());
   }
}

