package org.hit.android.haim.texasholdem.server.model.service;

import org.apache.logging.log4j.util.Strings;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Message;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class that serves sending / reading messages to / from game's chat.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Service
public class MessageService {
    @Autowired
    private GameService gameService;

    /**
     * Collects all messages in a channel, sorted by timestamp<br/>
     * Channel name is the hash of a game, for which we want to retrieve the messages
     *
     * @param channelName Name of the channel to collect messages from
     */
    public Collection<Message> findByChannelName(String channelName) {
        return findByGameId(channelName);
    }

    /**
     * Collects all messages in a game, sorted by timestamp<br/>
     * Channel name is the hash of a game, for which we want to retrieve the messages
     *
     * @param gameHash The game identifier to collect messages from its chat
     */
    public Collection<Message> findByGameId(String gameHash) {
        Optional<GameEngine> game = gameService.findById(gameHash);

        //@formatter:off
        return game.map(value -> value.getChat().getMessages()
            .stream()
            .sorted(Comparator.comparing(Message::getDateTimeSent))
            .collect(Collectors.toList()))
            .orElseGet(ArrayList::new);
        //@formatter:on
    }

    /**
     * Collects all messages in a channel, that arrived after the given {@code lastMessageDateTime}, sorted by timestamp<br/>
     * Channel name is the hash of a game, for which we want to retrieve the messages
     *
     * @param channelName Name of the channel to collect messages from
     * @param lastMessageDateTime To collect all messages that arrived after this date time
     */
    public Collection<Message> findLatestByChannelName(String channelName, LocalDateTime lastMessageDateTime) {
        Optional<GameEngine> game = gameService.findById(channelName);

        //@formatter:off
        return game.map(value -> value.getChat().getMessages()
            .stream()
            .filter(message -> message.getDateTimeSent().isAfter(lastMessageDateTime))
            .sorted(Comparator.comparing(Message::getDateTimeSent))
            .collect(Collectors.toList()))
            .orElseGet(ArrayList::new);
        //@formatter:on
    }

    /**
     * Sends a message to the specified channel<br/>
     * Channel name is the hash of a game, for which we want to retrieve the messages
     * @param channelName Name of the channel to which we will send the message
     * @param userId The user identifier, who sent the message
     * @param messageContent The message to send
     * @return A message reference
     */
    public Message sendMessage(String channelName, String userId, String messageContent) {
        if (Strings.isBlank(messageContent)) {
            throw new IllegalArgumentException("Cannot send empty message");
        }

        Optional<GameEngine> game = gameService.findById(channelName);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("Game not found: " + channelName);
        }

        Player player = game.get().getPlayers().getPlayerById(userId);
        if (player == null) {
            throw new IllegalArgumentException("Player not found: " + userId);
        }

        Message message = new Message(messageContent, LocalDateTime.now(), game.get().getChat(), player);
        game.get().getChat().getMessages().add(message);
        return message;
    }
}

