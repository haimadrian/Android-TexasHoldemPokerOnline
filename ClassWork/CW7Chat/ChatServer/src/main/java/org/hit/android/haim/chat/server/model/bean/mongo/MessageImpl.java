package org.hit.android.haim.chat.server.model.bean.mongo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * A message model that we persist to database.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Document(collection = "Message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MessageImpl {
    @Id
    private String id;

    private String message;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // Date format is: yyyy-MM-dd. e.g. 1995-08-30
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateTimeSent;

    private String channelName;

    private String userId;
}


