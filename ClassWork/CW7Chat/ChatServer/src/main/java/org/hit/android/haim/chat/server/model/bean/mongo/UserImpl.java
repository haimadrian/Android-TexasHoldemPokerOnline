package org.hit.android.haim.chat.server.model.bean.mongo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.android.haim.chat.server.model.bean.Gender;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * A user model that we persist to database.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Document(collection = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserImpl {
    @Id
    private String id;

    private String name;

    @JsonDeserialize(using = LocalDateDeserializer.class) // Date format is: yyyy-MM-dd. e.g. 1995-08-30
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfBirth;

    private Gender gender;

    private String channelName;

}


