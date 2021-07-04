package org.hit.android.haim.texasholdem.common.model.bean.chat;

import lombok.*;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A channel in chat.<br/>
 * For each online game we have a dedicated channel, at which players can communicate with each other.<br/>
 * There cannot be &gt;1 channel for a game.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = { "messages" })
public class Channel {
    private String name;

    @Builder.Default
    private Set<Player> users = new HashSet<>();

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * Clear all messages in chat
     */
    public void clear() {
        messages.clear();
    }
}

