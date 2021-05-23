package org.hit.android.haim.texasholdem.server.model.bean.chat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.android.haim.texasholdem.server.model.bean.user.User;

import java.time.LocalDateTime;

/**
 * A message model which we use in order to send messages in the application chat.<br/>
 * Note that messages are not stored to database. They disappear once their corresponding channel (game) is closed.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Message {
    /**
     * The message a user sent
     */
    private String message;

    /**
     * Time when user sent the message
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateTimeSent;

    /**
     * To which channel the message was sent
     */
    private Channel channel;

    /**
     * The user sent the message
     */
    private User user;
}


