package org.hit.android.haim.chat.server.model.bean.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * A channel in chat
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Document(collection = "Channel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = { "users", "messages" })
public class Channel {
    @Id
    private String name;

    @Builder.Default
    private List<String> users = new ArrayList<>();

    @Builder.Default
    private List<String> messages = new ArrayList<>();

    @Builder.Default
    private boolean isDeletable = true;
}

