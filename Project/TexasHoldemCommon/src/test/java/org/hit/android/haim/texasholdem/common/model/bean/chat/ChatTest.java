package org.hit.android.haim.texasholdem.common.model.bean.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.util.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Haim Adrian
 * @since 03-Jul-21
 */
public class ChatTest {
    @Test
    public void testMessage_checkMessageSerialization() throws JsonProcessingException {
        String message = "message1";

        String json = JsonUtils.writeValueAsString(new TextNode(message));
        TextNode text = JsonUtils.readValueFromString(json, TextNode.class);

        Assertions.assertEquals(message, text.asText(), "Message got modified");
    }

    @Test
    public void testChannel_checkChannelSerialization() throws JsonProcessingException {
        String channelId = "id";
        Channel channel = new Channel(channelId, new HashSet<>(), new ArrayList<>());
        channel.getMessages().add(new Message("message1", LocalDateTime.now(), channelId, new Player()));
        channel.getMessages().add(new Message("message2", LocalDateTime.now(), channelId, new Player()));

        String json = JsonUtils.writeValueAsString(channel);
        Channel channel2 = JsonUtils.readValueFromString(json, Channel.class);

        Assertions.assertNotNull(channel2.getMessages(), "There were messages");
        Assertions.assertEquals(2, channel2.getMessages().size(), "There were 2 messages");
    }
}

