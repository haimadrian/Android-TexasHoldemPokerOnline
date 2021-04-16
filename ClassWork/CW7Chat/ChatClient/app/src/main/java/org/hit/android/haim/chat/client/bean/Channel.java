package org.hit.android.haim.chat.client.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A channel in chat
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Channel {
    private String name;

    @Builder.Default
    private List<String> users = new ArrayList<>();

    @Builder.Default
    private List<String> messages = new ArrayList<>();

    @Builder.Default
    private boolean isDeletable = true;
}

