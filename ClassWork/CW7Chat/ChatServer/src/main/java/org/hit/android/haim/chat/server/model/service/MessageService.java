package org.hit.android.haim.chat.server.model.service;

import org.apache.logging.log4j.util.Strings;
import org.hit.android.haim.chat.server.model.bean.http.Message;
import org.hit.android.haim.chat.server.model.bean.http.User;
import org.hit.android.haim.chat.server.model.bean.mongo.Channel;
import org.hit.android.haim.chat.server.model.bean.mongo.MessageImpl;
import org.hit.android.haim.chat.server.model.repository.MessageRepository;
import org.hit.android.haim.chat.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wrap the access to the repository, so we can add additional logic between controller and repository.</br>
 * For example, we use a cache for better performance, so it is done in the service, rather than repository.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Component
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelService channelService;

    /**
     * See {@link UserRepository#findById(Object)}
     */
    @CachePut(value = "messageCache", key = "#id", condition = "#result != null") // Cache the results of this method.
    public Optional<Message> findById(String id) {
        Optional<MessageImpl> result = messageRepository.findById(id);
        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(messageImplToMessage(result.get()));
    }

    public List<Message> findByIds(List<String> ids) {
        List<Message> result = new ArrayList<>();

        // Use findById in order to use the cache for multiple results
        ids.forEach(id -> findById(id).ifPresent(result::add));

        return result;
    }

    /**
     * See {@link MessageRepository#findByChannelName(String)}
     */
    @CachePut(value = "messageCache", key = "#channelName") // Cache the results of this method.
    public Collection<Message> findByChannelName(String channelName) {
        return messageRepository.findByChannelName(channelName)
            .stream()
            .map(this::messageImplToMessage)
            .sorted(Comparator.comparing(Message::getDateTimeSent))
            .collect(Collectors.toList());
    }

    @CachePut(value = "messageCache", key = "#channelName") // Cache the results of this method.
    public Collection<Message> findLatestByChannelName(String channelName, LocalDateTime lastMessageDateTime) {
        return messageRepository.findByChannelName(channelName)
            .stream()
            .filter(message -> message.getDateTimeSent().isAfter(lastMessageDateTime))
            .map(this::messageImplToMessage)
            .sorted(Comparator.comparing(Message::getDateTimeSent))
            .collect(Collectors.toList());
    }

    /**
     * See {@link MessageRepository#findByUserIdIgnoreCase(String)}
     */
    public Collection<Message> findByUserId(String userId) {
        return messageRepository.findByUserIdIgnoreCase(userId).stream().map(this::messageImplToMessage).collect(Collectors.toList());
    }

    /**
     * See {@link MessageRepository#save(Object)}
     */
    @Caching(
        evict = {
            @CacheEvict(value = "messageCache", key = "#result.channel.name", condition = "#result.channel != null"),
            @CacheEvict(value = "messageCache", key = "#result.id")
        }
    )
    public Message save(String channelName, String userId, String messageContent) {
        if (Strings.isBlank(messageContent)) {
            throw new IllegalArgumentException("Cannot send empty message");
        }

        Optional<Channel> channel = channelService.findById(channelName);
        if (channel.isEmpty()) {
            throw new IllegalArgumentException("Channel not found: " + channelName);
        }

        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        MessageImpl message = messageRepository.save(new MessageImpl(UUID.randomUUID().toString(), messageContent, LocalDateTime.now(), channel.get().getName(), user.get().getId()));

        channel.get().getMessages().add(message.getId());
        channelService.update(channel.get());

        return messageImplToMessage(message);
    }

    /**
     * See {@link UserRepository#delete(Object)}<br/>
     */
    @Caching(
        evict = {
            @CacheEvict(value = "messageCache", key = "#result.channel.name", condition = "#result.channel != null"),
            @CacheEvict(value = "messageCache", key = "#result.id")
        }
    )
    public Message delete(String messageId) {
        Optional<MessageImpl> message = messageRepository.findById(messageId);
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Message does not exist. Id=" + messageId);
        }

        channelService.findById(message.get().getChannelName()).ifPresent(channel -> {
            if (channel.getMessages().removeIf(messageId::equals)) {
                channelService.update(channel);
            }
        });

        messageRepository.delete(message.get());
        return messageImplToMessage(message.get());
    }

    private Message messageImplToMessage(MessageImpl message) {
        return Message.builder()
            .id(message.getId())
            .message(message.getMessage())
            .dateTimeSent(message.getDateTimeSent())
            .channel(channelService.findById(message.getChannelName()).orElseGet(() -> Channel.builder().name(message.getChannelName()).build()))
            .user(userService.findById(message.getUserId()).orElseGet(() -> User.builder().id(message.getUserId()).build()))
            .build();
    }
}

