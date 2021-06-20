package org.hit.android.haim.texasholdem.model.chat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import org.hit.android.haim.texasholdem.model.User;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A message model that we use in game chat.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Message {
    private String id;

    private String message;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // Date format is: yyyy-MM-dd. e.g. 1995-08-30
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateTimeSent;

    private Channel channel;

    private User user;
}


